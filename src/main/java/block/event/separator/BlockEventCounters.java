package block.event.separator;

public class BlockEventCounters {

	// client-side
	public static boolean frozen;
	public static int subticks;
	public static int subticksTarget;
	public static int movingBlocks;

	// server-side
	public static int currentDepth;
	public static int currentBatch;
	public static int total;
	public static int movingBlocksThisEvent;
	public static int movingBlocksTotal;

}
