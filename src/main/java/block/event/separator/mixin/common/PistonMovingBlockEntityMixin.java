package block.event.separator.mixin.common;

import org.spongepowered.asm.mixin.Mixin;

import block.event.separator.BlockEventCounters;
import block.event.separator.interfaces.mixin.IBlockEntity;

import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;

@Mixin(PistonMovingBlockEntity.class)
public abstract class PistonMovingBlockEntityMixin implements IBlockEntity {

	@Override
	public void onServerLevelSet() {
		BlockEventCounters.movingBlocksThisEvent++;
		BlockEventCounters.movingBlocksTotal++;
	}
}
