package block.event.separator.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import block.event.separator.interfaces.mixin.IBlockEntity;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

@Mixin(BlockEntity.class)
public class BlockEntityMixin implements IBlockEntity {

	@Inject(
		method = "setLevel",
		at = @At(
			value = "RETURN"
		)
	)
	private void onSetLevel(Level level, CallbackInfo ci) {
		onLevelSet_bes();
	}

	@Override
	public void onLevelSet_bes() {

	}
}
