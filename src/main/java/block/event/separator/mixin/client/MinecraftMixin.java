package block.event.separator.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import block.event.separator.AnimationMode;
import block.event.separator.BlockEventSeparatorMod;
import block.event.separator.Counters;
import block.event.separator.KeyBindings;
import block.event.separator.TimerHelper;
import block.event.separator.interfaces.mixin.IMinecraft;
import block.event.separator.interfaces.mixin.IWorld;
import block.event.separator.utils.MathUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.Timer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

@Mixin(Minecraft.class)
public class MinecraftMixin implements IMinecraft {

	@Shadow private Timer timer;
	@Shadow private PlayerControllerMP playerController;
	@Shadow private WorldClient world;
	@Shadow private EntityPlayerSP player;
	@Shadow private boolean isGamePaused;
	@Shadow private float renderPartialTicksPaused;

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
		method = "runGameLoop",
		at = @At(
			value = "INVOKE_STRING",
			target = "Lnet/minecraft/profiler/Profiler;startSection(Ljava/lang/String;)V",
			args = "ldc=scheduledExecutables"
		)
	)
	private void preTick(CallbackInfo ci) {
		if (!isGamePaused && !serverFrozen_bes) {
			Counters.subticks += timer.elapsedTicks;
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
			ticksThisFrame_bes = timer.elapsedTicks;
		}
	}

	@Inject(
		method = "runGameLoop",
		at = @At(
			value = "INVOKE_STRING",
			target = "Lnet/minecraft/profiler/Profiler;startSection(Ljava/lang/String;)V",
			args = "ldc=render"
		)
	)
	private void savePartialTick(CallbackInfo ci) {
		TimerHelper.savePartialTick();

		if (Counters.frozen) {
			timer.renderPartialTicks = renderPartialTicksPaused;
		}
	}

	@Inject(
		method = "runGameLoop",
		at = @At(
			value = "RETURN"
		)
	)
	private void loadPartialTick(CallbackInfo ci) {
		TimerHelper.loadPartialTick();
	}

	@Inject(
		method = "runTick",
		cancellable = true,
		at = @At(
			value = "HEAD"
		)
	)
	private void cancelTick(CallbackInfo ci) {
		if (ticksThisFrame_bes > 0) {
			ticksThisFrame_bes--;
		} else if (!serverFrozen_bes) {
			if (!isGamePaused && world != null) {
				// keep packet handling going
				playerController.updateController();

				tickFixedSpeed_bes();
			}

			ci.cancel();
		}
	}

	@Inject(
		method = "runTick",
		at = @At(
			value = "TAIL"
		)
	)
	private void tickFixedSpeed(CallbackInfo ci) {
		if (!isGamePaused && !serverFrozen_bes && world != null) {
			tickFixedSpeed_bes();
		}
	}

	@Inject(
		method = "processKeyBinds",
		at = @At(
			value = "RETURN"
		)
	)
	private void processKeyBinds(CallbackInfo ci) {
		while(KeyBindings.TOGGLE_ANIMATION_MODE.isPressed()) {
			AnimationMode mode = BlockEventSeparatorMod.animationMode;
			int nextIndex = mode.index + 1;

			if (nextIndex >= AnimationMode.getCount()) {
				nextIndex = 0;
			}

			mode = AnimationMode.fromIndex(nextIndex);
			BlockEventSeparatorMod.animationMode = mode;

			ITextComponent text = new TextComponentString("Set animation mode to " + mode.name);
			player.sendStatusMessage(text, true);
		}
	}

	@Override
	public void setFrozen_bes(boolean frozen) {
		boolean wasFrozen = serverFrozen_bes;
		serverFrozen_bes = frozen;

		if (!wasFrozen && frozen) {
			TimerHelper.freezePartialTick = timer.renderPartialTicks;
			renderPartialTicksPaused = TimerHelper.adjustPartialTick(timer.renderPartialTicks);
		} else
		if (wasFrozen && !frozen) {
			timer.renderPartialTicks = TimerHelper.freezePartialTick;
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
			((IWorld)world).tickMovingBlocks_bes();
		}
	}
}
