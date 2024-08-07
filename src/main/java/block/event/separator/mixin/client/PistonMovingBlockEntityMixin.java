package block.event.separator.mixin.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import block.event.separator.AnimationMode;
import block.event.separator.Counters;
import block.event.separator.BlockEventSeparatorMod;
import block.event.separator.TimerHelper;
import block.event.separator.interfaces.mixin.IBlockEntity;
import block.event.separator.interfaces.mixin.IPistonMovingBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(
	value = PistonMovingBlockEntity.class,
	priority = 999
)
public abstract class PistonMovingBlockEntityMixin extends BlockEntity implements IBlockEntity, IPistonMovingBlockEntity {

	@Shadow @Final private static int TICKS_TO_EXTEND;

	@Shadow private float progress;
	@Shadow private float progressO;

	// progress for fixed speed animation
	private float progress_bes;
	private float progressO_bes;
	// progress field used by G4mespeed
	private float gs_actualLastProgress;

	private int animationOffset_bes;
	/** The progress at which this block entity starts animating. */
	private float startProgress_bes;
	private boolean skipProgressAdjustment_bes;

	private long savedTicks_bes;

	private PistonMovingBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Shadow private float getProgress(float partialTick) { return 0.0F; }

	@Inject(
		method = "getProgress",
		cancellable = true,
		at = @At(
			value = "HEAD"
		)
	)
	private void adjustProgress(float partialTick, CallbackInfoReturnable<Float> cir) {
		if (level.isClientSide() && !skipProgressAdjustment_bes) {
			float savedProgress = progress;
			float savedProgressO = progressO;
			float savedGsActualLastProgress = gs_actualLastProgress;

			if (BlockEventSeparatorMod.getAnimationMode() == AnimationMode.FIXED_SPEED) {
				progress = progress_bes;
				progressO = progressO_bes;
				gs_actualLastProgress = progressO_bes;

				partialTick = TimerHelper.savedPartialTick;
			}

			float p;

			try {
				skipProgressAdjustment_bes = true;
				p = getProgress(partialTick);
			} finally {
				skipProgressAdjustment_bes = false;

				if (BlockEventSeparatorMod.getAnimationMode() == AnimationMode.FIXED_SPEED) {
					progress = savedProgress;
					progressO = savedProgressO;
					gs_actualLastProgress = savedGsActualLastProgress;
				}
			}

			if (startProgress_bes > 0.0F) {
				p = adjustProgress_bes(p);
			}

			cir.setReturnValue(p);
		}
	}

	@Inject(
		method = "getProgress",
		cancellable = true,
		at = @At(
			value = "HEAD",
			shift = Shift.AFTER
		)
	)
	private void getProgressNoG4mespeed(float partialTick, CallbackInfoReturnable<Float> cir) {
		if (level.isClientSide()) {
			// For block event separation to work, pistons have to start animating
			// right away, rather than after the first tick, like in Vanilla.
			// G4mespeed already makes this change, but if that mod is not intalled,
			// we have to make this change ourselves.
			float p = progress + (partialTick / TICKS_TO_EXTEND);
			p = Mth.clamp(p, 0.0F, 1.0F);

			cir.setReturnValue(p);
		}
	}

	@Override
	public void onClientLevelSet() {
		savedTicks_bes = Counters.ticks;

		animationOffset_bes = switch (BlockEventSeparatorMod.getClientSeparationMode()) {
			case DEPTH -> Counters.subticks;
			case INDEX -> Counters.subticks;
			case BLOCK -> Counters.movingBlocks++ * BlockEventSeparatorMod.getClientSeparationInterval();
			default    -> 0;
		};
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
		if (BlockEventSeparatorMod.getAnimationMode() == AnimationMode.FIXED_SPEED) {
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
