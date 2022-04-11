package block.event.separator.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import block.event.separator.BlockEventSeparatorMod;
import block.event.separator.Counters;
import block.event.separator.SeparationMode;
import block.event.separator.interfaces.mixin.IMinecraftServer;
import block.event.separator.interfaces.mixin.IWorldServer;
import block.event.separator.utils.MathUtils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import net.minecraft.network.NetworkSystem;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SPacketCustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.world.WorldServer;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin implements IMinecraftServer {

	@Shadow private PlayerList playerList;
	@Shadow private WorldServer[] worlds;

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

	@Shadow private NetworkSystem getNetworkSystem( ) { return null; };

	@Inject(
		method = "updateTimeLightAndEntities",
		cancellable = true,
		at = @At(
			value = "HEAD"
		)
	)
	private void cancelTick(CallbackInfo ci) {
		if (subticks_bes > 0) {
			// keep packet handling going
			getNetworkSystem().networkTick();

			ci.cancel();
		}
	}

	@Inject(
		method = "tick",
		at = @At(
			value = "RETURN"
		)
	)
	private void adjustNextTickTime(CallbackInfo ci) {
		if (isPaused()) {
			return;
		}

		if (subticks_bes == 0) {
			mode_bes = BlockEventSeparatorMod.serverSeparationMode;
			separationInterval_bes = BlockEventSeparatorMod.serverSeparationInterval;

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
	public void postBlockEvents_bes() {
		maxBlockEventDepth_bes = Math.max(maxBlockEventDepth_bes, Counters.currentDepth);
		maxBlockEventTotal_bes = Math.max(maxBlockEventTotal_bes, Counters.total);
		maxMovingBlocksTotal_bes = Math.max(maxMovingBlocksTotal_bes, Counters.movingBlocksTotal);
	}

	private void syncNextTick_bes() {
		String channel = BlockEventSeparatorMod.PACKET_CHANNEL;
		int packetType = BlockEventSeparatorMod.NEXT_TICK_PACKET;

		ByteBuf buf = Unpooled.buffer();
		PacketBuffer buffer = new PacketBuffer(buf);

		buffer.writeByte(packetType);

		buffer.writeInt(maxOffset_bes);
		buffer.writeInt(separationInterval_bes);
		buffer.writeByte(mode_bes.index);

		Packet<?> packet = new SPacketCustomPayload(channel, buffer);
		playerList.sendPacketToAllPlayers(packet);
	}

	private void syncBlockEvents_bes() {
		int offsetLimit = subticks_bes / separationInterval_bes;

		for (WorldServer world : worlds) {
			((IWorldServer)world).sendBlockEvents_bes(offsetLimit);
		}
	}
}
