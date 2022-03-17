package block.event.separator.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import block.event.separator.BlockEventCounters;
import block.event.separator.interfaces.mixin.ITimer;
import block.event.separator.utils.MathUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Timer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;

@Mixin(Minecraft.class)
public class MinecraftMixin {

	@Shadow private Timer timer;
	@Shadow private MultiPlayerGameMode gameMode;
	@Shadow private ClientLevel level;
	@Shadow private boolean pause;

	// These are the maximum animation offsets of the past few ticks.
	private int prevPrevMaxOffset_bes;
	private int prevMaxOffset_bes;
	private int maxOffset_bes;

	@Inject(
		method = "runTick",
		locals = LocalCapture.CAPTURE_FAILHARD,
		at = @At(
			value = "INVOKE_STRING",
			target = "Lnet/minecraft/util/profiling/ProfilerFiller;push(Ljava/lang/String;)V",
			args = "ldc=tick"
		)
	)
	private void preTick(boolean isRunning, CallbackInfo ci, long time, int ticksThisFrame) {
		if (!pause) {
			if (BlockEventCounters.subTicks == 0) {
				if (ticksThisFrame > 0) {
					// Each new tick we save how much previous
					// ticks have been lengthened.
					prevPrevMaxOffset_bes = prevMaxOffset_bes;
					prevMaxOffset_bes = maxOffset_bes;
					maxOffset_bes = 0;
				}
				if (maxOffset_bes == 0) {
					// New block events could arrive at any time
					// within the tick, not just the start.
					maxOffset_bes = BlockEventCounters.cMaxOffset;
					BlockEventCounters.subTicksTarget = MathUtils.max(prevPrevMaxOffset_bes, prevMaxOffset_bes, maxOffset_bes);
				}
			}

			BlockEventCounters.subTicks += ticksThisFrame;

			if (BlockEventCounters.subTicks > BlockEventCounters.subTicksTarget) {
				BlockEventCounters.subTicks = 0;
			}

			BlockEventCounters.cCurrentOffset = -1;
			BlockEventCounters.cMaxOffset = 0;
		}

		((ITimer)timer).adjustPartialTick_bes();
	}

	@Inject(
		method = "tick",
		cancellable = true,
		at = @At(
			value = "HEAD"
		)
	)
	private void cancelTick(CallbackInfo ci) {
		if (BlockEventCounters.subTicks > 0) {
			// keep packet handling going
			if (!pause && level != null) {
				gameMode.tick();
			}

			ci.cancel();
		}
	}
}
