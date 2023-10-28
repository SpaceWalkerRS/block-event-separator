package block.event.separator.network;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.FriendlyByteBuf.Reader;
import net.minecraft.resources.ResourceLocation;

public class Payloads {

	public static final Map<Class<? extends BESPayload>, ResourceLocation> IDS = new HashMap<>();
	public static final Map<ResourceLocation, Class<? extends BESPayload>> TYPES = new HashMap<>();

	public static final Reader<BESPayload> READER = buffer -> {
		ResourceLocation id = buffer.readResourceLocation();
		Class<? extends BESPayload> type = TYPES.get(id);

		if (type == null) {
			throw new IllegalStateException("Unable to decode payload: " + id);
		}

		BESPayload payload;
		try {
			payload = type.getDeclaredConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new RuntimeException("unable to create payload " + type, e);
		}
		payload.read(buffer);

		return payload;
	};
	public static final BiConsumer<BESPayload, FriendlyByteBuf> WRITER = (payload, buffer) -> {
		Class<? extends BESPayload> type = payload.getClass();
		ResourceLocation id = IDS.get(type);

		if (id == null) {
			throw new IllegalStateException("Unable to encode payload: " + type);
		}

		payload.write(buffer);
	};

	private static void register(BESPayload payload) {
		ResourceLocation id = payload.id();
		Class<? extends BESPayload> type = payload.getClass();

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
		register(new FreezePayload());
		register(new TickPayload());
	}
}
