package block.event.separator.g4mespeed;

import com.g4mesoft.GSExtensionInfo;
import com.g4mesoft.GSExtensionUID;
import com.g4mesoft.GSIExtension;
import com.g4mesoft.core.GSVersion;
import com.g4mesoft.core.client.GSClientController;
import com.g4mesoft.core.server.GSServerController;
import com.g4mesoft.packet.GSIPacket;
import com.g4mesoft.registry.GSSupplierRegistry;

import block.event.separator.BlockEventSeparator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public class BlockEventSeparatorExtension implements GSIExtension {

	public static final String NAME = "Block Event Separator";
	/* "BESM" in ASCII as HEX */
	public static final GSExtensionUID UID = new GSExtensionUID(0x4245534D);
	public static final GSVersion VERSION = decodeVersionString(BlockEventSeparator.MOD_VERSION);
	
	public static final GSExtensionInfo INFO = new GSExtensionInfo(NAME, UID, VERSION);
	
	private static final String TRANSLATION_PATH = "/assets/blockeventseparator/lang/en.lang";
	
	@Environment(EnvType.CLIENT)
	private BlockEventSeparatorModule clientModule;
	private BlockEventSeparatorModule serverModule;
	
	@Override
	public void init() {
	}
	
	@Override
	public void registerPackets(GSSupplierRegistry<Integer, GSIPacket> registry) {
	}
	
	@Override
	public void addClientModules(GSClientController controller) {
		if (clientModule == null) {
			clientModule = new BlockEventSeparatorModule();
		}
		controller.addModule(clientModule);
	}

	@Override
	public void addServerModules(GSServerController controller) {
		if (serverModule == null) {
			serverModule = new BlockEventSeparatorModule();
		}
		controller.addModule(serverModule);
	}

	@Override
	public String getTranslationPath() {
		return TRANSLATION_PATH;
	}
	
	@Override
	public GSExtensionInfo getInfo() {
		return INFO;
	}
	
	private static GSVersion decodeVersionString(String modVersion) {
		int majorVersion = 1, minorVersion = 0, patchVersion = 0;
		
		String[] args = BlockEventSeparator.MOD_VERSION.split("\\.");
		if (args.length == 3) {
			try {
				majorVersion = Integer.parseInt(args[0]);
				minorVersion = Integer.parseInt(args[1]);
				patchVersion = Integer.parseInt(args[2]);
			} catch (NumberFormatException e) {
			}
		}
		
		return new GSVersion(majorVersion, minorVersion, patchVersion);
	}
}
