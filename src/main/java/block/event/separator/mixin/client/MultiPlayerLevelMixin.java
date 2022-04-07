package block.event.separator.mixin.client;

import java.util.function.BiFunction;

import org.spongepowered.asm.mixin.Mixin;

import block.event.separator.interfaces.mixin.IMultiPlayerLevel;
import block.event.separator.interfaces.mixin.IPistonMovingBlockEntity;

import net.minecraft.client.multiplayer.MultiPlayerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.dimension.Dimension;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.LevelData;

@Mixin(MultiPlayerLevel.class)
public abstract class MultiPlayerLevelMixin extends Level implements IMultiPlayerLevel {

	private MultiPlayerLevelMixin(LevelData data, DimensionType dimension, BiFunction<Level, Dimension, ChunkSource> chunkSource, ProfilerFiller profiler, boolean isClient) {
		super(data, dimension, chunkSource, profiler, isClient);
	}

	@Override
	public void tickMovingBlocks_bes() {
		updatingBlockEntities = true;

		for (int i = 0; i < tickableBlockEntities.size(); i++) {
			BlockEntity blockEntity = tickableBlockEntities.get(i);

			if (blockEntity.isRemoved() || !blockEntity.hasLevel()) {
				continue;
			}
			if (!(blockEntity instanceof PistonMovingBlockEntity)) {
				continue;
			}

			BlockPos pos = blockEntity.getBlockPos();

			if (!getChunkSource().isTickingChunk(pos) || !getWorldBorder().isWithinBounds(pos)) {
				continue;
			}
			if (!blockEntity.getType().isValid(getBlockState(pos).getBlock())) {
				continue;
			}

			((IPistonMovingBlockEntity)blockEntity).animationTick_bes();
		}

		updatingBlockEntities = false;
	}
}
