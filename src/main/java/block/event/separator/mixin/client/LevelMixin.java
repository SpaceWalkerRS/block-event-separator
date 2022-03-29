package block.event.separator.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.j2objc.annotations.ReflectionSupport.Level;

import block.event.separator.BlockEventCounters;

@Mixin(Level.class)
public class LevelMixin {

	@Shadow private boolean isClientSide() { return false; }

	@Inject(
		method = "tickBlockEntities",
		cancellable = true,
		at = @At(
			value = "HEAD"
		)
	)
	private void cancelTick(CallbackInfo ci) {
		if (isClientSide() && BlockEventCounters.frozen) {
			ci.cancel();
		}
	}
}
