package block.event.separator.utils;

public class MathUtils {

	public static long max(long l0, long l1, long l2) {
		long l = l0 > l1 ? l0 : l1;
		return l > l2 ? l : l2;
	}

	public static float min(float f0, float f1, float f2) {
		float f = f0 < f1 ? f0 : f1;
		return f < f2 ? f : f2;
	}
}
