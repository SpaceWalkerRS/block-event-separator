package block.event.separator.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import block.event.separator.BlockEvent;
import block.event.separator.BlockEventCounters;
import block.event.separator.BlockEventSeparator;
import block.event.separator.interfaces.mixin.IClientboundBlockEventPacket;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundBlockEventPacket;
import net.minecraft.world.level.block.Block;

// Adding data to an existing packet is risky. A lot can go wrong
// when multiple mods attempt it, and when the server and client
// have a mismatched mod set. To minimize the risk of data being
// lost, the priority is set extremely high. This makes it likely
// that our data is read and written last.
@Mixin(
	value = ClientboundBlockEventPacket.class,
	priority = Integer.MAX_VALUE
)
public class ClientboundBlockEventPacketMixin implements IClientboundBlockEventPacket {

	@Shadow private BlockPos pos;
	@Shadow private Block block;
	@Shadow private int b0;
	@Shadow private int b1;

	private int animationOffset_bes;

	@Inject(
		method = "<init>(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Block;II)V",
		at = @At(
			value = "RETURN"
		)
	)
	private void onInit(BlockPos pos, Block block, int type, int data, CallbackInfo ci) {
		animationOffset_bes = switch (BlockEventSeparator.mode) {
			case DEPTH -> BlockEventCounters.currentDepth; // depth is zero-indexed
			case INDEX -> BlockEventCounters.total - 1;    // total is not
			default    -> 0;
		};
	}

	@Inject(
		method = "<init>(Lnet/minecraft/network/FriendlyByteBuf;)V",
		at = @At(
			value = "RETURN"
		)
	)
	private void onInit(FriendlyByteBuf buffer, CallbackInfo ci) {
		animationOffset_bes = buffer.readInt();
	}

	@Inject(
		method = "write",
		at = @At(
			value = "RETURN"
		)
	)
	private void onWrite(FriendlyByteBuf buffer, CallbackInfo ci) {
		buffer.writeInt(animationOffset_bes);
	}

	@Override
	public BlockEvent getBlockEvent_bes() {
		return new BlockEvent(pos, block, b0, b1, animationOffset_bes);
	}
}
