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
	/** The start time of the current tick. */
	private long tickTime_bes = Util.getMillis();

	// These are the tick lengths based only on the number of block
	// events that tick. The actual length of any tick might be longer
	// due to block events in previous ticks.
	private long prevPrevTickLength_bes;
	private long prevTickLength_bes;
	private long tickLength_bes;

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
	private void adjustNextTickTime(CallbackInfo ci) {
		// Each tick is lengthened based on the number of block events
		// that happened the ticks before. Pistons only animate in the
		// second and third ticks of their existence, so those need to
		// be lengthened too, to keep the animations smooth.

		long baseTickLength = nextTickTime - tickTime_bes;
		long extraTickLength = getExtraTickLength_bes(baseTickLength);

		prevPrevTickLength_bes = prevTickLength_bes;
		prevTickLength_bes = tickLength_bes;
		tickLength_bes = baseTickLength + extraTickLength;

		// determine length of this tick
		long tickLength = MathUtils.max(prevPrevTickLength_bes, prevTickLength_bes, tickLength_bes);

		// adjust next tick time
		extraTickLength = tickLength - baseTickLength;
		nextTickTime += extraTickLength;
		delayedTasksMaxNextTickTime += extraTickLength;

		// save start time of the next tick
		tickTime_bes = nextTickTime;

		// reset block event counters ahead of next tick
		maxBlockEventDepth_bes = 0;
		maxBlockEventTotal_bes = 0;
	}

	@Override
	public void postBlockEvents_bes() {
		maxBlockEventDepth_bes = Math.max(maxBlockEventDepth_bes, BlockEventCounters.currentDepth);
		maxBlockEventTotal_bes = Math.max(maxBlockEventTotal_bes, BlockEventCounters.total);
	}

	private long getExtraTickLength_bes(long baseTickLength) {
		long separations = switch (BlockEventSeparator.mode) {
			case DEPTH -> maxBlockEventDepth_bes;
			case TOTAL -> maxBlockEventTotal_bes;
			default    -> 0;
		};

		return separations == 0 ? 0 : baseTickLength * (separations - 1);
	}
}
