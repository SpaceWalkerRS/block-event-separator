package block.event.separator;

public class Counters {

	// client-side
	public static long ticks;
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
