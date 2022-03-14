package block.event.separator.mixin.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import block.event.separator.BlockEventCounters;
import block.event.separator.interfaces.mixin.IPistonMovingBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(PistonMovingBlockEntity.class)
public class PistonMovingBlockEntityMixin extends BlockEntity implements IPistonMovingBlockEntity {

	@Shadow @Final private static int TICKS_TO_EXTEND;

	public PistonMovingBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	/**
	 * The animation offset. This could be the depth or the index of
	 * the block event that caused this block entity to be placed.
	 */
	private int animationOffset_bes = 0;
	/** The progress at which this block entity starts animating. */
	private float startProgress_bes = -1;

	@Inject(
		method = "<init>(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V",
		at = @At(
			value = "RETURN"
		)
	)
	private void onInit(BlockPos pos, BlockState state, CallbackInfo ci) {
		animationOffset_bes = BlockEventCounters.currentOffset;
	}

	@Inject(
		method = "getProgress",
		cancellable = true,
		at = @At(
			value = "RETURN"
		)
	)
	private void adjustProgress(float partialTick, CallbackInfoReturnable<Float> cir) {
		if (level == null || !level.isClientSide()) {
			return;
		}

		checkStartProgress_bes();

		if (startProgress_bes == 0.0F) {
			return;
		}

		float progress = cir.getReturnValue();
		progress = adjustProgress_bes(progress);

		cir.setReturnValue(progress);
	}

	@Inject(
		method = "tick",
		at = @At(
			value = "HEAD"
		)
	)
	private static void onTick(Level level, BlockPos pos, BlockState state, PistonMovingBlockEntity blockEntity, CallbackInfo ci) {
		if (level.isClientSide()) {
			((IPistonMovingBlockEntity)blockEntity).checkStartProgress_bes();
		}
	}

	@Override
	public void checkStartProgress_bes() {
		if (startProgress_bes < 0.0F) {
			float offset = animationOffset_bes;
			float maxOffset = BlockEventCounters.maxOffset;

			startProgress_bes = maxOffset == 0 ? 0.0F : offset / (maxOffset * TICKS_TO_EXTEND);
		}
	}

	private float adjustProgress_bes(float progress) {
		return progress > startProgress_bes ? (progress - startProgress_bes) / (1.0F - startProgress_bes) : 0.0F;
	}
}
