package block.event.separator.mixin.common;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.brigadier.CommandDispatcher;

import block.event.separator.command.BlockEventSeparatorCommand;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

@Mixin(Commands.class)
public class CommandsMixin {

	@Shadow @Final private CommandDispatcher<CommandSourceStack> dispatcher;

	@Inject(
		method = "<init>",
		at = @At(
			value = "INVOKE",
			shift = Shift.BEFORE,
			target = "Lcom/mojang/brigadier/CommandDispatcher;findAmbiguities(Lcom/mojang/brigadier/AmbiguityConsumer;)V"
		)
	)
	private void registerCommands(Commands.CommandSelection selection, CallbackInfo ci) {
		BlockEventSeparatorCommand.register(dispatcher);
	}
}
