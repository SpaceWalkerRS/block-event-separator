package block.event.separator.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import block.event.separator.BlockEventCounters;
import block.event.separator.interfaces.mixin.IMinecraft;
import block.event.separator.interfaces.mixin.ITimer;
import block.event.separator.utils.MathUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Timer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;

@Mixin(Minecraft.class)
public class MinecraftMixin implements IMinecraft {

	@Shadow private Timer timer;
	@Shadow private MultiPlayerGameMode gameMode;
	@Shadow private ClientLevel level;
	@Shadow private boolean pause;

	// These are the maximum animation offsets of the past few ticks.
	private int prevPrevMaxOffset_bes;
	private int prevMaxOffset_bes;
	private int maxOffset_bes;

	private boolean estimateNextTarget_bes;

	private int nextSubticksTarget_bes;
	private int queuedTicks_bes;

	@Inject(
		method = "runTick",
		locals = LocalCapture.CAPTURE_FAILHARD,
		at = @At(
			value = "INVOKE_STRING",
			target = "Lnet/minecraft/util/profiling/ProfilerFiller;push(Ljava/lang/String;)V",
			args = "ldc=scheduledExecutables"
		)
	)
	private void preTick(boolean isRunning, CallbackInfo ci, long time, int ticksThisFrame) {
		if (!pause) {
			BlockEventCounters.subticks += ticksThisFrame;
			queuedTicks_bes = 0;

			while (BlockEventCounters.subticks > BlockEventCounters.subticksTarget) {
				// If the client is ahead of the server, animation could speed up
				// for several frames before being corrected. To prevent some
				// instances of this, the next subticks target is estimated upon
				// a new client tick.
				if (estimateNextTarget_bes) {
					nextSubticksTarget_bes = Math.max(prevMaxOffset_bes, maxOffset_bes);
				}

				estimateNextTarget_bes = true;

				BlockEventCounters.subticks -= (1 + BlockEventCounters.subticksTarget);
				BlockEventCounters.subticksTarget = nextSubticksTarget_bes;
				nextSubticksTarget_bes = -1;

				queuedTicks_bes++;
			}
		}
	}

	@Inject(
		method = "runTick",
		at = @At(
			value = "INVOKE_STRING",
			target = "Lnet/minecraft/util/profiling/ProfilerFiller;push(Ljava/lang/String;)V",
			args = "ldc=tick"
		)
	)
	private void savePartialTick(boolean isRunning, CallbackInfo ci) {
		((ITimer)timer).savePartialTick_bes();
	}

	@Inject(
		method = "runTick",
		at = @At(
			value = "RETURN"
		)
	)
	private void loadPartialTick(boolean isRunning, CallbackInfo ci) {
		((ITimer)timer).loadPartialTick_bes();
	}

	@Inject(
		method = "tick",
		cancellable = true,
		at = @At(
			value = "HEAD",
			shift = Shift.AFTER
		)
	)
	private void cancelTick(CallbackInfo ci) {
		if (queuedTicks_bes > 0) {
			queuedTicks_bes--;
		} else {
			if (!pause && level != null) {
				// keep packet handling going
				gameMode.tick();
			}

			ci.cancel();
		}
	}

	@Override
	public void updateMaxOffset_bes(int maxOffset, int interval) {
		prevPrevMaxOffset_bes = prevMaxOffset_bes;
		prevMaxOffset_bes = maxOffset_bes;
		maxOffset_bes = maxOffset;

		int subticksTarget = interval * MathUtils.max(prevPrevMaxOffset_bes, prevMaxOffset_bes, maxOffset_bes);

		if (nextSubticksTarget_bes < 0) {
			BlockEventCounters.subticksTarget = subticksTarget;
			nextSubticksTarget_bes = 0;
		} else {
			estimateNextTarget_bes = false;

			// Make sure any queued subticks do not go lost...
			BlockEventCounters.subticksTarget += nextSubticksTarget_bes;
			nextSubticksTarget_bes = subticksTarget;
		}

		BlockEventCounters.movingBlocks = 0;
	}
}
