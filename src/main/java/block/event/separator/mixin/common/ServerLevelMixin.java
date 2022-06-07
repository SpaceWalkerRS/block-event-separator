package block.event.separator.mixin.common;

import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import block.event.separator.BlockEvent;
import block.event.separator.BlockEventSeparatorMod;
import block.event.separator.Counters;
import block.event.separator.interfaces.mixin.IMinecraftServer;
import block.event.separator.interfaces.mixin.IServerLevel;

import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockEventPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.BlockEventData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin extends Level implements IServerLevel {

	@Shadow private MinecraftServer server;
	@Shadow private ObjectLinkedOpenHashSet<BlockEventData> blockEvents;

	private final Queue<BlockEvent> successfulBlockEvents_bes = new LinkedList<>();

	// If the last batch does not have any successful block events,
	// its depth can be ignored for the max offset calculations.
	private boolean ignoreLastBatch_bes;
	private int gcp_microtick; // field from G4mespeed Capture & Playback

	private ServerLevelMixin(WritableLevelData data, ResourceKey<Level> dimension, DimensionType dimensionType, Supplier<ProfilerFiller> profiler, boolean isClient, boolean isDebug, long seed) {
		super(data, dimension, dimensionType, profiler, isClient, isDebug, seed);
	}

	@Shadow private boolean doBlockEvent(BlockEventData data) { return false; }

	@Inject(
		method = "runBlockEvents",
		at = @At(
			value = "HEAD"
		)
	)
	private void preBlockEvents(CallbackInfo ci) {
		Counters.currentDepth = 0;
		Counters.currentBatch = blockEvents.size();
		Counters.total = 0;
		Counters.movingBlocksTotal = 0;
	}

	@Inject(
		method = "runBlockEvents",
		at = @At(
			value = "INVOKE",
			shift = Shift.BEFORE,
			ordinal = 0,
			target = "Lit/unimi/dsi/fastutil/objects/ObjectLinkedOpenHashSet;isEmpty()Z"
		)
	)
	private void onNextBlockEvent(CallbackInfo ci) {
		if (!blockEvents.isEmpty()) {
			if (Counters.currentBatch == 0) {
				Counters.currentDepth++;
				Counters.currentBatch = blockEvents.size();

				ignoreLastBatch_bes = true;
			}

			Counters.currentBatch--;
		}
	}

	@Redirect(
		method = "runBlockEvents",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/level/ServerLevel;doBlockEvent(Lnet/minecraft/world/level/BlockEventData;)Z"
		)
	)
	private boolean cancelBlockEventPacket(ServerLevel level, BlockEventData data) {
		Counters.movingBlocksThisEvent = 0;

		if (doBlockEvent(data)) {
			// G4mespeed Capture & Playback can do multiple block events
			// per cycle, in which case we have to adjust our depth value.
			Counters.currentDepth = Math.max(Counters.currentDepth, gcp_microtick);
			Counters.total++;

			int offset;
			switch (BlockEventSeparatorMod.getServerSeparationMode()) {
			case DEPTH:
				offset = Counters.currentDepth;
				break;
			case INDEX:
				offset = Counters.total - 1;
				break;
			case BLOCK:
				offset = Counters.movingBlocksTotal - Counters.movingBlocksThisEvent;
				break;
			default:
				offset = 0;
			}

			ignoreLastBatch_bes = false;

			BlockEvent blockEvent = BlockEvent.of(data, offset);
			successfulBlockEvents_bes.add(blockEvent);
		}

		return false;
	}

	@Inject(
		method = "runBlockEvents",
		at = @At(
			value = "RETURN"
		)
	)
	private void postBlockEvents(CallbackInfo ci) {
		if (ignoreLastBatch_bes) {
			Counters.currentDepth--;
		}

		((IMinecraftServer)server).postBlockEvents_bes();
	}

	@Override
	public void sendBlockEvents_bes(int offsetLimit) {
		while (!successfulBlockEvents_bes.isEmpty()) {
			BlockEvent blockEvent = successfulBlockEvents_bes.peek();
			int offset = blockEvent.animationOffset;

			if (offset > offsetLimit) {
				break;
			}

			successfulBlockEvents_bes.poll();
			sendBlockEvent_bes(blockEvent);
		}
	}

	private void sendBlockEvent_bes(BlockEvent be) {
		int x = be.pos.getX();
		int y = be.pos.getY();
		int z = be.pos.getZ();
		float range = getBlockEventRange_bes();
		ResourceKey<Level> dimension = dimension();

		Packet<?> packet = new ClientboundBlockEventPacket(be.pos, be.block, be.type, be.data);
		server.getPlayerList().broadcast(null, x, y, z, range, dimension, packet);
	}

	private float getBlockEventRange_bes() {
		// Convert chunk distance to block distance
		return 16.0f * BlockEventSeparatorMod.blockEventDistanceSupplier.get();
	}
}
