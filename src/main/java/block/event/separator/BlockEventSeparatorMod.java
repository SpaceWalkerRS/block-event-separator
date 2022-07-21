package block.event.separator;

import java.io.File;
import java.lang.reflect.Field;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ModInitializer;
import net.minecraft.client.Minecraft;

public class BlockEventSeparatorMod implements ModInitializer {

	public static final String MOD_ID = "block-event-separator";
	public static final String MOD_NAME = "Block Event Separator";
	public static final String MOD_VERSION = "1.3.0";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

	public static boolean isConnectedToBesServer;

	public static SeparationMode serverSeparationMode = SeparationMode.OFF;
	public static SeparationMode clientSeparationMode = SeparationMode.OFF;
	public static int serverSeparationInterval = 1;
	public static int clientSeparationInterval = 1;
	public static AnimationMode animationMode = AnimationMode.DEFAULT;

	private static Field process_entities;

	@Override
	public void onInitialize() {
		detectCarpet();
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

	public static File getConfigFolder(Minecraft minecraft) {
		return new File(minecraft.gameDirectory, "config/" + MOD_ID);
	}
}
