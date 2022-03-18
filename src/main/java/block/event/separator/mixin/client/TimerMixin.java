package block.event.separator.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import block.event.separator.BlockEventCounters;
import block.event.separator.interfaces.mixin.ITimer;

import net.minecraft.client.Timer;

@Mixin(Timer.class)
public class TimerMixin implements ITimer {

	@Shadow private float partialTick;

	private float savedPartialTick_bes;

	@Inject(
		method = "advanceTime",
		at = @At(
			value = "HEAD"
		)
	)
	private void onAdvanceTime(long time, CallbackInfoReturnable<Integer> cir) {
		partialTick = savedPartialTick_bes;
	}

	@Override
	public void adjustPartialTick_bes() {
		savedPartialTick_bes = partialTick;

		float subTicks = BlockEventCounters.subticks;
		float range = BlockEventCounters.subticksTarget + 1;

		partialTick = (subTicks + partialTick) / range;
	}
}
