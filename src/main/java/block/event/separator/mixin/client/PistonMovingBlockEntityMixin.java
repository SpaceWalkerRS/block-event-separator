package block.event.separator.mixin.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import block.event.separator.AnimationMode;
import block.event.separator.BlockEventCounters;
import block.event.separator.BlockEventSeparator;
import block.event.separator.TimerHelper;
import block.event.separator.interfaces.mixin.IBlockEntity;
import block.event.separator.interfaces.mixin.IPistonMovingBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
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

	private int animationOffset_bes;
	/** The progress at which this block entity starts animating. */
	private float startProgress_bes;
	private boolean skipProgressAdjustment_bes;

	private PistonMovingBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Shadow private static void tick(Level level, BlockPos blockPos, BlockState blockState, PistonMovingBlockEntity pistonMovingBlockEntity) { }

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
			if (BlockEventSeparator.getAnimationMode() == AnimationMode.FIXED_SPEED) {
				partialTick = TimerHelper.savedPartialTick;
			}

			float p;

			try {
				skipProgressAdjustment_bes = true;
				p = getProgress(partialTick);
			} finally {
				skipProgressAdjustment_bes = false;
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
		animationOffset_bes = switch (BlockEventSeparator.getClientSeparationMode()) {
			case DEPTH -> BlockEventCounters.subticks;
			case INDEX -> BlockEventCounters.subticks;
			case BLOCK -> BlockEventCounters.movingBlocks++ * BlockEventSeparator.getClientSeparationInterval();
			default    -> 0;
		};
		int range = BlockEventCounters.subticksTarget + 1;

		startProgress_bes = (float)animationOffset_bes / (range * TICKS_TO_EXTEND);
	}

	@Override
	public void extraTick_bes() {
		if (shouldDoExtraTick_bes()) {
			tick(level, worldPosition, getBlockState(), (PistonMovingBlockEntity)(Object)this);
		}
	}

	private float adjustProgress_bes(float p) {
		if (BlockEventSeparator.getAnimationMode() == AnimationMode.FIXED_SPEED) {
			return (progress == 0.0F && BlockEventCounters.subticks < animationOffset_bes) ? 0.0F : p;
		} else {
			return p < startProgress_bes ? 0.0F : (p - startProgress_bes) / (1.0F - startProgress_bes);
		}
	}

	private boolean shouldDoExtraTick_bes() {
		return progress > 0.0F ? progressO < 1.0F : BlockEventCounters.subticks > animationOffset_bes;
	}
}
