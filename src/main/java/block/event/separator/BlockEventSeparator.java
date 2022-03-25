package block.event.separator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BlockEventSeparator {

	public static final String MOD_ID = "block-event-separator";
	public static final String MOD_NAME = "Block Event Separator";
	public static final String MOD_VERSION = "1.1.0";
	public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);

	public static SeparationMode serverSeparationMode = SeparationMode.OFF;
	public static SeparationMode clientSeparationMode = SeparationMode.OFF;
	public static int serverSeparationInterval = 1;
	public static int clientSeparationInterval = 1;

}
