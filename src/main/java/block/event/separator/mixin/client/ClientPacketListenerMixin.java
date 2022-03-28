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
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {

	@Shadow @Final private Minecraft minecraft;

	@Inject(
		method = "handleCustomPayload",
		cancellable = true,
		at = @At(
			value = "INVOKE",
			shift = Shift.BEFORE,
			ordinal = 0,
			target = "Lnet/minecraft/network/protocol/game/ClientboundCustomPayloadPacket;getIdentifier()Lnet/minecraft/resources/ResourceLocation;"
		)
	)
	private void handleCustomPayload(ClientboundCustomPayloadPacket packet, CallbackInfo ci) {
		ResourceLocation id = packet.getIdentifier();
		String namespace = id.getNamespace();
		String path = id.getPath();

		if (BlockEventSeparatorMod.MOD_ID.equals(namespace)) {
			FriendlyByteBuf buffer = packet.getData();

			switch (path) {
			case "freeze":
				boolean frozen = buffer.readBoolean();
				((IMinecraft)minecraft).setFrozen_bes(frozen);

				break;
			case "next_tick":
				int maxOffset = buffer.readInt();
				int interval = buffer.readInt();
				int modeIndex = buffer.readByte();
				SeparationMode mode = SeparationMode.fromIndex(modeIndex);

				((IMinecraft)minecraft).updateMaxOffset_bes(maxOffset, interval);
				BlockEventSeparatorMod.setClientSeparationInterval(interval);
				BlockEventSeparatorMod.setClientSeparationMode(mode);

				break;
			default:
				BlockEventSeparatorMod.LOGGER.info("Ignoring packet with unknown id \'" + path + "\'");

				return;
			}

			ci.cancel();
		}
	}
}
