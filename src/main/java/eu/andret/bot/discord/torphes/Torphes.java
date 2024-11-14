package eu.andret.bot.discord.torphes;

import eu.andret.bot.discord.torphes.command.DailyQuoteCommand;
import eu.andret.bot.discord.torphes.command.EnterCommand;
import eu.andret.bot.discord.torphes.command.HolidayCommand;
import eu.andret.bot.discord.torphes.guild.GuildsCountListener;
import eu.andret.bot.discord.torphes.util.Requestor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Properties;

public final class Torphes {
	private static final Requestor REQUESTOR = new Requestor();

	public static void main(final String[] args) throws IOException {
		final Properties properties = loadProperties();
		Configurator.setRootLevel(Level.toLevel(properties.getProperty("logger.level"), Level.INFO));

		final JDA jda = JDABuilder.createLight(properties.getProperty("app.token"), Collections.emptyList())
				.setStatus(OnlineStatus.DO_NOT_DISTURB)
				.addEventListeners(new EnterCommand())
				.addEventListeners(new HolidayCommand(REQUESTOR))
				.addEventListeners(new GuildsCountListener())
				.addEventListeners(new DailyQuoteCommand(REQUESTOR))
				.build();

		jda.updateCommands()
				.addCommands(
						Commands
								.slash("enter", "Count how many messages and subsequent messages users sent today")
								.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.VIEW_CHANNEL)),
						Commands
								.slash("holiday", "Get random today holiday")
								.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.VIEW_CHANNEL)),
						Commands
								.slash("quote", "Get today quote")
								.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.VIEW_CHANNEL)))
				.queue();
	}

	@NotNull
	private static Properties loadProperties() throws IOException {
		final InputStream config = ClassLoader.getSystemClassLoader().getResourceAsStream("config.properties");
		final Properties properties = new Properties();
		properties.load(config);
		return properties;
	}
}
