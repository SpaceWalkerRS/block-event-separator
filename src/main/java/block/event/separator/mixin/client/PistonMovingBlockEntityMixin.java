package block.event.separator.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import block.event.separator.AnimationMode;
import block.event.separator.BlockEventSeparatorMod;
import block.event.separator.Counters;
import block.event.separator.TimerHelper;
import block.event.separator.interfaces.mixin.IBlockEntity;
import block.event.separator.interfaces.mixin.IPistonMovingBlockEntity;

import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;

@Mixin(
	value = PistonMovingBlockEntity.class,
	priority = 999
)
public abstract class PistonMovingBlockEntityMixin extends BlockEntity implements IBlockEntity, IPistonMovingBlockEntity {

	private static final int TICKS_TO_EXTEND = 2;

	@Shadow private float progress;
	@Shadow private float progressO;

	// progress for fixed speed animation
	private float progress_bes;
	private float progressO_bes;

	private int animationOffset_bes;
	/** The progress at which this block entity starts animating. */
	private float startProgress_bes;

	private long savedTicks_bes;

	private PistonMovingBlockEntityMixin(BlockEntityType<?> type) {
		super(type);
	}

	@Inject(
		method = "getProgress",
		cancellable = true,
		at = @At(
			value = "HEAD"
		)
	)
	private void adjustProgress(float partialTick, CallbackInfoReturnable<Float> cir) {
		if (level.isClientSide()) {
			float p = progress;

			if (BlockEventSeparatorMod.animationMode == AnimationMode.FIXED_SPEED) {
				p = progress_bes;

				if (Counters.frozen) {
					partialTick = TimerHelper.freezePartialTick;
				} else {
					partialTick = TimerHelper.savedPartialTick;
				}
			}

			// For block event separation to work, pistons have to start animating
			// right away, rather than after the first tick, like in Vanilla.
			p += (partialTick / TICKS_TO_EXTEND);
			p = Mth.clamp(p, 0.0F, 1.0F);

			if (startProgress_bes > 0.0F) {
				p = adjustProgress_bes(p);
			}

			cir.setReturnValue(p);
		}
	}

	@Override
	public void onClientLevelSet() {
		savedTicks_bes = Counters.ticks;

		switch (BlockEventSeparatorMod.clientSeparationMode) {
		case DEPTH:
			animationOffset_bes = Counters.subticks;
			break;
		case INDEX:
			animationOffset_bes = Counters.subticks;
			break;
		case BLOCK:
			animationOffset_bes = Counters.movingBlocks++ * BlockEventSeparatorMod.clientSeparationInterval;
			break;
		default:
			animationOffset_bes = 0;
		}
		int range = Counters.subticksTarget + 1;

		startProgress_bes = (float)animationOffset_bes / (range * TICKS_TO_EXTEND);
	}

	@Override
	public void animationTick_bes() {
		if (shouldUpdateAnimationProgress_bes()) {
			progressO_bes = progress_bes;
			progress_bes += 1.0F / TICKS_TO_EXTEND;
		}

		savedTicks_bes = Counters.ticks;
	}

	private float adjustProgress_bes(float p) {
		if (BlockEventSeparatorMod.animationMode == AnimationMode.FIXED_SPEED) {
			return (progress_bes == 0.0F && Counters.subticks < animationOffset_bes) ? 0.0F : p;
		} else {
			return p < startProgress_bes ? 0.0F : (p - startProgress_bes) / (1.0F - startProgress_bes);
		}
	}

	private boolean shouldUpdateAnimationProgress_bes() {
		if (progress_bes > 0.0F) {
			return progressO_bes < 1.0F;
		}
		if (Counters.ticks > savedTicks_bes) {
			return true;
		}

		return Counters.subticks > animationOffset_bes;
	}
}
