package block.event.separator.network;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import block.event.separator.BlockEventSeparatorMod;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class Payloads {

	public static final Map<Class<? extends Payload>, ResourceLocation> IDS = new HashMap<>();
	public static final Map<ResourceLocation, Class<? extends Payload>> TYPES = new HashMap<>();

	public static void encode(PayloadWrapper wrapper, FriendlyByteBuf buffer) {
		Payload packet = wrapper.payload();
		ResourceLocation id = Payloads.IDS.get(wrapper.payload().getClass());

		if (id == null) {
			throw new IllegalStateException("Unable to encode packet: " + packet.getClass());
		}

		buffer.writeResourceLocation(id);
		packet.write(buffer);
	}

	public static PayloadWrapper decode(FriendlyByteBuf buffer) {
		ResourceLocation id = buffer.readResourceLocation();

		Payload packet;
		try {
			packet = Payloads.TYPES.get(id).getConstructor().newInstance();
		} catch (IllegalArgumentException | InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new IllegalStateException("Unable to decode packet: " + id, e);
		}

		packet.read(buffer);

		return new PayloadWrapper(packet);
	}

	private static void register(Payload payload) {
		ResourceLocation id = ResourceLocation.fromNamespaceAndPath(BlockEventSeparatorMod.MOD_ID, payload.name());
		Class<? extends Payload> type = payload.getClass();

		if (TYPES.containsKey(id)) {
			throw new RuntimeException("duplicate packet id " + id);
		}
		if (IDS.containsKey(type)) {
			throw new RuntimeException("duplicate packet type " + type + " registered for " + id + " but was already present for " + IDS.get(type));
		}

		TYPES.put(id, type);
		IDS.put(type, id);
	}

	static {
		register(new HandshakePayload());
		register(new TickPayload());
	}
}
