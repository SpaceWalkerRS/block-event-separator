package block.event.separator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Suppliers;

public class BlockEventSeparator {

	public static final String MOD_ID = "block-event-separator";
	public static final String MOD_NAME = "Block Event Separator";
	public static final String MOD_VERSION = "1.0.0";
	public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);

	/* Supplier for block event broadcast chunk distance */
	public static Supplier<Integer> blockEventDistanceSupplier = Suppliers.ofInstance(4);

	private static Mode mode = Mode.OFF;
	private static final List<Runnable> modeListeners = new ArrayList<>();
	
	public static Mode getMode() {
		return mode;
	}

	public static void setMode(Mode newMode) {
		if (newMode != null && newMode != mode) {
			mode = newMode;
			modeListeners.forEach(Runnable::run);
		}
	}
	
	public static void addModeChangeListener(Runnable listener) {
		modeListeners.add(listener);
	}
	
	public static enum Mode {

		OFF(0, "off", ""),
		DEPTH(1, "depth", "Block events are separated by depth (colloquially known as \"microticks\" or \"BED\"). Block events at the same depth start animating simultaneously. Depths are separated by 1gt worth of time."),
		INDEX(2, "index", "Block events are separated by index, based on the order in which they were executed. They are separated by 1gt worth of time.");

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

		public static Mode fromName(String name) {
			return BY_NAME.get(name);
		}

		public static Mode fromIndex(int index) {
			if (index >= 0 && index < ALL.length) {
				return ALL[index];
			}
			
			return null;
		}
		
		public static int getCount() {
			return ALL.length;
		}
	}
}
