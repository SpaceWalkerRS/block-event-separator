package block.event.separator.mixin.common;

import java.util.LinkedList;
import java.util.Queue;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import block.event.separator.BlockEvent;
import block.event.separator.BlockEventSeparatorMod;
import block.event.separator.Counters;
import block.event.separator.interfaces.mixin.IMinecraftServer;
import block.event.separator.interfaces.mixin.IWorldServer;

import net.minecraft.block.BlockEventData;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketBlockAction;
import net.minecraft.profiler.Profiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;

@Mixin(WorldServer.class)
public abstract class WorldServerMixin extends World implements IWorldServer {

	@Shadow private MinecraftServer server;

	private final Queue<BlockEvent> successfulBlockEvents_bes = new LinkedList<>();

	// If the last batch does not have any successful block events,
	// its depth can be ignored for the max offset calculations.
	private boolean ignoreLastBatch_bes;
	private boolean hasSuccessThisBatch_bes;

	private WorldServerMixin(ISaveHandler saveHandlerIn, WorldInfo info, WorldProvider providerIn, Profiler profilerIn, boolean client) {
		super(saveHandlerIn, info, providerIn, profilerIn, client);
	}

	@Inject(
		method = "sendQueuedBlockEvents",
		at = @At(
			value = "HEAD"
		)
	)
	private void preBlockEvents(CallbackInfo ci) {
		Counters.currentDepth = -1;
		Counters.total = 0;
		Counters.movingBlocksTotal = 0;
	}

	@Inject(
		method = "sendQueuedBlockEvents",
		at = @At(
			value = "CONSTANT",
			args = "intValue=1"
		)
	)
	private void onNextBlockEventBatch(CallbackInfo ci) {
		Counters.currentDepth++;

		ignoreLastBatch_bes = !hasSuccessThisBatch_bes;
		hasSuccessThisBatch_bes = false;
	}

	@Inject(
		method = "sendQueuedBlockEvents",
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

	@Inject(
		method = "fireBlockEvent",
		at = @At(
			value = "HEAD"
		)
	)
	private void onBlockEvent(BlockEventData data, CallbackInfoReturnable<Boolean> cir) {
		Counters.movingBlocksThisEvent = 0;
	}

	@Inject(
		method = "fireBlockEvent",
		cancellable = true,
		at = @At(
			value = "RETURN"
		)
	)
	private void onSuccessfulBlockEvent(BlockEventData data, CallbackInfoReturnable<Boolean> cir) {
		if (cir.getReturnValue()) {
			Counters.total++;

			int offset;
			switch (BlockEventSeparatorMod.serverSeparationMode) {
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

			hasSuccessThisBatch_bes = true;

			BlockEvent blockEvent = BlockEvent.of(data, offset);
			successfulBlockEvents_bes.add(blockEvent);

			cir.setReturnValue(false);
		}
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
		float range = 64.0F;
		DimensionType dimension = provider.getDimensionType();

		Packet<?> packet = new SPacketBlockAction(be.pos, be.block, be.type, be.data);
		server.getPlayerList().sendToAllNearExcept(null, x, y, z, range, dimension.getId(), packet);
	}
}
