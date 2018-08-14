package eu.andret.torphes;

import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.examples.command.AboutCommand;
import com.jagrosh.jdautilities.examples.command.PingCommand;
import com.jagrosh.jdautilities.examples.command.ShutdownCommand;
import eu.andret.torphes.command.CatCommand;
import eu.andret.torphes.command.ChooseCommand;
import eu.andret.torphes.command.HelloCommand;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Game;

import javax.security.auth.login.LoginException;
import java.awt.Color;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Torphes {
	public static void main(String[] args) throws IOException, LoginException {
		// config.txt contains two lines
		List<String> list = Files.readAllLines(Paths.get("config.txt"));

		// the first is the bot token
		String token = list.get(0);

		// the second is the bot's owner's id
		String ownerId = list.get(1);

		// define an eventwaiter, dont forget to add this to the JDABuilder!
		EventWaiter waiter = new EventWaiter();

		// define a command client
		CommandClientBuilder client = new CommandClientBuilder();

		// The default is "Type !!help" (or whatver prefix you set)
		client.useDefaultGame();

		// sets the owner of the bot
		client.setOwnerId(ownerId);

		// sets emojis used throughout the bot on successes, warnings, and failures
		client.setEmojis("\uD83D\uDE03", "\uD83D\uDE2E", "\uD83D\uDE26");

		// sets the bot prefix
		client.setPrefix("/");

		// adds commands
		client.addCommands(
				// command to show information about the bot
				new AboutCommand(Color.BLUE, "an example bot",
						new String[]{"Cool commands", "Nice examples", "Lots of fun!"},
						Permission.ADMINISTRATOR),

				new HelloCommand(waiter),

				new CatCommand(),

				new ChooseCommand(),

				new PingCommand(),

				new ShutdownCommand());
		new JDABuilder(AccountType.BOT)
				.setToken(token)
				.setStatus(OnlineStatus.DO_NOT_DISTURB)
				.setGame(Game.playing("loading..."))
				.addEventListener(waiter)
				.addEventListener(client.build()).build();
	}
}
