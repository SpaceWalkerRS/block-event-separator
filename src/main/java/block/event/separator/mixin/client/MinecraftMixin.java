package block.event.separator.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
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
	// This field is used when the client receives an update from the
	// server before it is done with all the subticks of the previous
	// ticks.
	private int nextMaxOffset_bes;

	@Inject(
		method = "runTick",
		locals = LocalCapture.CAPTURE_FAILHARD,
		at = @At(
			value = "INVOKE_STRING",
			ordinal = 0,
			target = "Lnet/minecraft/util/profiling/ProfilerFiller;push(Ljava/lang/String;)V",
			args = "ldc=scheduledExecutables"
		)
	)
	private void preTick(boolean isRunning, CallbackInfo ci, long time, int ticksThisFrame) {
		if (!pause) {
			BlockEventCounters.subticks += ticksThisFrame;

			if (BlockEventCounters.subticks > BlockEventCounters.subticksTarget) {
				int nextMaxOffset = nextMaxOffset_bes;

				prevPrevMaxOffset_bes = prevMaxOffset_bes;
				prevMaxOffset_bes = maxOffset_bes;
				maxOffset_bes = 0;
				nextMaxOffset_bes = 0;

				BlockEventCounters.subticks = 0;
				BlockEventCounters.subticksTarget = 0;

				updateMaxOffset_bes(nextMaxOffset);
			}
		}

		((ITimer)timer).adjustPartialTick_bes();
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
		// Only allow client ticks to happen
		// in the first subtick, skip the rest.
		if (BlockEventCounters.subticks > 0) {
			// keep packet handling going
			if (!pause && level != null) {
				gameMode.tick();
			}

			ci.cancel();
		}
	}

	@Override
	public void updateMaxOffset_bes(int maxOffset) {
		if (maxOffset_bes > 0) {
			// If the client is running a tad bit behind, save
			// the value for when it starts the next tick.
			nextMaxOffset_bes = maxOffset;
		} else {
			maxOffset_bes = maxOffset;
		}

		BlockEventCounters.subticksTarget = MathUtils.max(prevPrevMaxOffset_bes, prevMaxOffset_bes, maxOffset_bes);
	}
}
