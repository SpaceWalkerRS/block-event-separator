package block.event.separator.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.block.BlockPistonExtension;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.tileentity.TileEntityPistonRenderer;
import net.minecraft.tileentity.TileEntityPiston;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

@Mixin(
	value = TileEntityPistonRenderer.class,
	// Since this redirect is not required,
	// try to make sure it is applied last.
	priority = Integer.MAX_VALUE
)
public abstract class TileEntityPistonRendererMixin {

	@Shadow protected abstract boolean renderStateModel(BlockPos blockPos, IBlockState blockState, BufferBuilder bufferBuilder, World world, boolean bl);

	@Redirect(
		method = "render",
		require = -1,
		at = @At(
			value = "INVOKE",
			ordinal = 0,
			target = "Lnet/minecraft/tileentity/TileEntityPiston;getProgress(F)F"
		)
	)
	private float fixInvisibleMovingBlocks(TileEntityPiston blockEntity, float partialTick) {
		// Moving blocks do not render if the progress is 1.0F. To 'fix'
		// this we clamp the progress to 0.99F for the check.
		float progress = blockEntity.getProgress(partialTick);
		return MathHelper.clamp(progress, 0.0F, 0.99F);
	}

	@Redirect(
		method = "render",
		require = -1,
		at = @At(
			value = "INVOKE",
			ordinal = 0,
			target = "Lnet/minecraft/client/renderer/tileentity/TileEntityPistonRenderer;renderStateModel(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/client/renderer/BufferBuilder;Lnet/minecraft/world/World;Z)Z"
		)
	)
	private boolean fixShortPistonArm(TileEntityPistonRenderer renderer, BlockPos pos, IBlockState state, BufferBuilder bufferBuilder, World world, boolean bl, TileEntityPiston blockEntity, double d, double e, double f, float partialTick, int i, float j) {
		if (blockEntity.getProgress(partialTick) > 0.5F) {
			state = state.withProperty(BlockPistonExtension.SHORT, false);
		}

		return renderStateModel(pos, state, bufferBuilder, world, bl);
	}
}
