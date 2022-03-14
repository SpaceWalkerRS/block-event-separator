package block.event.separator.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import block.event.separator.BlockEventCounters;
import block.event.separator.interfaces.mixin.IMinecraftServer;

import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockEventData;

@Mixin(ServerLevel.class)
public class ServerLevelMixin {

	@Shadow private MinecraftServer server;
	@Shadow private ObjectLinkedOpenHashSet<BlockEventData> blockEvents;

	@Inject(
		method = "runBlockEvents",
		at = @At(
			value = "INVOKE",
			target = "Lit/unimi/dsi/fastutil/objects/ObjectLinkedOpenHashSet;isEmpty()Z"
		)
	)
	private void onNextBlockEvent(CallbackInfo ci) {
		if (BlockEventCounters.currentBatch == 0) {
			BlockEventCounters.currentDepth++;
			BlockEventCounters.currentBatch = blockEvents.size();
		}

		BlockEventCounters.currentBatch--;
	}

	@Inject(
		method = "runBlockEvents",
		at = @At(
			value = "INVOKE",
			shift = Shift.AFTER,
			target = "Lnet/minecraft/server/players/PlayerList;broadcast(Lnet/minecraft/world/entity/player/Player;DDDDLnet/minecraft/resources/ResourceKey;Lnet/minecraft/network/protocol/Packet;)V"
		)
	)
	private void onSuccessfulBlockEvent(CallbackInfo ci) {
		BlockEventCounters.total++;
	}

	@Inject(
		method = "runBlockEvents",
		at = @At(
			value = "RETURN"
		)
	)
	private void afterBlockEvents(CallbackInfo ci) {
		((IMinecraftServer)server).postBlockEvents_bes();

		// Reset block event counters ahead of next tick.
		// Depth is set to -1 because it is incremented
		// before the first block event is processed.
		BlockEventCounters.currentDepth = -1;
		BlockEventCounters.currentBatch = 0;
		BlockEventCounters.total = 0;
	}
}
