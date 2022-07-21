package block.event.separator.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import block.event.separator.AnimationMode;
import block.event.separator.BlockEventSeparatorMod;
import block.event.separator.Counters;
import block.event.separator.KeyMappings;
import block.event.separator.TimerHelper;
import block.event.separator.interfaces.mixin.IMinecraft;
import block.event.separator.interfaces.mixin.IMultiPlayerLevel;
import block.event.separator.utils.MathUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Timer;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.multiplayer.MultiPlayerLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

@Mixin(Minecraft.class)
public class MinecraftMixin implements IMinecraft {

	@Shadow private Timer timer;
	@Shadow private MultiPlayerGameMode gameMode;
	@Shadow private MultiPlayerLevel level;
	@Shadow private LocalPlayer player;
	@Shadow private boolean pause;
	@Shadow private float pausePartialTick;

	// These are the maximum animation offsets of the past few ticks.
	private int prevPrevMaxOffset_bes;
	private int prevMaxOffset_bes;
	private int maxOffset_bes;

	private boolean estimateNextTarget_bes;

	private int nextSubticksTarget_bes;
	private int ticksThisFrame_bes;

	private boolean serverFrozen_bes;

	@Inject(
		method = "<init>",
		at = @At(
			value = "RETURN"
		)
	)
	private void init(CallbackInfo ci) {
		TimerHelper.init(timer);
	}

	@Inject(
		method = "runTick",
		at = @At(
			value = "INVOKE_STRING",
			target = "Lnet/minecraft/util/profiling/GameProfiler;push(Ljava/lang/String;)V",
			args = "ldc=scheduledExecutables"
		)
	)
	private void preTick(boolean isRunning, CallbackInfo ci) {
		if (!pause && !serverFrozen_bes) {
			Counters.subticks += timer.ticks;
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
			ticksThisFrame_bes = timer.ticks;
		}
	}

	@Inject(
		method = "runTick",
		at = @At(
			value = "INVOKE_STRING",
			target = "Lnet/minecraft/util/profiling/GameProfiler;push(Ljava/lang/String;)V",
			args = "ldc=render"
		)
	)
	private void savePartialTick(boolean isRunning, CallbackInfo ci) {
		TimerHelper.savePartialTick();

		if (Counters.frozen) {
			timer.partialTick = pausePartialTick;
		}
	}

	@Inject(
		method = "runTick",
		at = @At(
			value = "RETURN"
		)
	)
	private void loadPartialTick(boolean isRunning, CallbackInfo ci) {
		TimerHelper.loadPartialTick();
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
		} else if (!serverFrozen_bes) {
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
		if (!pause && !serverFrozen_bes && level != null) {
			tickFixedSpeed_bes();
		}
	}

	@Inject(
		method = "clearLevel(Lnet/minecraft/client/gui/screens/Screen;)V",
		at = @At(
			value = "HEAD"
		)
	)
	private void onDisconnect(CallbackInfo ci) {
		BlockEventSeparatorMod.isConnectedToBesServer = false;
	}

	@Inject(
		method = "handleKeybinds",
		at = @At(
			value = "RETURN"
		)
	)
	private void handleKeyMappings(CallbackInfo ci) {
		while (KeyMappings.TOGGLE_ANIMATION_MODE.consumeClick()) {
			int nextIndex = BlockEventSeparatorMod.animationMode.index + 1;

			if (nextIndex >= AnimationMode.getCount()) {
				nextIndex = 0;
			}

			AnimationMode mode = AnimationMode.fromIndex(nextIndex);
			BlockEventSeparatorMod.animationMode = mode;

			Component text = new TextComponent("Set animation mode to " + mode.name);
			player.displayClientMessage(text, true);
		}
	}

	@Override
	public void onHandshake_bes(String modVersion) {
		BlockEventSeparatorMod.isConnectedToBesServer = true;
	}

	@Override
	public void setFrozen_bes(boolean frozen) {
		boolean wasFrozen = serverFrozen_bes;
		serverFrozen_bes = frozen;

		if (!wasFrozen && frozen) {
			TimerHelper.freezePartialTick = timer.partialTick;
			pausePartialTick = TimerHelper.adjustPartialTick(timer.partialTick);
		} else
		if (wasFrozen && !frozen) {
			timer.partialTick = TimerHelper.freezePartialTick;
		}

		Counters.frozen = serverFrozen_bes;
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
		if (BlockEventSeparatorMod.animationMode == AnimationMode.FIXED_SPEED) {
			((IMultiPlayerLevel)level).tickMovingBlocks_bes();
		}
	}
}
