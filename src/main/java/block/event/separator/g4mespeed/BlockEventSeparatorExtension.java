package block.event.separator.g4mespeed;

import com.g4mesoft.GSExtensionInfo;
import com.g4mesoft.GSExtensionUID;
import com.g4mesoft.GSIExtension;
import com.g4mesoft.core.GSVersion;
import com.g4mesoft.core.client.GSClientController;
import com.g4mesoft.core.server.GSServerController;
import com.g4mesoft.packet.GSIPacket;
import com.g4mesoft.registry.GSSupplierRegistry;

import block.event.separator.BlockEventSeparatorMod;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public class BlockEventSeparatorExtension implements GSIExtension {

	public static final String NAME = BlockEventSeparatorMod.MOD_NAME;
	/* "BESM" in ASCII as HEX */
	public static final GSExtensionUID UID = new GSExtensionUID(0x4245534D);
	public static final GSVersion VERSION = new GSVersion(BlockEventSeparatorMod.MOD_VERSION);
	
	public static final GSExtensionInfo INFO = new GSExtensionInfo(NAME, UID, VERSION);
	
	private static final String TRANSLATION_PATH = "/assets/block/event/separator/lang/en.lang";
	
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
}
