package block.event.separator.interfaces.mixin;

public interface IMinecraftServer {

	default boolean isPaused() {
		return false;
	}

	public void postBlockEvents_bes();

}
