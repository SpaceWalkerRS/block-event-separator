package block.event.separator;

public enum AnimationMode {

	DEFAULT(0, "default"),
	FIXED_SPEED(1, "fixed_speed");

	private static final AnimationMode[] ALL;

	static {

		AnimationMode[] modes = values();
		ALL = new AnimationMode[modes.length];

		for (AnimationMode mode : modes) {
			ALL[mode.index] = mode;
		}
	}

	public final int index;
	public final String name;

	private AnimationMode(int index, String name) {
		this.index = index;
		this.name = name;
	}

	public static AnimationMode fromIndex(int index) {
		if (index >= 0 && index < ALL.length) {
			return ALL[index];
		}

		return null;
	}

	public static int getCount() {
		return ALL.length;
	}
}
