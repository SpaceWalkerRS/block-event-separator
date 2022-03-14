package block.event.separator;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ModInitializer;

public class BlockEventSeparator implements ModInitializer {

	public static final String MOD_ID = "block-event-separator";
	public static final String MOD_NAME = "Block Event Separator";
	public static final String MOD_VERSION = "1.0.0";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

	public static Mode mode = Mode.OFF;

	@Override
	public void onInitialize() {

	}

	public static enum Mode {

		OFF(0, "off", ""),
		DEPTH(1, "depth", "Block events are separated based on their depth (colloquially known as \"microticks\" or \"BED\"). Block events at the same depth start animating at the same time."),
		TOTAL(2, "total", "Block events are separated based on the order in which they were executed. Each block event starts animating after the previous.");

		private static final Mode[] ALL;
		private static final Map<String, Mode> BY_NAME;

		static {

			Mode[] modes = values();

			ALL = new Mode[modes.length];
			BY_NAME = new HashMap<>();

			for (Mode mode : modes) {
				ALL[mode.index] = mode;
				BY_NAME.put(mode.name, mode);
			}
		}

		public final int index;
		public final String name;
		public final String description;

		private Mode(int index, String name, String description) {
			this.index = index;
			this.name = name;
			this.description = description;
		}

		public static Mode fromIndex(int index) {
			if (index >= 0 && index < ALL.length) {
				return ALL[index];
			}

			return null;
		}

		public static Mode fromName(String name) {
			return BY_NAME.get(name);
		}
	}
}
