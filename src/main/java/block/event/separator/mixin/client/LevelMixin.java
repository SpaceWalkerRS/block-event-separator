package block.event.separator.mixin.client;

import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import block.event.separator.Counters;
import block.event.separator.interfaces.mixin.ILevel;
import block.event.separator.interfaces.mixin.IPistonMovingBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;

@Mixin(Level.class)
public class LevelMixin implements ILevel {

	@Shadow @Final private List<TickingBlockEntity> blockEntityTickers;
	@Shadow private boolean tickingBlockEntities;

	@Shadow private boolean isClientSide() { return false; }
	@Shadow private BlockEntity getBlockEntity(BlockPos pos) { return null; }

	@Inject(
		method = "tickBlockEntities",
		cancellable = true,
		at = @At(
			value = "HEAD"
		)
	)
	private void cancelTick(CallbackInfo ci) {
		if (isClientSide() && Counters.frozen) {
			ci.cancel();
		}
	}

	@Override
	public void tickMovingBlocks_bes() {
		tickingBlockEntities = true;

		for (int i = 0; i < blockEntityTickers.size(); i++) {
			TickingBlockEntity ticker = blockEntityTickers.get(i);

			BlockPos pos = ticker.getPos();
			BlockEntity blockEntity = getBlockEntity(pos);

			if (blockEntity == null || blockEntity.isRemoved()) {
				continue;
			}

			if (blockEntity instanceof PistonMovingBlockEntity) {
				((IPistonMovingBlockEntity)blockEntity).animationTick_bes();
			}
		}

		tickingBlockEntities = false;
	}
}
