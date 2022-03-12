package block.event.separator.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import block.event.separator.interfaces.mixin.IMinecraftServer;

import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockEventData;

@Mixin(ServerLevel.class)
public class ServerLevelMixin {

	@Shadow private MinecraftServer server;
	@Shadow private ObjectLinkedOpenHashSet<BlockEventData> blockEvents;

	private long currentDepth_bes;
	private long currentBatch_bes;
	private long blockEventTotal_bes;

	@Inject(
		method = "runBlockEvents",
		at = @At(
			value = "INVOKE",
			target = "Lit/unimi/dsi/fastutil/objects/ObjectLinkedOpenHashSet;isEmpty()Z"
		)
	)
	private void onNextBlockEvent(CallbackInfo ci) {
		if (currentBatch_bes == 0) {
			currentDepth_bes++;
			currentBatch_bes = blockEvents.size();
		}

		currentBatch_bes--;
	}

	@Inject(
		method = "runBlockEvents",
		at = @At(
			value = "RETURN"
		)
	)
	private void afterBlockEvents(CallbackInfo ci) {
		((IMinecraftServer)server).postBlockEvents_bes(currentDepth_bes, blockEventTotal_bes);

		// Reset block event counters ahead of next tick.
		// Depth is set to -1 because it is incremented
		// before the first block event is processed.
		currentDepth_bes = -1;
		currentBatch_bes = 0;
		blockEventTotal_bes = 0;
	}

	@Inject(
		method = "doBlockEvent",
		at = @At(
			value = "INVOKE",
			shift = Shift.BEFORE,
			target = "Lnet/minecraft/world/level/block/state/BlockState;triggerEvent(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;II)Z"
		)
	)
	private void onBlockEvent(CallbackInfoReturnable<Boolean> cir) {
		blockEventTotal_bes++;
	}
}
