package block.event.separator.mixin.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import block.event.separator.BlockEventCounters;
import block.event.separator.BlockEventSeparator;
import block.event.separator.interfaces.mixin.IBlockEntity;

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
public abstract class PistonMovingBlockEntityMixin extends BlockEntity implements IBlockEntity {

	@Shadow @Final private static int TICKS_TO_EXTEND;

	@Shadow private float progress;

	/** The progress at which this block entity starts animating. */
	private float startProgress_bes;

	private PistonMovingBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
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
			p = Mth.clamp(p, 0.0F, 1.0F);

			if (startProgress_bes > 0.0F) {
				p = adjustProgress_bes(p);
			}

			cir.setReturnValue(p);
		}
	}

	@Override
	public void onClientLevelSet() {
		int offset = switch (BlockEventSeparator.clientSeparationMode) {
			case DEPTH -> BlockEventCounters.subticks;
			case INDEX -> BlockEventCounters.subticks;
			case BLOCK -> BlockEventCounters.movingBlocks++ * BlockEventSeparator.clientSeparationInterval;
			default    -> 0;
		};
		int range = BlockEventCounters.subticksTarget + 1;

		startProgress_bes = (float)offset / (range * TICKS_TO_EXTEND);
	}

	private float adjustProgress_bes(float p) {
		return p < startProgress_bes ? 0.0F : (p - startProgress_bes) / (1.0F - startProgress_bes);
	}
}
