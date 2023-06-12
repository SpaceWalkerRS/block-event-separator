package block.event.separator.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import block.event.separator.BlockEventSeparatorMod;
import block.event.separator.SeparationMode;
import block.event.separator.interfaces.mixin.IMinecraftServer;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class BlockEventSeparatorCommand {

	private static final String[] MODES;
	private static final SimpleCommandExceptionType ERROR_INVALID_NAME = new SimpleCommandExceptionType(Component.literal("That is not a valid mode!"));

	static {

		SeparationMode[] modes = SeparationMode.values();
		MODES = new String[modes.length];

		for (int i = 0; i < modes.length; i++) {
			SeparationMode mode = modes[i];
			MODES[mode.index] = mode.name;
		}
	}

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		LiteralArgumentBuilder<CommandSourceStack> builder = Commands.
			literal("blockeventseparator").
			requires(source -> source.hasPermission(2) && isBesClient(source)).
			executes(context -> queryMode(context.getSource())).
			then(Commands.
				literal("mode").
				executes(context -> queryMode(context.getSource())).
				then(Commands.
					argument("new mode", StringArgumentType.word()).
					suggests((context, suggestionsBuilder) -> SharedSuggestionProvider.suggest(MODES, suggestionsBuilder)).
					executes(context -> setMode(context.getSource(), StringArgumentType.getString(context, "new mode"))))).
			then(Commands.
				literal("interval").
				executes(context -> queryInterval(context.getSource())).
				then(Commands.
					argument("new interval", IntegerArgumentType.integer(1, 64)).
					executes(context -> setInterval(context.getSource(), IntegerArgumentType.getInteger(context, "new interval")))));

		dispatcher.register(builder);
	}

	private static boolean isBesClient(CommandSourceStack source) {
		MinecraftServer server = source.getServer();
		ServerPlayer player = source.getPlayer();

		return player != null && ((IMinecraftServer)server).isBesClient(player);
	}

	private static int queryMode(CommandSourceStack source) {
		SeparationMode mode = BlockEventSeparatorMod.getServerSeparationMode();

		source.sendSuccess(() -> {
			if (mode == SeparationMode.OFF) {
				return Component.literal("Block event separation is currently disabled");
			} else {
				return Component.literal("").
					append("Block event separation is currently running in [").
					append(Component.literal(mode.name).withStyle(style -> style.
						applyFormat(ChatFormatting.GREEN).
						withHoverEvent(new HoverEvent(
							HoverEvent.Action.SHOW_TEXT,
							Component.literal(mode.description))))).
					append("] mode");
			}
		}, false);

		return Command.SINGLE_SUCCESS;
	}

	private static int setMode(CommandSourceStack source, String name) throws CommandSyntaxException {
		SeparationMode mode = SeparationMode.fromName(name);

		if (mode == null) {
			throw ERROR_INVALID_NAME.create();
		}

		BlockEventSeparatorMod.setServerSeparationMode(mode);

		source.sendSuccess(() -> {
			if (mode == SeparationMode.OFF) {
				return Component.literal("Disabled block event separation");
			} else {
				return Component.literal("").
					append("Enabled block event separation in [").
					append(Component.literal(mode.name).withStyle(style -> style.
						applyFormat(ChatFormatting.GREEN).
						withHoverEvent(new HoverEvent(
							HoverEvent.Action.SHOW_TEXT,
							Component.literal(mode.description))))).
					append("] mode");
			}
		}, true);

		return Command.SINGLE_SUCCESS;
	}

	private static int queryInterval(CommandSourceStack source) {
		int interval = BlockEventSeparatorMod.getServerSeparationInterval();

		source.sendSuccess(() -> {
			return Component.literal(String.format("The separation interval is currently set to %s", interval));
		}, false);

		return Command.SINGLE_SUCCESS;
	}

	private static int setInterval(CommandSourceStack source, int interval) {
		BlockEventSeparatorMod.setServerSeparationInterval(interval);

		source.sendSuccess(() -> {
			return Component.literal(String.format("Set the separation interval to %s", interval));
		}, true);

		return Command.SINGLE_SUCCESS;
	}
}
