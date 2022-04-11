package block.event.separator;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mumfrey.liteloader.LiteMod;

import net.minecraft.client.Minecraft;

public class BlockEventSeparatorMod implements LiteMod {

	public static final String MOD_ID = "block-event-separator-lite";
	public static final String MOD_NAME = "Block Event Separator Lite";
	public static final String MOD_VERSION = "1.2.0";
	public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);

	public static final String PACKET_CHANNEL = "blockeventseparator";
	public static final int FREEZE_PACKET     = 0;
	public static final int NEXT_TICK_PACKET  = 1;

	public static SeparationMode serverSeparationMode = SeparationMode.OFF;
	public static SeparationMode clientSeparationMode = SeparationMode.OFF;
	public static int serverSeparationInterval = 1;
	public static int clientSeparationInterval = 1;
	public static AnimationMode animationMode = AnimationMode.DEFAULT;

	@Override
	public String getName() {
		return MOD_NAME;
	}

	@Override
	public String getVersion() {
		return MOD_VERSION;
	}

	@Override
	public void init(File configPath) {

	}

	@Override
	public void upgradeSettings(String version, File configPath, File oldConfigPath) {

	}

	public static File getConfigFolder(Minecraft minecraft) {
		return new File(minecraft.gameDir, "config/" + MOD_ID);
	}
}
