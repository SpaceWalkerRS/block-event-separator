package block.event.separator.mixin.client;

import java.util.LinkedList;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import block.event.separator.BlockEvent;
import block.event.separator.BlockEventCounters;
import block.event.separator.interfaces.mixin.IMinecraft;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Timer;
import net.minecraft.client.multiplayer.ClientLevel;

@Mixin(Minecraft.class)
public class MinecraftMixin implements IMinecraft {

	@Shadow private ClientLevel level;
	@Shadow private Timer timer;

	private boolean inFirstFrameOfTick_bes;
	private boolean doingBlockEvents_bes;

	private final LinkedList<BlockEvent> blockEvents_bes = new LinkedList<>();
	
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
		inFirstFrameOfTick_bes = ticksThisFrame > 0;

		if (level == null) {
			// Discard block events when switching
			// dimensions or leaving the game.
			blockEvents_bes.clear();
			doingBlockEvents_bes = false;

			return;
		}

		// If the frame rate is too low, no
		// block event separation can occur.
		if (ticksThisFrame > 1) {
			doAllBlockEvents_bes();
		}
	}

	@Inject(
		method = "runTick",
		slice = @Slice(
			from = @At(
				value = "INVOKE_STRING",
				target = "Lnet/minecraft/util/profiling/ProfilerFiller;push(Ljava/lang/String;)V",
				args = "ldc=tick"
			)
		),
		at = @At(
			value = "INVOKE",
			ordinal = 0,
			target = "Lnet/minecraft/util/profiling/ProfilerFiller;pop()V"
		)
	)
	private void postTick(boolean isRunning, CallbackInfo ci) {
		if (blockEvents_bes.isEmpty()) {
			return;
		}
		if (inFirstFrameOfTick_bes) {
			doOldBlockEvents_bes();
		}
		if (doingBlockEvents_bes) {
			doNextBlockEvents_bes();
		}
	}

	@Override
	public void queueBlockEvent_bes(BlockEvent be) {
		blockEvents_bes.offer(be);

		if (be.animationOffset > BlockEventCounters.maxOffset) {
			BlockEventCounters.maxOffset = be.animationOffset;
		}
	}

	private void doAllBlockEvents_bes() {
		while (!blockEvents_bes.isEmpty()) {
			doBlockEvent(blockEvents_bes.poll());
		}

		doingBlockEvents_bes = false;
	}

	private void doOldBlockEvents_bes() {
		while (!blockEvents_bes.isEmpty()) {
			BlockEvent blockEvent = blockEvents_bes.peek();
			int offset = blockEvent.animationOffset;

			if (offset == 0) {
				break;
			}

			blockEvents_bes.poll();
			doBlockEvent(blockEvent);
		}

		doingBlockEvents_bes = !blockEvents_bes.isEmpty();
	}

	private void doNextBlockEvents_bes() {
		// Compute the last offset that should be processed this frame.
		long range = BlockEventCounters.maxOffset + 1;
		long lastOffset = (long)(timer.partialTick * range);

		while (!blockEvents_bes.isEmpty()) {
			BlockEvent blockEvent = blockEvents_bes.peek();
			int offset = blockEvent.animationOffset;

			if (offset > lastOffset) {
				break;
			}

			blockEvents_bes.poll();
			doBlockEvent(blockEvent);
		}

		doingBlockEvents_bes = !blockEvents_bes.isEmpty();

		if (!doingBlockEvents_bes) {
			BlockEventCounters.currentOffset = -1;
			BlockEventCounters.maxOffset = -1;
		}
	}

	private void doBlockEvent(BlockEvent be) {
		BlockEventCounters.currentOffset = be.animationOffset;
		level.blockEvent(be.pos, be.block, be.type, be.data);
	}
}
