package block.event.separator.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public interface Payload {

	String name();

	void write(FriendlyByteBuf buffer);

	void read(FriendlyByteBuf buffer);

	void handle(Minecraft minecraft);

	void handle(MinecraftServer server, ServerPlayer player);

}
