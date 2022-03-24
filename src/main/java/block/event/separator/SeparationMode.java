package block.event.separator;

import java.util.HashMap;
import java.util.Map;

public enum SeparationMode {

	OFF(0, "off", ""),
	DEPTH(1, "depth", "Block events are separated by depth (colloquially known as \"microticks\" or \"BED\"). Block events at the same depth start animating simultaneously. Depths are separated by 1gt worth of time."),
	INDEX(2, "index", "Block events are separated by index, based on the order in which they were executed. They are separated by 1gt worth of time."),
	BLOCK(3, "block", "Moving blocks are separated by index, based on the order in which they were created. They are separated by 1gt worth of time.");

	private static final SeparationMode[] ALL;
	private static final Map<String, SeparationMode> BY_NAME;

	static {

		SeparationMode[] modes = values();

		ALL = new SeparationMode[modes.length];
		BY_NAME = new HashMap<>();

		for (SeparationMode mode : modes) {
			ALL[mode.index] = mode;
			BY_NAME.put(mode.name, mode);
		}
	}

	public final int index;
	public final String name;
	public final String description;

	private SeparationMode(int index, String name, String description) {
		this.index = index;
		this.name = name;
		this.description = description;
	}

	public static SeparationMode fromName(String name) {
		return BY_NAME.get(name);
	}

	public static SeparationMode fromIndex(int index) {
		if (index >= 0 && index < ALL.length) {
			return ALL[index];
		}

		return null;
	}

	public static int getCount() {
		return ALL.length;
	}
}
