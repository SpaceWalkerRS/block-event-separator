package block.event.separator.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import block.event.separator.BlockEventCounters;
import block.event.separator.interfaces.mixin.IMinecraft;
import block.event.separator.interfaces.mixin.ITimer;

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

	private int nextSubticksTarget_bes;

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
			BlockEventCounters.subticks += ticksThisFrame;

			if (BlockEventCounters.subticks > BlockEventCounters.subticksTarget) {
				BlockEventCounters.subticks = 0;
				BlockEventCounters.subticksTarget = nextSubticksTarget_bes;
				nextSubticksTarget_bes = 0;
			}
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
	public void syncSubticks_bes(int subticksTarget) {
		if (BlockEventCounters.subticksTarget > 0) {
			// If the client is running a tad bit behind, save
			// the value for when it starts the next tick.
			nextSubticksTarget_bes = subticksTarget;
		} else {
			BlockEventCounters.subticksTarget = subticksTarget;
		}
	}
}
