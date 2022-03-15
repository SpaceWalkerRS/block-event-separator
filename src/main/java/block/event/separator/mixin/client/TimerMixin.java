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

	// These are the maximum animation offsets of the past few ticks.
	private int prevPrevMaxOffset_bes;
	private int prevMaxOffset_bes;
	private int maxOffset_bes;

	// These must be initialized as 1 or the client freezes on startup.
	private float prevTpsFactor_bes = 1.0F;
	private float tpsFactor_bes = 1.0F;

	private boolean newTick_bes;

	@Inject(
		method = "advanceTime",
		at = @At(
			value = "FIELD",
			shift = Shift.BEFORE,
			ordinal = 0,
			target = "Lnet/minecraft/client/Timer;partialTick:F"
		)
	)
	private void preAdvanceTime(long time, CallbackInfoReturnable<Integer> cir) {
		// Each tick is lengthened based on the number of block events
		// that happened the ticks before. Pistons only animate in the
		// second and third ticks of their existence, so those need to
		// be lengthened too, to keep the animations smooth.

		if (newTick_bes) {
			prevPrevMaxOffset_bes = prevMaxOffset_bes;
			prevMaxOffset_bes = maxOffset_bes;
			maxOffset_bes = BlockEventCounters.maxOffset;

			// Determine the offset that will be used to lengthen the current tick.
			int maxOffset = MathUtils.max(prevPrevMaxOffset_bes, prevMaxOffset_bes, maxOffset_bes);

			// determine length of this tick
			float extraTickLength = maxOffset * msPerTick;
			float tickLength = msPerTick + extraTickLength;

			prevTpsFactor_bes = tpsFactor_bes;
			tpsFactor_bes = msPerTick / tickLength;

			// adjust partial tick to new tick length
			partialTick *= (tpsFactor_bes / prevTpsFactor_bes);
		}

		tickDelta *= tpsFactor_bes;
	}

	@Inject(
		method = "advanceTime",
		at = @At(
			value = "RETURN"
		)
	)
	private void postAdvanceTime(long time, CallbackInfoReturnable<Integer> cir) {
		newTick_bes = cir.getReturnValue() > 0;
	}
}
