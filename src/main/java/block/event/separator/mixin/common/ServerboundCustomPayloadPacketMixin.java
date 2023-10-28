package block.event.separator.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import block.event.separator.network.Payloads;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

@Mixin(ServerboundCustomPayloadPacket.class)
public class ServerboundCustomPayloadPacketMixin {

	@Inject(
		method = "readPayload",
		cancellable = true,
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/network/protocol/common/ServerboundCustomPayloadPacket;readUnknownPayload(Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/network/FriendlyByteBuf;)Lnet/minecraft/network/protocol/common/custom/DiscardedPayload;"
		)
	)
	private static void rsmm$readPayload(ResourceLocation channel, FriendlyByteBuf buffer, CallbackInfoReturnable<CustomPacketPayload> cir) {
		if (Payloads.TYPES.containsKey(channel)) {
			cir.setReturnValue(Payloads.READER.apply(buffer));
		}
	}
}
