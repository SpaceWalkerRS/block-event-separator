package block.event.separator;

import net.minecraft.client.Timer;

public class TimerHelper {

	public static float savedPartialTick = -1.0F;

	public static float adjustPartialTick(float partialTick) {
		float subTicks = BlockEventCounters.subticks;
		float range = BlockEventCounters.subticksTarget + 1;

		return (subTicks + partialTick) / range;
	}

	public static void savePartialTick(Timer timer) {
		if (savedPartialTick < 0.0F) {
			savedPartialTick = timer.partialTick;
			timer.partialTick = adjustPartialTick(timer.partialTick);
		}
	}

	public static void loadPartialTick(Timer timer) {
		if (savedPartialTick >= 0.0F) {
			timer.partialTick = savedPartialTick;
			savedPartialTick = -1.0F;
		}
	}
}
