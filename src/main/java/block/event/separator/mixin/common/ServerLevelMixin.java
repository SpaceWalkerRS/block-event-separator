package block.event.separator.mixin.common;

import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import block.event.separator.BlockEvent;
import block.event.separator.BlockEventCounters;
import block.event.separator.BlockEventSeparator;
import block.event.separator.interfaces.mixin.IMinecraftServer;
import block.event.separator.interfaces.mixin.IServerLevel;

import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;

import net.minecraft.core.Holder;
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

	private int gcp_microtick; // field from G4mespeed Capture & Playback

	private ServerLevelMixin(WritableLevelData data, ResourceKey<Level> dimension, Holder<DimensionType> holder, Supplier<ProfilerFiller> supplier, boolean isClient, boolean isDebug, long seed) {
		super(data, dimension, holder, supplier, isClient, isDebug, seed);
	}

	@Inject(
		method = "runBlockEvents",
		at = @At(
			value = "HEAD"
		)
	)
	private void preBlockEvents(CallbackInfo ci) {
		BlockEventCounters.currentDepth = 0;
		BlockEventCounters.currentBatch = blockEvents.size();
		BlockEventCounters.total = 0;
		BlockEventCounters.movingBlocksTotal = 0;
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
			if (BlockEventCounters.currentBatch == 0) {
				BlockEventCounters.currentDepth++;
				BlockEventCounters.currentBatch = blockEvents.size();
			}

			BlockEventCounters.currentBatch--;
		}
	}

	@Inject(
		method = "runBlockEvents",
		at = @At(
			value = "RETURN"
		)
	)
	private void postBlockEvents(CallbackInfo ci) {
		((IMinecraftServer)server).postBlockEvents_bes();
	}

	@Inject(
		method = "doBlockEvent",
		at = @At(
			value = "HEAD"
		)
	)
	private void onBlockEvent(BlockEventData data, CallbackInfoReturnable<Boolean> cir) {
		BlockEventCounters.movingBlocksThisEvent = 0;
	}

	@Inject(
		method = "doBlockEvent",
		cancellable = true,
		at = @At(
			value = "RETURN"
		)
	)
	private void onSuccessfulBlockEvent(BlockEventData data, CallbackInfoReturnable<Boolean> cir) {
		if (cir.getReturnValue()) {
			// G4mespeed Capture & Playback can do multiple block events
			// per cycle, in which case we have to adjust our depth value.
			BlockEventCounters.currentDepth = Math.max(BlockEventCounters.currentDepth, gcp_microtick);
			BlockEventCounters.total++;

			int offset = switch (BlockEventSeparator.getServerMode()) {
				case DEPTH -> BlockEventCounters.currentDepth;
				case INDEX -> BlockEventCounters.total - 1;
				case BLOCK -> BlockEventCounters.movingBlocksTotal - BlockEventCounters.movingBlocksThisEvent;
				default    -> 0;
			};

			BlockEvent blockEvent = BlockEvent.of(data, offset);
			successfulBlockEvents_bes.add(blockEvent);

			cir.setReturnValue(false);
		}
	}

	@Override
	public void sendBlockEvents_bes(int maxOffset) {
		while (!successfulBlockEvents_bes.isEmpty()) {
			BlockEvent blockEvent = successfulBlockEvents_bes.peek();
			int offset = blockEvent.animationOffset;

			if (offset > maxOffset) {
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
		return 16.0f * BlockEventSeparator.blockEventDistanceSupplier.get();
	}
}
