package block.event.separator.mixin.common;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import block.event.separator.interfaces.mixin.IMinecraftServer;

import net.minecraft.network.Connection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;

@Mixin(PlayerList.class)
public class PlayerListMixin {

	@Shadow @Final private MinecraftServer server;

	@Inject(
		method = "placeNewPlayer",
		at = @At(
			value = "RETURN"
		)
	)
	private void onPlayerJoin(Connection connection, ServerPlayer player, CallbackInfo ci) {
		((IMinecraftServer)server).onPlayerJoin_bes(player);
	}

	@Inject(
		method = "remove",
		at = @At(
			value = "HEAD"
		)
	)
	private void onPlayerLeave(ServerPlayer player, CallbackInfo ci) {
		((IMinecraftServer)server).onPlayerLeave_bes(player);
	}
}
