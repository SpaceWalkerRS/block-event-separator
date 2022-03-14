package block.event.separator.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import block.event.separator.BlockEventCounters;
import block.event.separator.utils.MathUtils;

import net.minecraft.client.Timer;

@Mixin(Timer.class)
public class TimerMixin {

	// We assume tickspeed mods change this field to adjust client tickspeed...
	@Shadow private float msPerTick;
	@Shadow private float partialTick;
	@Shadow private float tickDelta;

	// These must be initialized as 1 or the client freezes on startup.
	private float prevTpsFactor_bes = 1.0F;
	private float tpsFactor_bes = 1.0F;
	private float minTpsFactor_bes = 1.0F;

	private boolean newTick;

	@Inject(
		method = "advanceTime",
		at = @At(
			value = "FIELD",
			shift = Shift.BEFORE,
			ordinal = 0,
			target = "Lnet/minecraft/client/Timer;partialTick:F"
		)
	)
	private void beforeAdvanceTime(long time, CallbackInfoReturnable<Integer> cir) {
		// Each tick is lengthened based on the number of block events
		// that happened the ticks before. Pistons only animate in the
		// second and third ticks of their existence, so those need to
		// be lengthened. The tick after is also lengthened, both for
		// compatibility with G4mespeed and convenience when working
		// with 0-tick contraptions, which often operate in 3gt intervals.

		if (newTick) {
			float extraTickTime = BlockEventCounters.maxOffset * msPerTick;
			float nextTickTime = msPerTick + extraTickTime;

			float prevPrevTpsFactor = prevTpsFactor_bes;
			prevTpsFactor_bes = tpsFactor_bes;
			tpsFactor_bes = msPerTick / nextTickTime;

			minTpsFactor_bes = MathUtils.min(prevPrevTpsFactor, prevTpsFactor_bes, tpsFactor_bes);

			// adjust partial tick to new tick length
			partialTick *= minTpsFactor_bes;

			// reset block event counters ahead of next tick
			BlockEventCounters.currentOffset = 0;
			BlockEventCounters.maxOffset = 0;
		}

		tickDelta *= minTpsFactor_bes;
	}

	@Inject(
		method = "advanceTime",
		at = @At(
			value = "RETURN"
		)
	)
	private void afterAdvanceTime(long time, CallbackInfoReturnable<Integer> cir) {
		newTick = cir.getReturnValue() > 0;
	}
}
