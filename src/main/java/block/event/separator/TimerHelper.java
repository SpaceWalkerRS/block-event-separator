package block.event.separator;

public class TimerHelper {

	public static float savedPartialTick = -1.0F;

	public static float adjustPartialTick(float partialTick) {
		savedPartialTick = partialTick;

		float subticks = Counters.subticks;
		float range = Counters.subticksTarget + 1;

		return (subticks + partialTick) / range;
	}
}
