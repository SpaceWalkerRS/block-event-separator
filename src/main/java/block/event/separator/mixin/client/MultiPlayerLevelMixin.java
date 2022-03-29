package block.event.separator.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import block.event.separator.BlockEventCounters;

import net.minecraft.client.multiplayer.MultiPlayerLevel;

@Mixin(MultiPlayerLevel.class)
public abstract class MultiPlayerLevelMixin {

	@Inject(
		method = "tickEntities",
		cancellable = true,
		at = @At(
			value = "HEAD"
		)
	)
	private void cancelTick(CallbackInfo ci) {
		if (BlockEventCounters.frozen) {
			ci.cancel();
		}
	}
}
