package block.event.separator.mixin.client;

import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import block.event.separator.Counters;
import block.event.separator.interfaces.mixin.ITileEntityPiston;
import block.event.separator.interfaces.mixin.IWorld;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityPiston;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;

@Mixin(World.class)
public class WorldMixin implements IWorld {

	@Shadow @Final public List<TileEntity> tickableTileEntities;
	@Shadow private boolean isRemote;
	@Shadow private boolean processingLoadedTiles;

	@Shadow private boolean isBlockLoaded(BlockPos pos) { return false; }
	@Shadow private WorldBorder getWorldBorder() { return null; }

	@Inject(
		method = "updateEntities",
		cancellable = true,
		at = @At(
			value = "INVOKE_STRING",
			target = "Lnet/minecraft/profiler/Profiler;endStartSection(Ljava/lang/String;)V",
			args = "ldc=blockEntities"
		)
	)
	private void cancelTick(CallbackInfo ci) {
		if (isRemote && Counters.frozen) {
			ci.cancel();
		}
	}

	@Override
	public void tickMovingBlocks_bes() {
		processingLoadedTiles = true;

		for (int i = 0; i < tickableTileEntities.size(); i++) {
			TileEntity tileEntity = tickableTileEntities.get(i);

			if (tileEntity.isInvalid() || !tileEntity.hasWorld()) {
				continue;
			}
			if (!(tileEntity instanceof TileEntityPiston)) {
				continue;
			}

			BlockPos pos = tileEntity.getPos();

			if (!isBlockLoaded(pos) || !getWorldBorder().contains(pos)) {
				continue;
			}

			((ITileEntityPiston)tileEntity).animationTick_bes();
		}

		processingLoadedTiles = false;
	}
}
