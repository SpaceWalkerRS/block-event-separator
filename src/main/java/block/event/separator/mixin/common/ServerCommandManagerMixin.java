package block.event.separator.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import block.event.separator.command.BlockEventSeparatorCommand;

import net.minecraft.command.CommandHandler;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.server.MinecraftServer;

@Mixin(ServerCommandManager.class)
public abstract class ServerCommandManagerMixin extends CommandHandler {

	@Inject(
		method = "<init>",
		at = @At(
			value = "RETURN"
		)
	)
	private void registerCommands(MinecraftServer server, CallbackInfo ci) {
		registerCommand(new BlockEventSeparatorCommand());
	}
}
