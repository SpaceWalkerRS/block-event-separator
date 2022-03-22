package block.event.separator.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import block.event.separator.BlockEventCounters;

import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;

@Mixin(
	value = PistonMovingBlockEntity.class,
	priority = 999
)
public abstract class PistonMovingBlockEntityMixin extends BlockEntity {

	private static final int TICKS_TO_EXTEND = 2;

	@Shadow private float progress;

	/** The progress at which this block entity starts animating. */
	private float startProgress_bes;
	private boolean skipProgressAdjustment_bes;

	public PistonMovingBlockEntityMixin(BlockEntityType<?> type) {
		super(type);
	}

	@Shadow protected abstract float getProgress(float partialTick);

	@Inject(
		method = "<init>()V",
		at = @At(
			value = "RETURN"
		)
	)
	private void onInit(CallbackInfo ci) {
		float offset = BlockEventCounters.subticks;
		float range = BlockEventCounters.subticksTarget + 1;

		if (offset > 0 && range > 0) {
			startProgress_bes = offset / (range * TICKS_TO_EXTEND);
		}
	}

	@Inject(
		method = "getProgress",
		cancellable = true,
		at = @At(
			value = "HEAD"
		)
	)
	private void adjustProgress(float partialTick, CallbackInfoReturnable<Float> cir) {
		if (level.isClientSide() && !skipProgressAdjustment_bes) {
			float progress;

			try {
				skipProgressAdjustment_bes = true;
				progress = getProgress(partialTick);
			} finally {
				skipProgressAdjustment_bes = false;
			}

			if (startProgress_bes > 0.0F) {
				progress = adjustProgress_bes(progress);
			}

			cir.setReturnValue(progress);
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

	private float adjustProgress_bes(float progress) {
		return progress < startProgress_bes ? 0.0F : (progress - startProgress_bes) / (1.0F - startProgress_bes);
	}
}
