package block.event.separator.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import block.event.separator.BlockEventCounters;
import block.event.separator.interfaces.mixin.IMinecraftServer;

import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockEventData;

@Mixin(ServerLevel.class)
public class ServerLevelMixin {

	@Shadow private MinecraftServer server;
	@Shadow private ObjectLinkedOpenHashSet<BlockEventData> blockEvents;

	@Inject(
		method = "runBlockEvents",
		at = @At(
			value = "HEAD"
		)
	)
	private void preBlockEvents(CallbackInfo ci) {
		BlockEventCounters.currentDepth = 0;
		BlockEventCounters.currentBatch = blockEvents.size();
		BlockEventCounters.total = 0;
	}

	@Inject(
		method = "runBlockEvents",
		at = @At(
			value = "INVOKE",
			shift = Shift.BEFORE,
			target = "Lit/unimi/dsi/fastutil/objects/ObjectLinkedOpenHashSet;removeFirst()Ljava/lang/Object;"
		)
	)
	private void onNextBlockEvent(CallbackInfo ci) {
		if (BlockEventCounters.currentBatch == 0) {
			BlockEventCounters.currentDepth++;
			BlockEventCounters.currentBatch = blockEvents.size();
		}

		BlockEventCounters.currentBatch--;
	}

	@Inject(
		method = "runBlockEvents",
		at = @At(
			value = "RETURN"
		)
	)
	private void postBlockEvents(CallbackInfo ci) {
		((IMinecraftServer)server).postBlockEvents_bes();
	}

	@Inject(
		method = "doBlockEvent",
		at = @At(
			value = "RETURN"
		)
	)
	private void onSuccessfulBlockEvent(CallbackInfoReturnable<Boolean> cir) {
		if (cir.getReturnValue()) {
			BlockEventCounters.total++;
		}
	}
}
