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

import block.event.separator.AnimationMode;
import block.event.separator.BlockEventSeparator;
import block.event.separator.SeparationMode;

public class BlockEventSeparatorModule implements GSIModule {

	public static final String KEY_CATEGORY = "blockeventseparator";
	private static final GSSettingCategory SETTING_CATEGORY = new GSSettingCategory("blockeventseparator");

	private final GSIntegerSetting sSeparationMode;
	private final GSIntegerSetting cAnimationMode;

	public BlockEventSeparatorModule() {
		sSeparationMode = new GSIntegerSetting("separationMode", SeparationMode.OFF.index, 0, SeparationMode.getCount() - 1);
		cAnimationMode = new GSIntegerSetting("animationMode", AnimationMode.DEFAULT.index, 0, AnimationMode.getCount() - 1);
	}

	@Override
	public void init(GSIModuleManager manager) {
		manager.runOnClient(clientManager -> {
			GSTpsModule tpsModule = clientManager.getModule(GSTpsModule.class);
			// Enable fixed movement even when on default tps (since we are modifying the
			// partialTick value, it can be quite disturbing through longer periods).
			tpsModule.setFixedMovementOnDefaultTps(true);

			// Detect whether the animation mode setting changed
			clientManager.getSettingManager().addChangeListener(new GSISettingChangeListener() {

				@Override
				public void onSettingChanged(GSSettingCategory category, GSSetting<?> setting) {
					if (setting == cAnimationMode) {
						onAnimationModeSettingChanged();
					}
				}
			});

			// Make sure we use the correct initial value
			onAnimationModeSettingChanged();
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
			BlockEventSeparator.addServerSeparationModeListener(() -> {
				SeparationMode mode = BlockEventSeparator.getServerMode();

				if (mode.index != sSeparationMode.getValue()) {
					sSeparationMode.setValue(mode.index);
				}
			});

			// Make sure we use the correct initial value
			onSeparationModeSettingChanged();
		});
	}

	private void onSeparationModeSettingChanged() {
		SeparationMode mode = SeparationMode.fromIndex(sSeparationMode.getValue());

		if (mode != BlockEventSeparator.getServerMode()) {
			// Ensure that we do not get change listener loop.
			BlockEventSeparator.setServerSeparationMode(mode);
		}
	}

	private void onAnimationModeSettingChanged() {
		AnimationMode mode = AnimationMode.fromIndex(cAnimationMode.getValue());

		if (mode != BlockEventSeparator.getAnimationMode()) {
			// Ensure that we do not get change listener loop.
			BlockEventSeparator.setAnimationMode(mode);
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
		keyManager.registerKey("toggleAnimationMode", KEY_CATEGORY, GSKeyCode.UNKNOWN_KEY, () -> {
			int nextValue = cAnimationMode.getValue() + cAnimationMode.getInterval();

			if (nextValue > cAnimationMode.getMaxValue()) {
				nextValue = cAnimationMode.getMinValue();
			}

			cAnimationMode.setValue(nextValue);
		}, GSEKeyEventType.PRESS);
	}

	@Override
	public void registerServerSettings(GSSettingManager settings) {
		settings.registerSetting(SETTING_CATEGORY, sSeparationMode);
	}

	@Override
	public void registerClientSettings(GSSettingManager settings) {
		settings.registerSetting(SETTING_CATEGORY, cAnimationMode);
	}
}
