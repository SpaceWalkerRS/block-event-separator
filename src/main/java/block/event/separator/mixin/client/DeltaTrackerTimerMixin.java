package block.event.separator.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import block.event.separator.TimerHelper;
import block.event.separator.interfaces.mixin.IMinecraft;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;

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
		if ((Object)this == ((IMinecraft)Minecraft.getInstance()).getTimer_bes()) {
			cir.setReturnValue(TimerHelper.adjustPartialTick(cir.getReturnValue()));
		}
	}
}
