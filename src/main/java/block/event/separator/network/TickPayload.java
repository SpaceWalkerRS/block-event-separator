package block.event.separator.network;

import block.event.separator.BlockEventSeparatorMod;
import block.event.separator.SeparationMode;
import block.event.separator.interfaces.mixin.IMinecraft;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class TickPayload implements Payload {

	private int maxOffset;
	private int separationInterval;
	private SeparationMode mode;

	public TickPayload() {
	}

	public TickPayload(int maxOffset, int separationInterval, SeparationMode mode) {
		this.maxOffset = maxOffset;
		this.separationInterval = separationInterval;
		this.mode = mode;
	}

	@Override
	public String name() {
		return "next_tick";
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeInt(maxOffset);
		buffer.writeInt(separationInterval);
		buffer.writeByte(mode.index);
	}

	@Override
	public void read(FriendlyByteBuf buffer) {
		maxOffset = buffer.readInt();
		separationInterval = buffer.readInt();
		mode = SeparationMode.fromIndex(buffer.readByte());
	}

	@Override
	public void handle(Minecraft minecraft) {
		((IMinecraft)minecraft).updateMaxOffset_bes(maxOffset, separationInterval);
		BlockEventSeparatorMod.setClientSeparationInterval(separationInterval);
		BlockEventSeparatorMod.setClientSeparationMode(mode);
	}

	@Override
	public void handle(MinecraftServer server, ServerPlayer player) {
	}
}
