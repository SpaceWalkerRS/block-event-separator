package block.event.separator.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import block.event.separator.BlockEventCounters;
import block.event.separator.interfaces.mixin.IClientboundBlockEventPacket;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEventPacket;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {

	@Inject(
		method = "handleBlockEvent",
		at = @At(
			value = "INVOKE",
			shift = Shift.BEFORE,
			target = "Lnet/minecraft/client/multiplayer/ClientLevel;blockEvent(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Block;II)V"
		)
	)
	private void onBlockEvent(ClientboundBlockEventPacket packet, CallbackInfo ci) {
		int animationOffset = ((IClientboundBlockEventPacket)packet).getAnimationOffset_bes();

		BlockEventCounters.currentOffset = animationOffset;
		BlockEventCounters.maxOffset = Math.max(BlockEventCounters.maxOffset, animationOffset);
	}
}
