package block.event.separator.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import block.event.separator.BlockEventCounters;

import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;

@Mixin(PistonMovingBlockEntity.class)
public abstract class PistonMovingBlockEntityMixin extends BlockEntity {

	private static final int TICKS_TO_EXTEND = 2;

	@Shadow private float progress;

	/** The progress at which this block entity starts animating. */
	private float startProgress_bes;

	public PistonMovingBlockEntityMixin(BlockEntityType<?> type) {
		super(type);
	}

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
		if (level.isClientSide()) {
			// For block event separation to work, pistons have to start animating
			// right away, rather than after the first tick, like in Vanilla.
			float p = progress + (partialTick / TICKS_TO_EXTEND);

			if (startProgress_bes > 0.0F) {
				p = adjustProgress_bes(p);
			}

			// Clamped to 0.99F because moving blocks do not render if the progress
			// is greater than or equal to 1.0F.
			p = Mth.clamp(p, 0.0F, 0.99F); 

			cir.setReturnValue(p);
		}
	}

	private float adjustProgress_bes(float progress) {
		return progress < startProgress_bes ? 0.0F : (progress - startProgress_bes) / (1.0F - startProgress_bes);
	}
}
