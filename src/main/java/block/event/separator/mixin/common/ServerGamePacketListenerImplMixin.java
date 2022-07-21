package block.event.separator.mixin.common;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import block.event.separator.BlockEventSeparatorMod;
import block.event.separator.interfaces.mixin.IMinecraftServer;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin {

	@Shadow @Final private MinecraftServer server;
    @Shadow private ServerPlayer player;

	@Inject(
		method = "handleCustomPayload",
		cancellable = true,
		at = @At(
			value = "HEAD"
		)
	)
	private void handleCustomPayload(ServerboundCustomPayloadPacket packet, CallbackInfo ci) {
		PacketUtils.ensureRunningOnSameThread(packet, (ServerGamePacketListener)this, server);

		ResourceLocation id = packet.getIdentifier();
		String namespace = id.getNamespace();
		String path = id.getPath();

		if (BlockEventSeparatorMod.MOD_ID.equals(namespace)) {
			FriendlyByteBuf buffer = packet.getData();

			switch (path) {
			case "handshake":
				String modVersion = buffer.readUtf();
				((IMinecraftServer)server).onHandshake_bes(player, modVersion);

				break;
			default:
				return;
			}

			ci.cancel();
		}
	}
}
