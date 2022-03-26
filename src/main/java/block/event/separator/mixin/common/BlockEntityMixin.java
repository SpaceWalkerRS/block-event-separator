package block.event.separator.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import block.event.separator.interfaces.mixin.IBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

@Mixin(BlockEntity.class)
public class BlockEntityMixin implements IBlockEntity {

	@Shadow private Level level;

	@Inject(
		method = "setLevelAndPosition",
		at = @At(
			value = "TAIL"
		)
	)
	private void onLevelSet(Level level, BlockPos pos, CallbackInfo ci) {
		if (this.level != null) {
			if (this.level.isClientSide()) {
				onClientLevelSet();
			} else {
				onServerLevelSet();
			}
		}
	}

	@Override
	public void onServerLevelSet() {

	}

	@Override
	public void onClientLevelSet() {

	}
}
