package block.event.separator.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import block.event.separator.BlockEventSeparator;
import block.event.separator.interfaces.mixin.IMinecraftServer;

import net.minecraft.Util;
import net.minecraft.server.MinecraftServer;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin implements IMinecraftServer {

	@Shadow private long nextTickTime;
	@Shadow private long delayedTasksMaxNextTickTime;

	// if this is not initialized the first server tick will take a looooooong time...
	private long tickStartTime_bes = Util.getMillis();

	/** The greatest block event depth across all levels in the past tick. */
	private long maxBlockEventDepth_bes;
	/** The greatest number of block events across all levels in the past tick. */
	private long maxBlockEventTotal_bes;

	@Inject(
		method = "waitUntilNextTick",
		at = @At(
			value = "HEAD"
		)
	)
	private void adjustTickEndTime(CallbackInfo ci) {
		long baseTickTime = nextTickTime - tickStartTime_bes;
		long extraTickTime = getExtraTickTime_bes(baseTickTime);

		// adjust the tick end time
		nextTickTime += extraTickTime;
		delayedTasksMaxNextTickTime += extraTickTime;
		// save the start time of the next tick
		tickStartTime_bes = nextTickTime;

		// reset block event counters ahead of next tick
		maxBlockEventDepth_bes = 0;
		maxBlockEventTotal_bes = 0;
	}

	@Override
	public void postBlockEvents_bes(long maxDepth, long total) {
		maxBlockEventDepth_bes = Math.max(maxDepth, maxBlockEventDepth_bes);
		maxBlockEventTotal_bes = Math.max(total, maxBlockEventTotal_bes);
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
