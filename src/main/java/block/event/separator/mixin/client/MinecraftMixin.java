package block.event.separator.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import block.event.separator.AnimationMode;
import block.event.separator.BlockEventSeparatorMod;
import block.event.separator.Counters;
import block.event.separator.interfaces.mixin.ILevel;
import block.event.separator.interfaces.mixin.IMinecraft;
import block.event.separator.utils.MathUtils;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;

@Mixin(Minecraft.class)
public class MinecraftMixin implements IMinecraft {

	@Shadow private DeltaTracker.Timer deltaTracker;
	@Shadow private MultiPlayerGameMode gameMode;
	@Shadow private ClientLevel level;
	@Shadow private boolean pause;

	// These are the maximum animation offsets of the past few ticks.
	private int prevPrevMaxOffset_bes;
	private int prevMaxOffset_bes;
	private int maxOffset_bes;

	private boolean estimateNextTarget_bes;

	private int nextSubticksTarget_bes;
	private int ticksThisFrame_bes;

	@Shadow private boolean isLevelRunningNormally() { return false; }

	@Inject(
		method = "runTick",
		locals = LocalCapture.CAPTURE_FAILHARD,
		at = @At(
			value = "INVOKE_STRING",
			target = "Lnet/minecraft/util/profiling/ProfilerFiller;push(Ljava/lang/String;)V",
			args = "ldc=scheduledExecutables"
		)
	)
	private void preTick(boolean isRunning, CallbackInfo ci, int ticksThisFrame) {
		if (!pause && isLevelRunningNormally()) {
			Counters.subticks += ticksThisFrame;
			ticksThisFrame_bes = 0;

			while (Counters.subticks > Counters.subticksTarget) {
				// If the client is ahead of the server, animation could speed up
				// for several frames before being corrected. To prevent some
				// instances of this, the next subticks target is estimated upon
				// a new client tick.
				if (estimateNextTarget_bes) {
					nextSubticksTarget_bes = Math.max(prevMaxOffset_bes, maxOffset_bes);
				}

				estimateNextTarget_bes = true;

				Counters.subticks -= (1 + Counters.subticksTarget);
				Counters.subticksTarget = nextSubticksTarget_bes;
				nextSubticksTarget_bes = -1;

				Counters.ticks++;
				ticksThisFrame_bes++;
			}
		} else {
			ticksThisFrame_bes = ticksThisFrame;
		}
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
		if (ticksThisFrame_bes > 0) {
			ticksThisFrame_bes--;
		} else if (isLevelRunningNormally()) {
			if (!pause && level != null) {
				// keep packet handling going
				gameMode.tick();

				tickFixedSpeed_bes();
			}

			ci.cancel();
		}
	}

	@Inject(
		method = "tick",
		at = @At(
			value = "TAIL"
		)
	)
	private void tickFixedSpeed(CallbackInfo ci) {
		if (!pause && isLevelRunningNormally() && level != null) {
			tickFixedSpeed_bes();
		}
	}

	@Inject(
		method = "disconnect(Lnet/minecraft/client/gui/screens/Screen;)V",
		at = @At(
			value = "HEAD"
		)
	)
	private void onDisconnect(CallbackInfo ci) {
		BlockEventSeparatorMod.isConnectedToBesServer = false;
	}

	@Override
	public void onHandshake_bes(String modVersion) {
		BlockEventSeparatorMod.isConnectedToBesServer = true;
	}

	@Override
	public void updateMaxOffset_bes(int maxOffset, int interval) {
		prevPrevMaxOffset_bes = prevMaxOffset_bes;
		prevMaxOffset_bes = maxOffset_bes;
		maxOffset_bes = maxOffset;

		int subticksTarget = interval * MathUtils.max(prevPrevMaxOffset_bes, prevMaxOffset_bes, maxOffset_bes);

		if (nextSubticksTarget_bes < 0) {
			Counters.subticksTarget = subticksTarget;
			nextSubticksTarget_bes = 0;
		} else {
			estimateNextTarget_bes = false;

			// Make sure any queued subticks do not go lost...
			Counters.subticksTarget += nextSubticksTarget_bes;
			nextSubticksTarget_bes = subticksTarget;
		}

		Counters.movingBlocks = 0;
	}

	private void tickFixedSpeed_bes() {
		if (BlockEventSeparatorMod.getAnimationMode() == AnimationMode.FIXED_SPEED) {
			((ILevel)level).tickMovingBlocks_bes();
		}
	}
}
