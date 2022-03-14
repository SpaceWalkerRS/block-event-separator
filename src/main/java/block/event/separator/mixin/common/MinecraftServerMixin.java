package block.event.separator.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import block.event.separator.BlockEventCounters;
import block.event.separator.BlockEventSeparator;
import block.event.separator.interfaces.mixin.IMinecraftServer;
import block.event.separator.utils.MathUtils;

import net.minecraft.Util;
import net.minecraft.server.MinecraftServer;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin implements IMinecraftServer {

	@Shadow private long nextTickTime;
	@Shadow private long delayedTasksMaxNextTickTime;

	// if this is not initialized the first server tick will take a looooooong time...
	private long tickStartTime_bes = Util.getMillis();

	private long prevPrevExtraTickTime_bes;
	private long prevExtraTickTime_bes;
	private long extraTickTime_bes;

	/** The greatest block event depth across all levels in the past tick. */
	private int maxBlockEventDepth_bes;
	/** The greatest number of block events across all levels in the past tick. */
	private int maxBlockEventTotal_bes;

	@Inject(
		method = "waitUntilNextTick",
		at = @At(
			value = "HEAD"
		)
	)
	private void adjustTickEndTime(CallbackInfo ci) {
		// Each tick is lengthened based on the number of block events
		// that happened the ticks before. Pistons only animate in the
		// second and third ticks of their existence, so those need to
		// be lengthened. The tick after is also lengthened, both for
		// compatibility with G4mespeed and convenience when working
		// with 0-tick contraptions, which often operate in 3gt intervals.

		long baseTickTime = nextTickTime - tickStartTime_bes;
		long extraTickTime = MathUtils.max(prevPrevExtraTickTime_bes, prevExtraTickTime_bes, extraTickTime_bes);

		// adjust tick end time
		nextTickTime += extraTickTime;
		delayedTasksMaxNextTickTime += extraTickTime;
		// save start time of the next tick
		tickStartTime_bes = nextTickTime;

		// save extra tick time for the next tick
		prevPrevExtraTickTime_bes = prevExtraTickTime_bes;
		prevExtraTickTime_bes = extraTickTime_bes;
		extraTickTime_bes = getExtraTickTime_bes(baseTickTime);

		// reset block event counters ahead of next tick
		maxBlockEventDepth_bes = 0;
		maxBlockEventTotal_bes = 0;
	}

	@Override
	public void postBlockEvents_bes() {
		maxBlockEventDepth_bes = Math.max(maxBlockEventDepth_bes, BlockEventCounters.currentDepth);
		maxBlockEventTotal_bes = Math.max(maxBlockEventTotal_bes, BlockEventCounters.total);
	}

	private long getExtraTickTime_bes(long baseTickTime) {
		long separations = switch (BlockEventSeparator.mode) {
			case DEPTH -> maxBlockEventDepth_bes;
			case TOTAL -> maxBlockEventTotal_bes;
			default    -> 0;
		};

		return separations == 0 ? 0 : baseTickTime * (separations - 1);
	}
}
