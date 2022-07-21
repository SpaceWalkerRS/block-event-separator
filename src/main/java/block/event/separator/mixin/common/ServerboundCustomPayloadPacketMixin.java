package block.event.separator.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import block.event.separator.interfaces.mixin.IServerboundCustomPayloadPacket;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;

@Mixin(ServerboundCustomPayloadPacket.class)
public class ServerboundCustomPayloadPacketMixin implements IServerboundCustomPayloadPacket {

	@Shadow private ResourceLocation identifier;
	@Shadow private FriendlyByteBuf data;

	@Override
	public ResourceLocation getIdentifier_bes() {
		return identifier;
	}

	@Override
	public FriendlyByteBuf getData_bes() {
		return data;
	}
}
