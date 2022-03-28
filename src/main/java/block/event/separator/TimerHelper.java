package block.event.separator;

import net.minecraft.client.Timer;

public class TimerHelper {

	private static Timer timer;

	public static float savedPartialTick = -1.0F;
	public static float freezePartialTick = 0.0F;

	public static void init(Timer timer) {
		TimerHelper.timer = timer;
	}

	public static float adjustPartialTick(float partialTick) {
		float subTicks = BlockEventCounters.subticks;
		float range = BlockEventCounters.subticksTarget + 1;

		return (subTicks + partialTick) / range;
	}

	public static void savePartialTick() {
		if (savedPartialTick < 0.0F) {
			savedPartialTick = timer.partialTick;
			timer.partialTick = adjustPartialTick(timer.partialTick);
		}
	}

	public static void loadPartialTick() {
		if (savedPartialTick >= 0.0F) {
			timer.partialTick = savedPartialTick;
			savedPartialTick = -1.0F;
		}
	}
}
