package block.event.separator.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import block.event.separator.interfaces.mixin.IMinecraftServer;

import net.minecraft.server.integrated.IntegratedServer;

@Mixin(IntegratedServer.class)
public abstract class IntegratedServerMixin implements IMinecraftServer {

	@Shadow private boolean isGamePaused;

	@Override
	public boolean isPaused() {
		return isGamePaused;
	}
}
