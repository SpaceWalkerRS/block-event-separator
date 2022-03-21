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
import block.event.separator.BlockEventSeparator;
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

	private int currentDepth_bes;
	private int currentBatch_bes;
	private int total_bes;

	private int gcp_microtick; // field from G4mespeed Capture & Playback

	protected ServerLevelMixin(WritableLevelData data, ResourceKey<Level> dimension, DimensionType dimensionType, Supplier<ProfilerFiller> supplier, boolean isClient, boolean isDebug, long seed) {
		super(data, dimension, dimensionType, supplier, isClient, isDebug, seed);
	}

	@Inject(
		method = "runBlockEvents",
		at = @At(
			value = "HEAD"
		)
	)
	private void preBlockEvents(CallbackInfo ci) {
		currentDepth_bes = 0;
		currentBatch_bes = blockEvents.size();
		total_bes = 0;
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
			if (currentBatch_bes == 0) {
				currentDepth_bes++;
				currentBatch_bes = blockEvents.size();
			}

			currentBatch_bes--;
		}
	}

	@Inject(
		method = "runBlockEvents",
		at = @At(
			value = "RETURN"
		)
	)
	private void postBlockEvents(CallbackInfo ci) {
		((IMinecraftServer)server).postBlockEvents_bes(currentDepth_bes, total_bes);
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
			currentDepth_bes = Math.max(currentDepth_bes, gcp_microtick);
			total_bes++;

			int offset = switch (BlockEventSeparator.getMode()) {
				case DEPTH -> currentDepth_bes;
				case INDEX -> total_bes - 1;
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
