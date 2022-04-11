package block.event.separator.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import block.event.separator.interfaces.mixin.ITileEntity;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

@Mixin(TileEntity.class)
public class TileEntityMixin implements ITileEntity {

	@Shadow private World world;

	@Inject(
		method = "setWorld",
		at = @At(
			value = "TAIL"
		)
	)
	private void onWorldSet(World world, CallbackInfo ci) {
		if (this.world != null) {
			if (this.world.isRemote) {
				onClientWorldSet();
			} else {
				onServerWorldSet();
			}
		}
	}

	@Override
	public void onServerWorldSet() {

	}

	@Override
	public void onClientWorldSet() {

	}
}
