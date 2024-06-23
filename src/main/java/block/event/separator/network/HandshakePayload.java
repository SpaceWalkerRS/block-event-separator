package block.event.separator.network;

import block.event.separator.BlockEventSeparatorMod;
import block.event.separator.interfaces.mixin.IMinecraft;
import block.event.separator.interfaces.mixin.IMinecraftServer;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class HandshakePayload implements Payload {

	public String modVersion;

	public HandshakePayload() {
	}

	public HandshakePayload(String modVersion) {
		this.modVersion = modVersion;
	}

	@Override
	public String name() {
		return "handshake";
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeUtf(BlockEventSeparatorMod.MOD_VERSION);
	}

	@Override
	public void read(FriendlyByteBuf buffer) {
		modVersion = buffer.readUtf();
	}

	@Override
	public void handle(Minecraft minecraft) {
		((IMinecraft)minecraft).onHandshake_bes(modVersion);
	}

	@Override
	public void handle(MinecraftServer server, ServerPlayer player) {
		((IMinecraftServer)server).onHandshake_bes(player, modVersion);
	}
}
