package block.event.separator.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import block.event.separator.BlockEventCounters;
import block.event.separator.TimerHelper;
import block.event.separator.interfaces.mixin.ITimer;

import net.minecraft.client.Timer;

@Mixin(Timer.class)
public class TimerMixin implements ITimer {

	@Shadow private float partialTick;

	@Override
	public void savePartialTick_bes() {
		TimerHelper.savedPartialTick = partialTick;

		float subTicks = BlockEventCounters.subticks;
		float range = BlockEventCounters.subticksTarget + 1;

		partialTick = (subTicks + partialTick) / range;
	}

	@Override
	public void loadPartialTick_bes() {
		if (TimerHelper.savedPartialTick >= 0.0F) {
			partialTick = TimerHelper.savedPartialTick;
			TimerHelper.savedPartialTick = -1.0F;
		}
	}
}
