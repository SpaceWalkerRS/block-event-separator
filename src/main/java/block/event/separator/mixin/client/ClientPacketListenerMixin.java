package block.event.separator.mixin.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import block.event.separator.BlockEvent;
import block.event.separator.interfaces.mixin.IClientboundBlockEventPacket;
import block.event.separator.interfaces.mixin.IMinecraft;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEventPacket;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {

	@Shadow @Final private Minecraft minecraft;

	@Inject(
		method = "handleBlockEvent",
		cancellable = true,
		at = @At(
			value = "INVOKE",
			shift = Shift.BEFORE,
			target = "Lnet/minecraft/client/multiplayer/ClientLevel;blockEvent(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Block;II)V"
		)
	)
	private void onBlockEvent(ClientboundBlockEventPacket packet, CallbackInfo ci) {
		BlockEvent blockEvent = ((IClientboundBlockEventPacket)packet).getBlockEvent_bes();
		((IMinecraft)minecraft).queueBlockEvent_bes(blockEvent);

		ci.cancel();
	}
}
