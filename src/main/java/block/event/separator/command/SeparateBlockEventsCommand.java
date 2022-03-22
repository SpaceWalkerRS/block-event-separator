package block.event.separator.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import block.event.separator.BlockEventSeparator;
import block.event.separator.BlockEventSeparator.Mode;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.TextComponent;

public class SeparateBlockEventsCommand {

	private static final String[] MODES;
	private static final SimpleCommandExceptionType ERROR_INVALID_NAME = new SimpleCommandExceptionType(new TextComponent("That is not a valid mode!"));

	static {

		Mode[] modes = Mode.values();
		MODES = new String[modes.length];

		for (int i = 0; i < modes.length; i++) {
			Mode mode = modes[i];
			MODES[mode.index] = mode.name;
		}
	}

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		LiteralArgumentBuilder<CommandSourceStack> builder = Commands.
			literal("separateblockevents").
			requires(source -> source.hasPermission(2)).
			executes(context -> query(context.getSource())).
			then(Commands.
				argument("mode", StringArgumentType.word()).
				suggests((context, suggestionsBuilder) -> SharedSuggestionProvider.suggest(MODES, suggestionsBuilder)).
				executes(context -> set(context.getSource(), StringArgumentType.getString(context, "mode"))));

		dispatcher.register(builder);
	}

	private static int query(CommandSourceStack source) {
		Mode mode = BlockEventSeparator.mode;
		Component text;

		if (mode == Mode.OFF) {
			text = new TextComponent("Block event separation is currently disabled");
		} else {
			text = new TextComponent("").
				append("Block event separation is currently running in [").
				append(new TextComponent(mode.name).withStyle(style -> style.
					setColor(ChatFormatting.GREEN).
					setHoverEvent(new HoverEvent(
						HoverEvent.Action.SHOW_TEXT,
						new TextComponent(mode.description))))).
				append("] mode");
		}

		source.sendSuccess(text, false);

		return Command.SINGLE_SUCCESS;
	}

	private static int set(CommandSourceStack source, String name) throws CommandSyntaxException {
		Mode mode = Mode.fromName(name);

		if (mode == null) {
			throw ERROR_INVALID_NAME.create();
		}

		BlockEventSeparator.mode = mode;
		Component text;

		if (mode == Mode.OFF) {
			text = new TextComponent("Disabled block event separation");
		} else {
			text = new TextComponent("").
				append("Enabled block event separation in [").
				append(new TextComponent(mode.name).withStyle(style -> style.
					setColor(ChatFormatting.GREEN).
					setHoverEvent(new HoverEvent(
						HoverEvent.Action.SHOW_TEXT,
						new TextComponent(mode.description))))).
				append("] mode");
		}

		source.sendSuccess(text, true);

		return Command.SINGLE_SUCCESS;
	}
}
