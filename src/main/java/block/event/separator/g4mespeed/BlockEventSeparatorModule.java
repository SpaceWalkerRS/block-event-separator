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
import block.event.separator.BlockEventSeparatorMod;
import block.event.separator.SeparationMode;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

public class BlockEventSeparatorModule implements GSIModule {

	public static final String KEY_CATEGORY = "blockeventseparator";
	private static final GSSettingCategory SETTING_CATEGORY = new GSSettingCategory("blockeventseparator");

	private final GSIntegerSetting sSeparationMode;
	private final GSIntegerSetting sSeparationInterval;

	private final GSIntegerSetting cAnimationMode;

	public BlockEventSeparatorModule() {
		sSeparationMode = new GSIntegerSetting("separationMode", SeparationMode.OFF.index, 0, SeparationMode.getCount() - 1);
		sSeparationInterval = new GSIntegerSetting("separationInterval", 1, 1, 64);

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

			// Disable incompatible settings.
			tpsModule.sImmediateBlockBroadcast.setEnabledInGui(false);
			tpsModule.sImmediateBlockBroadcast.set(false);
			tpsModule.sParanoidMode.setEnabledInGui(false);
			tpsModule.sParanoidMode.set(false);

			// Register block event distance method
			BlockEventSeparatorMod.blockEventDistanceSupplier = tpsModule.sBlockEventDistance::get;

			// Detect whenever the separation mode setting changed
			serverManager.getSettingManager().addChangeListener(new GSISettingChangeListener() {

				@Override
				public void onSettingChanged(GSSettingCategory category, GSSetting<?> setting) {
					if (setting == sSeparationMode) {
						onSeparationModeSettingChanged();
					} else
					if (setting == sSeparationInterval) {
						onSeparationIntervalSettingChanged();
					}
				}
			});

			// Detect whenever the mode is changed via command
			BlockEventSeparatorMod.addServerSeparationModeListener(() -> {
				SeparationMode mode = BlockEventSeparatorMod.getServerSeparationMode();

				if (mode.index != sSeparationMode.get()) {
					sSeparationMode.set(mode.index);
				}
			});
			// Detect whenever the interval is changed via command
			BlockEventSeparatorMod.addServerSeparationIntervalListener(() -> {
				int interval = BlockEventSeparatorMod.getServerSeparationInterval();

				if (interval != sSeparationInterval.get()) {
					sSeparationInterval.set(interval);
				}
			});

			// Make sure we use the correct initial values
			onSeparationModeSettingChanged();
			onSeparationIntervalSettingChanged();
		});
	}

	private void onSeparationModeSettingChanged() {
		SeparationMode mode = SeparationMode.fromIndex(sSeparationMode.get());

		if (mode != BlockEventSeparatorMod.getServerSeparationMode()) {
			// Ensure that we do not get change listener loop.
			BlockEventSeparatorMod.setServerSeparationMode(mode);
		}
	}

	private void onSeparationIntervalSettingChanged() {
		int interval = sSeparationInterval.get();

		if (interval != BlockEventSeparatorMod.getServerSeparationInterval()) {
			// Ensure that we do not get change listener loop.
			BlockEventSeparatorMod.setServerSeparationInterval(interval);
		}
	}

	private void onAnimationModeSettingChanged() {
		AnimationMode mode = AnimationMode.fromIndex(cAnimationMode.get());

		if (mode != BlockEventSeparatorMod.getAnimationMode()) {
			// Ensure that we do not get change listener loop.
			BlockEventSeparatorMod.setAnimationMode(mode);
		}
	}

	@Override
	public void registerHotkeys(GSKeyManager keyManager) {
		keyManager.registerKey("toggleSeparationMode", KEY_CATEGORY, GSKeyCode.UNKNOWN_KEY, () -> {
			int nextValue = sSeparationMode.get() + sSeparationMode.getInterval();

			if (nextValue > sSeparationMode.getMin()) {
				nextValue = sSeparationMode.getMin();
			}

			sSeparationMode.set(nextValue);
		}, GSEKeyEventType.PRESS);
		keyManager.registerKey("toggleAnimationMode", KEY_CATEGORY, GSKeyCode.UNKNOWN_KEY, () -> {
			int nextValue = cAnimationMode.get() + cAnimationMode.getInterval();

			if (nextValue > cAnimationMode.getMax()) {
				nextValue = cAnimationMode.getMax();
			}

			cAnimationMode.set(nextValue);

			Minecraft minecraft = Minecraft.getInstance();
			LocalPlayer player = minecraft.player;

			if (player != null) {
				AnimationMode mode = BlockEventSeparatorMod.getAnimationMode();

				Component text = Component.literal("Set animation mode to " + mode.name);
				player.displayClientMessage(text, true);
			}
		}, GSEKeyEventType.PRESS);
	}

	@Override
	public void registerServerSettings(GSSettingManager settings) {
		settings.registerSetting(SETTING_CATEGORY, sSeparationMode);
		settings.registerSetting(SETTING_CATEGORY, sSeparationInterval);
	}

	@Override
	public void registerClientSettings(GSSettingManager settings) {
		settings.registerSetting(SETTING_CATEGORY, cAnimationMode);
	}
}
