package block.event.separator.mixin.common;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import block.event.separator.interfaces.mixin.IServerPacketListener;

import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.common.ServerCommonPacketListener;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;

@Mixin(ServerCommonPacketListenerImpl.class)
public class ServerCommonPacketListenerImplMixin implements IServerPacketListener {

	@Shadow @Final private MinecraftServer server;

	@Inject(
		method = "handleCustomPayload",
		cancellable = true,
		at = @At(
			value = "HEAD"
		)
	)
	public void handleCustomPayload_bes(ServerboundCustomPayloadPacket packet, CallbackInfo ci) {
		PacketUtils.ensureRunningOnSameThread(packet, (ServerCommonPacketListener) this, server);

		if (handleCustomPayload_bes(packet.payload())) {
			ci.cancel();
		}
	}

	@Override
	public boolean handleCustomPayload_bes(CustomPacketPayload payload) {
		return false;
	}
}
