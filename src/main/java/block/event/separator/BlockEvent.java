package block.event.separator;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockEventData;
import net.minecraft.world.level.block.Block;

public class BlockEvent {

	public final BlockPos pos;
	public final Block block;
	public final int type;
	public final int data;
	public final int animationOffset;

	public BlockEvent(BlockPos pos, Block block, int type, int data, int animationOffset) {
		this.pos = pos;
		this.block = block;
		this.type = type;
		this.data = data;
		this.animationOffset = animationOffset;
	}

	public static BlockEvent of(BlockEventData data, int animationOffset) {
		return new BlockEvent(data.pos(), data.block(), data.paramA(), data.paramB(), animationOffset);
	}
}
