package block.event.separator.mixin.common;

import org.spongepowered.asm.mixin.Mixin;

import block.event.separator.Counters;
import block.event.separator.interfaces.mixin.ITileEntity;

import net.minecraft.tileentity.TileEntityPiston;

@Mixin(TileEntityPiston.class)
public abstract class TileEntityPistonMixin implements ITileEntity {

	@Override
	public void onServerWorldSet() {
		Counters.movingBlocksThisEvent++;
		Counters.movingBlocksTotal++;
	}
}
