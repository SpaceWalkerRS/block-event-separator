package block.event.separator.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.mojang.blaze3d.vertex.BufferBuilder;

import net.minecraft.client.renderer.blockentity.PistonHeadRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.piston.PistonHeadBlock;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(
	value = PistonHeadRenderer.class,
	// Since this redirect is not required,
	// try to make sure it is applied last.
	priority = Integer.MAX_VALUE
)
public abstract class PistonHeadRendererMixin {

	@Shadow protected abstract boolean renderBlock(BlockPos blockPos, BlockState blockState, BufferBuilder bufferBuilder, Level level, boolean bl);

	@Redirect(
		method = "render",
		require = -1,
		at = @At(
			value = "INVOKE",
			ordinal = 0,
			target = "Lnet/minecraft/client/renderer/blockentity/PistonHeadRenderer;renderBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lcom/mojang/blaze3d/vertex/BufferBuilder;Lnet/minecraft/world/level/Level;Z)Z"
		)
	)
	private boolean fixShortPistonArm(PistonHeadRenderer renderer, BlockPos pos, BlockState state, BufferBuilder bufferBuilder, Level level, boolean bl, PistonMovingBlockEntity blockEntity, double d, double e, double f, float partialTick, int i) {
		if (blockEntity.getProgress(partialTick) > 0.5F) {
			state = state.setValue(PistonHeadBlock.SHORT, false);
		}

		return renderBlock(pos, state, bufferBuilder, level, bl);
	}
}
