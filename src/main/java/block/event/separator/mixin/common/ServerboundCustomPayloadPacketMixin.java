package block.event.separator.mixin.common;

import java.util.ArrayList;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import block.event.separator.network.PayloadWrapper;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

@Mixin(ServerboundCustomPayloadPacket.class)
public class ServerboundCustomPayloadPacketMixin {

	@Inject(
			method = "method_58271",
			at = @At(
				value = "HEAD"
			)
		)
		private static void bes$registerRsmmPayloads(ArrayList<CustomPacketPayload.TypeAndCodec<? super RegistryFriendlyByteBuf, ?>> typesAndCodecs, CallbackInfo ci) {
			typesAndCodecs.add(new CustomPacketPayload.TypeAndCodec<>(PayloadWrapper.TYPE, PayloadWrapper.STREAM_CODEC));
		}
}
