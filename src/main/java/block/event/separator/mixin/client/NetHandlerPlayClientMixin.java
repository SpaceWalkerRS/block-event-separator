package block.event.separator.mixin.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import block.event.separator.BlockEventSeparatorMod;
import block.event.separator.SeparationMode;
import block.event.separator.interfaces.mixin.IMinecraft;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SPacketCustomPayload;

@Mixin(NetHandlerPlayClient.class)
public class NetHandlerPlayClientMixin {

	@Shadow @Final private Minecraft client;

	@Inject(
		method = "handleCustomPayload",
		cancellable = true,
		at = @At(
			value = "INVOKE",
			shift = Shift.AFTER,
			ordinal = 0,
			target = "Lnet/minecraft/network/PacketThreadUtil;checkThreadAndEnqueue(Lnet/minecraft/network/Packet;Lnet/minecraft/network/INetHandler;Lnet/minecraft/util/IThreadListener;)V"
		)
	)
	private void handleCustomPayload(SPacketCustomPayload packet, CallbackInfo ci) {
		String channel = packet.getChannelName();

		if (BlockEventSeparatorMod.PACKET_CHANNEL.equals(channel)) {
			PacketBuffer buffer = packet.getBufferData();
			int packetType = buffer.readByte();

			switch (packetType) {
			case BlockEventSeparatorMod.FREEZE_PACKET:
				boolean frozen = buffer.readBoolean();
				((IMinecraft)client).setFrozen_bes(frozen);

				break;
			case BlockEventSeparatorMod.NEXT_TICK_PACKET:
				int maxOffset = buffer.readInt();
				int interval = buffer.readInt();
				int modeIndex = buffer.readByte();
				SeparationMode mode = SeparationMode.fromIndex(modeIndex);

				((IMinecraft)client).updateMaxOffset_bes(maxOffset, interval);
				BlockEventSeparatorMod.clientSeparationInterval = interval;
				BlockEventSeparatorMod.clientSeparationMode = mode;

				break;
			default:
				BlockEventSeparatorMod.LOGGER.info("Ignoring packet with unknown id \'" + packetType + "\'");
			}

			ci.cancel();
		}
	}
}
