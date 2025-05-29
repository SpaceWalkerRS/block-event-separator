package block.event.separator.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import block.event.separator.BlockEventSeparatorMod;
import block.event.separator.SeparationMode;
import block.event.separator.network.BESPayload;
import block.event.separator.network.HandshakePayload;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.CommonListenerCookie;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin extends ClientCommonPacketListenerImpl {

	private ClientPacketListenerMixin(Minecraft minecraft, Connection connection, CommonListenerCookie cookie) {
		super(minecraft, connection, cookie);
	}

	@Inject(
		method = "handleLogin",
		at = @At(
			value = "RETURN"
		)
	)
	private void onConnect(ClientboundLoginPacket loginPacket, CallbackInfo ci) {
		BlockEventSeparatorMod.setClientSeparationMode(SeparationMode.OFF);
		BlockEventSeparatorMod.setClientSeparationInterval(1);

		send(new ServerboundCustomPayloadPacket(new HandshakePayload(BlockEventSeparatorMod.MOD_VERSION)));
	}

	@Inject(
		method = "handleCustomPayload",
		cancellable = true,
		at = @At(
			value = "HEAD"
		)
	)
	private void handleCustomPayload(CustomPacketPayload customPayload, CallbackInfo ci) {
		if (customPayload instanceof BESPayload payload) {
			payload.handle(minecraft);
			ci.cancel();
		}
	}
}
