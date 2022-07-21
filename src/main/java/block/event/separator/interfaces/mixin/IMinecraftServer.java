package block.event.separator.interfaces.mixin;

import net.minecraft.server.level.ServerPlayer;

public interface IMinecraftServer {

	default boolean isPaused() {
		return false;
	}

	public void onPlayerJoin_bes(ServerPlayer player);

	public void onPlayerLeave_bes(ServerPlayer player);

	public void postBlockEvents_bes();

	public void onHandshake_bes(ServerPlayer player, String modVersion);

	public boolean isBesClient(ServerPlayer player);

}
