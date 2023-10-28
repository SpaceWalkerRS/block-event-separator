package block.event.separator.network;

import block.event.separator.BlockEventSeparatorMod;
import block.event.separator.interfaces.mixin.IMinecraft;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class FreezePayload implements BESPayload {

	private boolean frozen;

	public FreezePayload() {
	}

	public FreezePayload(boolean frozen) {
		this.frozen = frozen;
	}

	@Override
	public ResourceLocation id() {
		return new ResourceLocation(BlockEventSeparatorMod.MOD_ID, "freeze");
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeBoolean(frozen);
	}

	@Override
	public void read(FriendlyByteBuf buffer) {
		frozen = buffer.readBoolean();
	}

	@Override
	public void handle(Minecraft minecraft) {
		((IMinecraft)minecraft).setFrozen_bes(frozen);
	}

	@Override
	public void handle(MinecraftServer server, ServerPlayer player) {
	}
}
