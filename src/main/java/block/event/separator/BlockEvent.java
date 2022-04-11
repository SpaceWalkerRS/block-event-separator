package block.event.separator;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEventData;
import net.minecraft.util.math.BlockPos;

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
		return new BlockEvent(data.getPosition(), data.getBlock(), data.getEventID(), data.getEventParameter(), animationOffset);
	}
}
