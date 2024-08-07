package block.event.separator.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import block.event.separator.TimerHelper;

import net.minecraft.client.DeltaTracker;

@Mixin(DeltaTracker.Timer.class)
public class DeltaTrackerTimerMixin {

	@Inject(
		method = "getGameTimeDeltaPartialTick",
		cancellable = true,
		at = @At(
			value = "RETURN",
			ordinal = 1
		)
	)
	private void adjustPartialTick(boolean bl, CallbackInfoReturnable<Float> cir) {
		cir.setReturnValue(TimerHelper.adjustPartialTick(cir.getReturnValue()));
	}
}
