package block.event.separator;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Suppliers;

import net.fabricmc.api.ModInitializer;

public class BlockEventSeparatorMod implements ModInitializer {

	public static final String MOD_ID = "block-event-separator";
	public static final String MOD_NAME = "Block Event Separator";
	public static final String MOD_VERSION = "1.3.0";
	public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);

	private static final List<Runnable> SERVER_SEPARATION_MODE_LISTENERS = new LinkedList<>();
	private static final List<Runnable> SERVER_SEPARATION_INTERVAL_LISTENERS = new LinkedList<>();
	private static final List<Runnable> ANIMATION_MODE_LISTENERS = new LinkedList<>();

	/* Supplier for block event broadcast chunk distance */
	public static Supplier<Integer> blockEventDistanceSupplier = Suppliers.ofInstance(4);

	public static boolean isConnectedToBesServer;

	private static SeparationMode serverSeparationMode = SeparationMode.OFF;
	private static SeparationMode clientSeparationMode = SeparationMode.OFF;
	private static int serverSeparationInterval = 1;
	private static int clientSeparationInterval = 1;
	private static AnimationMode animationMode = AnimationMode.DEFAULT;

	private static Field process_entities;

	@Override
	public void onInitialize() {
		detectCarpet();
	}

	public static SeparationMode getServerSeparationMode() {
		return serverSeparationMode;
	}

	public static void setServerSeparationMode(SeparationMode mode) {
		if (mode != null && mode != serverSeparationMode) {
			serverSeparationMode = mode;
			SERVER_SEPARATION_MODE_LISTENERS.forEach(Runnable::run);
		}
	}

	public static void addServerSeparationModeListener(Runnable listener) {
		SERVER_SEPARATION_MODE_LISTENERS.add(listener);
	}

	public static SeparationMode getClientSeparationMode() {
		return clientSeparationMode;
	}

	public static void setClientSeparationMode(SeparationMode mode) {
		if (mode != null && mode != clientSeparationMode) {
			clientSeparationMode = mode;
		}
	}

	public static int getServerSeparationInterval() {
		return serverSeparationInterval;
	}

	public static void setServerSeparationInterval(int interval) {
		if (interval != serverSeparationInterval) {
			serverSeparationInterval = interval;
			SERVER_SEPARATION_INTERVAL_LISTENERS.forEach(Runnable::run);
		}
	}

	public static void addServerSeparationIntervalListener(Runnable listener) {
		SERVER_SEPARATION_INTERVAL_LISTENERS.add(listener);
	}

	public static int getClientSeparationInterval() {
		return clientSeparationInterval;
	}

	public static void setClientSeparationInterval(int interval) {
		if (interval != clientSeparationInterval) {
			clientSeparationInterval = interval;
		}
	}

	public static AnimationMode getAnimationMode() {
		return animationMode;
	}

	public static void setAnimationMode(AnimationMode mode) {
		if (mode != null && mode != animationMode) {
			animationMode = mode;
			ANIMATION_MODE_LISTENERS.forEach(Runnable::run);
		}
	}

	public static void addAnimationModeListener(Runnable listener) {
		ANIMATION_MODE_LISTENERS.add(listener);
	}

	private static void detectCarpet() {
		try {
			Class<?> TickSpeed = Class.forName("carpet.helpers.TickSpeed");
			process_entities = TickSpeed.getField("process_entities");
		} catch (ClassNotFoundException | NoSuchFieldException | SecurityException e) {

		}
	}

	public static boolean isFrozen() {
		if (process_entities != null) {
			try {
				return !process_entities.getBoolean(null);
			} catch (IllegalArgumentException | IllegalAccessException e) {

			}
		}

		return false;
	}
}
