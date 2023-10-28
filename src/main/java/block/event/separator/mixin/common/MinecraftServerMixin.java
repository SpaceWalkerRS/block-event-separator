package block.event.separator.mixin.common;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.BooleanSupplier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import block.event.separator.BlockEventSeparatorMod;
import block.event.separator.Counters;
import block.event.separator.SeparationMode;
import block.event.separator.interfaces.mixin.IMinecraftServer;
import block.event.separator.interfaces.mixin.IServerLevel;
import block.event.separator.network.BESPayload;
import block.event.separator.network.FreezePayload;
import block.event.separator.network.HandshakePayload;
import block.event.separator.network.TickPayload;
import block.event.separator.utils.MathUtils;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerConnectionListener;
import net.minecraft.server.players.PlayerList;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.GameRules;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin implements IMinecraftServer {

	@Shadow private int tickCount;
	@Shadow private ProfilerFiller profiler;
	@Shadow private PlayerList playerList;

	private final Set<UUID> connectedPlayers = new HashSet<>();

	private SeparationMode mode_bes;
	private int separationInterval_bes;
	private boolean frozen_bes;

	// These are the maximum animation offsets of the past few ticks.
	private int prevPrevMaxOffset_bes;
	private int prevMaxOffset_bes;
	private int maxOffset_bes;

	private int subticks_bes;
	private int subticksTarget_bes;

	/** The greatest block event depth across all levels in the past tick. */
	private int maxBlockEventDepth_bes;
	/** The greatest number of block events across all levels in the past tick. */
	private int maxBlockEventTotal_bes;
	/** The greatest number of moving blocks across all levels in the past tick. */
	private int maxMovingBlocksTotal_bes;

	@Shadow protected abstract Iterable<ServerLevel> getAllLevels();
	@Shadow protected abstract ServerConnectionListener getConnection();

	@Inject(
		method = "tickChildren",
		cancellable = true,
		at = @At(
			value = "HEAD"
		)
	)
	private void cancelTick(BooleanSupplier isAheadOfTime, CallbackInfo ci) {
		boolean wasFrozen = frozen_bes;
		frozen_bes = BlockEventSeparatorMod.isFrozen();

		if (frozen_bes != wasFrozen) {
			syncFreeze_bes();
		}

		if (subticks_bes > 0) {
			// G4mespeed relies on time sync packets
			// to sync the client to the server.
			if (tickCount % 20 == 0) {
				syncTime_bes();
			}
			// keep packet handling going
			getConnection().tick();

			ci.cancel();
		}
	}

	@Inject(
		method = "waitUntilNextTick",
		at = @At(
			value = "HEAD"
		)
	)
	private void adjustNextTickTime(CallbackInfo ci) {
		if (frozen_bes || isPaused()) {
			return;
		}

		if (subticks_bes == 0) {
			mode_bes = BlockEventSeparatorMod.getServerSeparationMode();
			separationInterval_bes = BlockEventSeparatorMod.getServerSeparationInterval();

			prevPrevMaxOffset_bes = prevMaxOffset_bes;
			prevMaxOffset_bes = maxOffset_bes;
			maxOffset_bes = switch (mode_bes) {
				case DEPTH -> maxBlockEventDepth_bes;
				case INDEX -> maxBlockEventTotal_bes - 1;
				case BLOCK -> maxMovingBlocksTotal_bes - 1;
				default    -> 0;
			};

			// The max offset is synced every tick to make sure
			// clients with low frame rates don't get out of whack.
			syncNextTick_bes();

			// Reset the block event counters to the lowest
			// value for which there is no separation.
			maxBlockEventDepth_bes = 0; // depth is zero-indexed
			maxBlockEventTotal_bes = 1; // total is not
			maxMovingBlocksTotal_bes = 1;

			subticksTarget_bes = separationInterval_bes * MathUtils.max(prevPrevMaxOffset_bes, prevMaxOffset_bes, maxOffset_bes);
		}

		syncBlockEvents_bes();

		if (++subticks_bes > subticksTarget_bes) {
			subticks_bes = 0;
		}
	}

	@Override
	public void onPlayerJoin_bes(ServerPlayer player) {

	}

	@Override
	public void onPlayerLeave_bes(ServerPlayer player) {
		connectedPlayers.remove(player.getUUID());
	}

	@Override
	public void postBlockEvents_bes() {
		maxBlockEventDepth_bes = Math.max(maxBlockEventDepth_bes, Counters.currentDepth);
		maxBlockEventTotal_bes = Math.max(maxBlockEventTotal_bes, Counters.total);
		maxMovingBlocksTotal_bes = Math.max(maxMovingBlocksTotal_bes, Counters.movingBlocksTotal);
	}

	@Override
	public void onHandshake_bes(ServerPlayer player, String modVersion) {
		if (connectedPlayers.add(player.getUUID())) {
			player.connection.send(new ClientboundCustomPayloadPacket(new HandshakePayload(BlockEventSeparatorMod.MOD_VERSION)));

			playerList.sendPlayerPermissionLevel(player);
		}
	}

	@Override
	public boolean isBesClient(ServerPlayer player) {
		return connectedPlayers.contains(player.getUUID());
	}

	private void syncFreeze_bes() {
		send_bes(new FreezePayload(frozen_bes));
	}

	private void syncTime_bes() {
		for (ServerLevel level : getAllLevels()) {
			profiler.push("timeSync");

			long gameTime = level.getGameTime();
			long dayTime = level.getDayTime();
			boolean doDayLightCycle = level.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT);

			Packet<?> packet = new ClientboundSetTimePacket(gameTime, dayTime, doDayLightCycle);
			playerList.broadcastAll(packet, level.dimension());

			profiler.pop();
		}
	}

	private void syncNextTick_bes() {
		send_bes(new TickPayload(maxOffset_bes, separationInterval_bes, mode_bes));
	}

	private void syncBlockEvents_bes() {
		int offsetLimit = subticks_bes / separationInterval_bes;

		for (ServerLevel level : getAllLevels()) {
			((IServerLevel)level).sendBlockEvents_bes(offsetLimit);
		}
	}

	private void send_bes(BESPayload payload) {
		Packet<?> packet = null;

		for (UUID uuid : connectedPlayers) {
			ServerPlayer player = playerList.getPlayer(uuid);

			if (player != null) {
				if (packet == null) {
					packet = new ClientboundCustomPayloadPacket(payload);
				}

				player.connection.send(packet);
			}
		}
	}
}
