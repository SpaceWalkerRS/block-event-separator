package block.event.separator.interfaces.mixin;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public interface IServerboundCustomPayloadPacket {

	public ResourceLocation getIdentifier_bes();

	public FriendlyByteBuf getData_bes();

}
