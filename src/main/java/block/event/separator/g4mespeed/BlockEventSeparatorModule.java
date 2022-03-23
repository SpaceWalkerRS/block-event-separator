package block.event.separator.g4mespeed;

import com.g4mesoft.core.GSIModule;
import com.g4mesoft.core.GSIModuleManager;
import com.g4mesoft.hotkey.GSEKeyEventType;
import com.g4mesoft.hotkey.GSKeyCode;
import com.g4mesoft.hotkey.GSKeyManager;
import com.g4mesoft.module.tps.GSTpsModule;
import com.g4mesoft.setting.GSISettingChangeListener;
import com.g4mesoft.setting.GSSetting;
import com.g4mesoft.setting.GSSettingCategory;
import com.g4mesoft.setting.GSSettingManager;
import com.g4mesoft.setting.types.GSIntegerSetting;

import block.event.separator.BlockEventSeparator;
import block.event.separator.BlockEventSeparator.Mode;

public class BlockEventSeparatorModule implements GSIModule {

	public static final String KEY_CATEGORY = "blockeventseparator";
	private static final GSSettingCategory SETTING_CATEGORY = new GSSettingCategory("blockeventseparator");
	
	private final GSIntegerSetting sSeparationMode;
	
	public BlockEventSeparatorModule() {
		sSeparationMode = new GSIntegerSetting("separationMode", Mode.OFF.index, 0, Mode.getCount() - 1);
	}
	
	@Override
	public void init(GSIModuleManager manager) {
		manager.runOnClient(clientManager -> {
			GSTpsModule tpsModule = clientManager.getModule(GSTpsModule.class);
			// Enable fixed movement even when on default tps (since we are modifying the
			// partialTick value, it can be quite disturbing through longer periods).
			tpsModule.setFixedMovementOnDefaultTps(true);
		});
		
		manager.runOnServer(serverManager -> {
			GSTpsModule tpsModule = serverManager.getModule(GSTpsModule.class);
			
			// Disable immediate block broadcast, since it is not compatible.
			tpsModule.sImmediateBlockBroadcast.setEnabledInGui(false);
			tpsModule.sImmediateBlockBroadcast.setValue(false);

			// Register block event distance method
			BlockEventSeparator.blockEventDistanceSupplier = tpsModule.sBlockEventDistance::getValue;

			// Detect whenever the separation mode setting changed
			serverManager.getSettingManager().addChangeListener(new GSISettingChangeListener() {
				@Override
				public void onSettingChanged(GSSettingCategory category, GSSetting<?> setting) {
					if (setting == sSeparationMode) {
						onSeparationModeSettingChanged();
					}
				}
			});
			
			// Detect whenever the mode is changed via command
			BlockEventSeparator.addServerModeListener(() -> {
				Mode mode = BlockEventSeparator.getServerMode();
				if (mode.index != sSeparationMode.getValue()) {
					sSeparationMode.setValue(mode.index);
				}
			});
			
			// Make sure we use the correct initial value
			onSeparationModeSettingChanged();
		});
	}

	private void onSeparationModeSettingChanged() {
		Mode mode = Mode.fromIndex(sSeparationMode.getValue());
		if (mode != BlockEventSeparator.getServerMode()) {
			// Ensure that we do not get change listener loop.
			BlockEventSeparator.setServerMode(mode);
		}
	}

	@Override
	public void registerHotkeys(GSKeyManager keyManager) {
		keyManager.registerKey("toggleSeparationMode", KEY_CATEGORY, GSKeyCode.UNKNOWN_KEY, () -> {
			int nextValue = sSeparationMode.getValue() + sSeparationMode.getInterval();
			if (nextValue > sSeparationMode.getMaxValue()) {
				nextValue = sSeparationMode.getMinValue();
			}
			sSeparationMode.setValue(nextValue);
		}, GSEKeyEventType.PRESS);
	}
	
	@Override
	public void registerServerSettings(GSSettingManager settings) {
		settings.registerSetting(SETTING_CATEGORY, sSeparationMode);
	}
}
