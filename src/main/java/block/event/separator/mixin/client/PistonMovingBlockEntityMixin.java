package block.event.separator.mixin.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import block.event.separator.BlockEventCounters;
import block.event.separator.interfaces.mixin.IBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(PistonMovingBlockEntity.class)
public class PistonMovingBlockEntityMixin extends BlockEntity implements IBlockEntity {

	@Shadow @Final private static int TICKS_TO_EXTEND;

	/** The progress at which this block entity starts animating. */
	private float startProgress_bes;

	private PistonMovingBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Inject(
		method = "<init>(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V",
		at = @At(
			value = "RETURN"
		)
	)
	private void onInit(BlockPos pos, BlockState state, CallbackInfo ci) {
		float offset = BlockEventCounters.currentOffset;
		float range = BlockEventCounters.maxOffset + 1;

		if (offset > 0 && range > 0) {
			startProgress_bes = offset / (range * TICKS_TO_EXTEND);
		}
	}

	@Inject(
		method = "getProgress",
		cancellable = true,
		at = @At(
			value = "RETURN"
		)
	)
	private void adjustProgress(float partialTick, CallbackInfoReturnable<Float> cir) {
		if (level.isClientSide() && startProgress_bes > 0.0F) {
			float progress = cir.getReturnValue();
			progress = adjustProgress_bes(progress);

			cir.setReturnValue(progress);
		}
	}

	@Override
	public void onLevelSet_bes() {
		if (level.isClientSide()) {
			BlockState state = getBlockState();
			PistonMovingBlockEntity blockEntity = (PistonMovingBlockEntity)(Object)this;

			// The block entity must be ticked upon placement
			// to make sure it starts animating right away.
			PistonMovingBlockEntity.tick(level, worldPosition, state, blockEntity);
		}
	}

	private float adjustProgress_bes(float progress) {
		return progress < startProgress_bes ? 0.0F : (progress - startProgress_bes) / (1.0F - startProgress_bes);
	}
}
