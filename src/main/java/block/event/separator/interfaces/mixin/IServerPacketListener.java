package block.event.separator.interfaces.mixin;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public interface IServerPacketListener {

	boolean handleCustomPayload_bes(CustomPacketPayload payload);

}
