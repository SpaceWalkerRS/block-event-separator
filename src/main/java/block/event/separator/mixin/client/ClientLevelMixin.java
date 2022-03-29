package block.event.separator.mixin.client;

import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Mixin;

import block.event.separator.interfaces.mixin.IClientLevel;
import block.event.separator.interfaces.mixin.IPistonMovingBlockEntity;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;

@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin extends Level implements IClientLevel {

	private ClientLevelMixin(WritableLevelData data, ResourceKey<Level> dimension, Holder<DimensionType> holder, Supplier<ProfilerFiller> supplier, boolean isClient, boolean isDebug, long seed) {
		super(data, dimension, holder, supplier, isClient, isDebug, seed);
	}

	@Override
	public void tickMovingBlocks_bes() {
		for (int i = 0; i < blockEntityTickers.size(); i++) {
			TickingBlockEntity ticker = blockEntityTickers.get(i);

			BlockPos pos = ticker.getPos();
			long l = ChunkPos.asLong(pos);

			if (!shouldTickBlocksAt(l)) {
				continue;
			}

			BlockEntity blockEntity = getBlockEntity(pos);

			if (blockEntity == null || blockEntity.isRemoved()) {
				continue;
			}

			if (blockEntity instanceof PistonMovingBlockEntity) {
				((IPistonMovingBlockEntity)blockEntity).extraTick_bes();
			}
		}
	}
}
