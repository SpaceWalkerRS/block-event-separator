package block.event.separator.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record PayloadWrapper(Payload payload) implements CustomPacketPayload {

	public static final StreamCodec<FriendlyByteBuf, PayloadWrapper> STREAM_CODEC = CustomPacketPayload.codec(Payloads::encode, Payloads::decode);
	public static final Type<PayloadWrapper> TYPE = CustomPacketPayload.createType("rsmm");

	public PayloadWrapper(Payload payload) {
		this.payload = payload;
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
