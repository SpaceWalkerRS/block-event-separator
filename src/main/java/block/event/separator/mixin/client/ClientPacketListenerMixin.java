package block.event.separator.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import block.event.separator.BlockEventSeparatorMod;
import block.event.separator.network.PayloadWrapper;
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
	private void bes$handleLogin(ClientboundLoginPacket packet, CallbackInfo ci) {
		send(new ServerboundCustomPayloadPacket(new PayloadWrapper(new HandshakePayload(BlockEventSeparatorMod.MOD_VERSION))));
	}

	@Inject(
		method = "handleCustomPayload",
		cancellable = true,
		at = @At(
			value = "HEAD"
		)
	)
	private void bes$handleCustomPayload(CustomPacketPayload packet, CallbackInfo ci) {
		if (packet instanceof PayloadWrapper p) {
			p.payload().handle(minecraft);
			ci.cancel();
		}
	}
}
