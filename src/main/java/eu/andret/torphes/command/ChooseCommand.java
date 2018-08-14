package eu.andret.torphes.command;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import java.util.Random;

public class ChooseCommand extends Command {
	private static final Random random = new Random();

	public ChooseCommand() {
		name = "choose";
		help = "make a decision";
		arguments = "<item> <item> ...";
		guildOnly = false;
	}

	@Override
	protected void execute(CommandEvent event) {
		if (event.getArgs().isEmpty()) {
			event.replyWarning("You didn't give me any choices!");
		} else {
			String[] items = event.getArgs().split("\\s+");
			if (items.length == 1) {
				event.replyWarning("You only gave me one option, `" + items[0] + "`");
			} else {
				event.replySuccess("I choose `" + items[random.nextInt(items.length)] + "`");
			}
		}
	}

}