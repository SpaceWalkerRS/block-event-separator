package block.event.separator.mixin.client;

import java.io.File;
import java.util.Collection;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import block.event.separator.BlockEventSeparatorMod;
import block.event.separator.KeyBindings;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;

@Mixin(GameSettings.class)
public class GameSettingsMixin {

	@Shadow private Minecraft mc;
	@Shadow private KeyBinding[] keyBindings;

	private boolean initialized;

	@Inject(
		method = "loadOptions",
		at = @At(
			value = "HEAD"
		)
	)
	private void loadOptions(CallbackInfo ci) {
		if (initialized) {
			return;
		}

		Collection<KeyBinding> besKeys = KeyBindings.getKeyBindings();

		KeyBinding[] mcKeys = keyBindings;
		keyBindings = new KeyBinding[mcKeys.length + besKeys.size()];

		int index = 0;
		for (int i = 0; i < mcKeys.length; ) {
			keyBindings[index++] = mcKeys[i++];
		}
		for (KeyBinding key : besKeys) {
			keyBindings[index++] = key;
		}

		initialized = true;
	}

	@Inject(
		method = "loadOptions",
		at = @At(
			value = "RETURN"
		)
	)
	private void loadKeyBindings(CallbackInfo ci) {
		File folder = BlockEventSeparatorMod.getConfigFolder(mc);
		KeyBindings.load(folder);
	}

	@Inject(
		method = "saveOptions",
		at = @At(
			value = "HEAD"
		)
	)
	private void saveKeyBindings(CallbackInfo ci) {
		File folder = BlockEventSeparatorMod.getConfigFolder(mc);
		KeyBindings.save(folder);
	}
}
