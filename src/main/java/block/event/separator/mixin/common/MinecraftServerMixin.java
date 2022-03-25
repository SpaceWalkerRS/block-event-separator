package block.event.separator.mixin.common;

import java.util.function.BooleanSupplier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import block.event.separator.BlockEventCounters;
import block.event.separator.BlockEventSeparator;
import block.event.separator.SeparationMode;
import block.event.separator.interfaces.mixin.IMinecraftServer;
import block.event.separator.interfaces.mixin.IServerLevel;
import block.event.separator.utils.MathUtils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.ServerConnectionListener;
import net.minecraft.server.players.PlayerList;
import net.minecraft.util.profiling.GameProfiler;
import net.minecraft.world.level.GameRules;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin implements IMinecraftServer {

	@Shadow private int tickCount;
	@Shadow private GameProfiler profiler;
	@Shadow private PlayerList playerList;

	private SeparationMode mode_bes;
	private int separationInterval_bes;

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
		if (isPaused()) {
			return;
		}

		if (subticks_bes == 0) {
			mode_bes = BlockEventSeparator.serverSeparationMode;
			separationInterval_bes = BlockEventSeparator.serverSeparationInterval;

			prevPrevMaxOffset_bes = prevMaxOffset_bes;
			prevMaxOffset_bes = maxOffset_bes;
			switch (mode_bes) {
			case DEPTH:
				maxOffset_bes = maxBlockEventDepth_bes;
				break;
			case INDEX:
				maxOffset_bes = maxBlockEventTotal_bes - 1;
				break;
			case BLOCK:
				maxOffset_bes = maxMovingBlocksTotal_bes - 1;
				break;
			default:
				maxOffset_bes = 0;
			}

			// The max offset is synced every tick to make sure
			// clients with low frame rates don't get out of whack.
			syncMaxOffset_bes();

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
	public void postBlockEvents_bes() {
		maxBlockEventDepth_bes = Math.max(maxBlockEventDepth_bes, BlockEventCounters.currentDepth);
		maxBlockEventTotal_bes = Math.max(maxBlockEventTotal_bes, BlockEventCounters.total);
		maxMovingBlocksTotal_bes = Math.max(maxMovingBlocksTotal_bes, BlockEventCounters.movingBlocksTotal);
	}

	private void syncTime_bes() {
		for (ServerLevel level : getAllLevels()) {
			profiler.push("timeSync");

			long gameTime = level.getGameTime();
			long dayTime = level.getDayTime();
			boolean doDayLightCycle = level.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT);

			Packet<?> packet = new ClientboundSetTimePacket(gameTime, dayTime, doDayLightCycle);
			playerList.broadcastAll(packet, level.dimension.getType());

			profiler.pop();
		}
	}

	private void syncMaxOffset_bes() {
		String namespace = BlockEventSeparator.MOD_ID;
		String path = "next_tick";
		ResourceLocation id = new ResourceLocation(namespace, path);

		ByteBuf buf = Unpooled.buffer();
		FriendlyByteBuf buffer = new FriendlyByteBuf(buf);

		buffer.writeInt(maxOffset_bes);
		buffer.writeInt(separationInterval_bes);
		buffer.writeByte(mode_bes.index);

		Packet<?> packet = new ClientboundCustomPayloadPacket(id, buffer);
		playerList.broadcastAll(packet);
	}

	private void syncBlockEvents_bes() {
		int offsetLimit = subticks_bes / separationInterval_bes;

		for (ServerLevel level : getAllLevels()) {
			((IServerLevel)level).sendBlockEvents_bes(offsetLimit);
		}
	}
}
