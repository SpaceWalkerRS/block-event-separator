package block.event.separator.mixin.client;

import java.io.File;
import java.util.Collection;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import block.event.separator.BlockEventSeparatorMod;
import block.event.separator.KeyMappings;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;

@Mixin(Options.class)
public class OptionsMixin {

	@Shadow @Final @Mutable private KeyMapping[] keyMappings;
	@Shadow private Minecraft minecraft;

	private boolean initialized_bes;

	@Inject(
		method = "load",
		at = @At(
			value = "HEAD"
		)
	)
	private void registerKeyMappings(CallbackInfo ci) {
		if (initialized_bes) {
			return;
		}

		Collection<KeyMapping> besMappings = KeyMappings.getKeyMappings();

		KeyMapping[] mcMappings = keyMappings;
		keyMappings = new KeyMapping[mcMappings.length + besMappings.size()];

		int index = 0;
		for (int i = 0; i < mcMappings.length; i++) {
			keyMappings[index++] = mcMappings[i];
		}
		for (KeyMapping mapping : besMappings) {
			keyMappings[index++] = mapping;
		}

		initialized_bes = true;
	}

	@Inject(
		method = "load",
		at = @At(
			value = "RETURN"
		)
	)
	private void loadKeyMappings(CallbackInfo ci) {
		File folder = BlockEventSeparatorMod.getConfigFolder(minecraft);
		KeyMappings.load(folder);
	}

	@Inject(
		method = "save",
		at = @At(
			value = "HEAD"
		)
	)
	private void saveKeyMappings(CallbackInfo ci) {
		File folder = BlockEventSeparatorMod.getConfigFolder(minecraft);
		KeyMappings.save(folder);
	}
}
