package block.event.separator.mixin.client;

import java.util.Map;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import block.event.separator.KeyMappings;

import net.minecraft.client.KeyMapping;

@Mixin(KeyMapping.class)
public class KeyMappingMixin {

	@Shadow @Final private static Map<String, Integer> CATEGORY_SORT_ORDER;

	@Inject(
		method = "<clinit>",
		at = @At(
			value = "RETURN"
		)
	)
	private static void registerCategories(CallbackInfo ci) {
		int index = CATEGORY_SORT_ORDER.size();

		for (String category : KeyMappings.getCategories()) {
			CATEGORY_SORT_ORDER.put(category, ++index);
		}
	}
}
