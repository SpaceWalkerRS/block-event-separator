package block.event.separator.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import block.event.separator.interfaces.mixin.IServerPacketListener;
import block.event.separator.network.BESPayload;

import net.minecraft.network.Connection;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin extends ServerCommonPacketListenerImpl implements IServerPacketListener {

	@Shadow private ServerPlayer player;

	private ServerGamePacketListenerImplMixin(MinecraftServer server, Connection connection, CommonListenerCookie cookie) {
		super(server, connection, cookie);
	}

	@Override
	public boolean handleCustomPayload_bes(CustomPacketPayload customPayload) {
		if (customPayload instanceof BESPayload payload) {
			payload.handle(server, player);
			return true;
		}
		return false;
	}
}
