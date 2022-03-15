package block.event.separator.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import block.event.separator.interfaces.mixin.IBlockableEventLoop;

import net.minecraft.util.thread.BlockableEventLoop;

@Mixin(BlockableEventLoop.class)
public class BlockableEventLoopMixin implements IBlockableEventLoop {

	@Inject(
		method = "runAllTasks",
		cancellable = true,
		at = @At(
			value = "HEAD"
		)
	)
	private void trySkipTasks(CallbackInfo ci) {
		if (shouldSkipTasks_bes()) {
			ci.cancel();
		}
	}

	@Override
	public boolean shouldSkipTasks_bes() {
		return false;
	}
}
