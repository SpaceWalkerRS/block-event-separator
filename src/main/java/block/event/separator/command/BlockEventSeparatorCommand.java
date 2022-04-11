package block.event.separator.command;

import java.util.Collections;
import java.util.List;

import block.event.separator.BlockEventSeparatorMod;
import block.event.separator.SeparationMode;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.HoverEvent;

public class BlockEventSeparatorCommand extends CommandBase {

	private static final String COMMAND_NAME = "blockeventseparator";

	private static final String USAGE_MODE_QUERY     = singleUsage("mode");
	private static final String USAGE_MODE_SET       = singleUsage("mode <new mode>");
	private static final String USAGE_MODE           = buildUsage(USAGE_MODE_QUERY, USAGE_MODE_SET);

	private static final String USAGE_INTERVAL_QUERY = singleUsage("interval");
	private static final String USAGE_INTERVAL_SET   = singleUsage("interval <new interval>");
	private static final String USAGE_INTERVAL       = buildUsage(USAGE_INTERVAL_QUERY, USAGE_INTERVAL_SET);

	private static final String TOTAL_USAGE          = buildUsage(USAGE_MODE, USAGE_INTERVAL);

	private static String singleUsage(String usage) {
		return String.format("/%s %s", COMMAND_NAME, usage);
	}

	private static String buildUsage(String... usages) {
		return String.join(" OR ", usages);
	}

	private static final String[] MODES;

	static {

		SeparationMode[] modes = SeparationMode.values();
		MODES = new String[modes.length];

		for (int i = 0; i < modes.length; i++) {
			SeparationMode mode = modes[i];
			MODES[mode.index] = mode.name;
		}
	}

	public BlockEventSeparatorCommand() {

	}

	@Override
	public String getName() {
		return COMMAND_NAME;
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return TOTAL_USAGE;
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer minecraftServer, ICommandSender sender, String[] args, BlockPos pos) {
		switch (args.length) {
		case 1:
			return getListOfStringsMatchingLastWord(args, "mode", "interval");
		case 2:
			switch (args[0]) {
			case "mode":
				return getListOfStringsMatchingLastWord(args, MODES);
			case "interval":
				return getListOfStringsMatchingLastWord(args, "1");
			}
		}

		return Collections.emptyList();
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (args.length > 0) {
			switch (args[0]) {
			case "mode":
				switch (args.length) {
				case 1:
					queryMode(sender);
					return;
				case 2:
					setMode(sender, args[1]);
					return;
				}

				throw new WrongUsageException(USAGE_MODE);
			case "interval":
				switch (args.length) {
				case 1:
					queryInterval(sender);
					return;
				case 2:
					setInterval(sender, parseInt(args[1], 1));
					return;
				}

				throw new WrongUsageException(USAGE_INTERVAL);
			}
		}

		throw new WrongUsageException(getUsage(sender));
	}

	private void queryMode(ICommandSender sender) {
		SeparationMode mode = BlockEventSeparatorMod.serverSeparationMode;
		ITextComponent text;

		if (mode == SeparationMode.OFF) {
			text = new TextComponentString("Block event separation is currently disabled");
		} else {
			text = new TextComponentString("").
				appendText("Block event separation is currently running in [").
				appendSibling(new TextComponentString(mode.name).setStyle(new Style().
					setColor(TextFormatting.GREEN).
					setHoverEvent(new HoverEvent(
						HoverEvent.Action.SHOW_TEXT,
						new TextComponentString(mode.description))))).
				appendText("] mode");
		}

		sender.sendMessage(text);
	}

	private void setMode(ICommandSender sender, String name) throws WrongUsageException {
		SeparationMode mode = SeparationMode.fromName(name);

		if (mode == null) {
			throw new WrongUsageException(USAGE_MODE_SET);
		}

		BlockEventSeparatorMod.serverSeparationMode = mode;
		ITextComponent text;

		if (mode == SeparationMode.OFF) {
			text = new TextComponentString("Disabled block event separation");
		} else {
			text = new TextComponentString("").
				appendText("Enabled block event separation in [").
				appendSibling(new TextComponentString(mode.name).setStyle(new Style().
					setColor(TextFormatting.GREEN).
					setHoverEvent(new HoverEvent(
						HoverEvent.Action.SHOW_TEXT,
						new TextComponentString(mode.description))))).
				appendText("] mode");
		}

		notifyCommandListener(sender, this, text.getFormattedText());
	}

	private void queryInterval(ICommandSender sender) {
		int interval = BlockEventSeparatorMod.serverSeparationInterval;

		ITextComponent text = new TextComponentString(String.format("The separation interval is currently set to %s", interval));
		sender.sendMessage(text);
	}

	private void setInterval(ICommandSender sender, int interval) {
		BlockEventSeparatorMod.serverSeparationInterval = interval;

		ITextComponent text = new TextComponentString(String.format("Set the separation interval to %s", interval));
		notifyCommandListener(sender, this, text.getFormattedText());
	}
}
