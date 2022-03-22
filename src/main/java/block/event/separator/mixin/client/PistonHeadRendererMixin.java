package block.event.separator.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.renderer.blockentity.PistonHeadRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;

@Mixin(
	value = PistonHeadRenderer.class,
	// Since this redirect is not required,
	// try to make sure it is applied last.
	priority = Integer.MAX_VALUE
)
public abstract class PistonHeadRendererMixin {

	@Redirect(
		method = "render",
		require = -1,
		at = @At(
			value = "INVOKE",
			ordinal = 0,
			target = "Lnet/minecraft/world/level/block/piston/PistonMovingBlockEntity;getProgress(F)F"
		)
	)
	private float fixInvisibleMovingBlocks(PistonMovingBlockEntity blockEntity, float partialTick) {
		// Moving blocks do not render if the progress is 1.0F. To 'fix'
		// this we clamp the progress to 0.99F for the check.
		float progress = blockEntity.getProgress(partialTick);
		return Mth.clamp(progress, 0.0F, 0.99F);
	}
}
