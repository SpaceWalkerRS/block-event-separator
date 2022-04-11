package block.event.separator.mixin.client;

import java.util.Map;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import block.event.separator.KeyBindings;

import net.minecraft.client.settings.KeyBinding;

@Mixin(KeyBinding.class)
public class KeyBindingMixin {

	@Shadow @Final private static Map<String, Integer> CATEGORY_ORDER;

	@Inject(
		method = "<clinit>",
		at = @At(
			value = "RETURN"
		)
	)
	private static void initKeyBindingCategories(CallbackInfo ci) {
		int index = CATEGORY_ORDER.size();

		for (String category : KeyBindings.getCategories()) {
			CATEGORY_ORDER.put(category, ++index);
		}
	}
}
