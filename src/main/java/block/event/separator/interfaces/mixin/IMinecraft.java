package block.event.separator.interfaces.mixin;

import net.minecraft.client.DeltaTracker;

public interface IMinecraft {

	public void onHandshake_bes(String modVersion);

	public void updateMaxOffset_bes(int maxOffset, int interval);

	public DeltaTracker.Timer getTimer_bes();

}
