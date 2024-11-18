package eu.andret.bot.discord.torphes.command;

import com.google.gson.reflect.TypeToken;
import eu.andret.bot.discord.torphes.entity.QuoteResponse;
import eu.andret.bot.discord.torphes.util.Requestor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DailyQuoteCommand extends ListenerAdapter {
	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private static final Logger LOGGER = LoggerFactory.getLogger(DailyQuoteCommand.class);

	private final Requestor requestor;

	public DailyQuoteCommand(@NotNull final Requestor requestor) {
		this.requestor = requestor;
	}

	@Override
	public void onSlashCommandInteraction(@NotNull final SlashCommandInteractionEvent event) {
		LOGGER.debug("Slash command: DailyQuoteCommand");
		if (!event.getName().equals("quote")) {
			return;
		}
		LOGGER.info("Executed command: /quote");
		event.deferReply().queue();
		final LocalDate now = LocalDate.now();
		final String url = String.format("https://dailyquote.andret.eu/pl/json/%s", now.format(FORMATTER));
		LOGGER.debug("Requesting URL: {}", url);
		requestor.executeRequest(url, TypeToken.get(QuoteResponse.class))
				.thenAccept(quoteResponse -> {
					LOGGER.debug("Response: {}", quoteResponse);
					final LocalDate localDate = LocalDate.now();
					final String date = localDate.format(FORMATTER);
					event.getHook().editOriginal("")
							.setEmbeds(new EmbedBuilder()
									.setTitle(quoteResponse.content())
									.setDescription(String.format("Daily Quote • %s", date))
									.setColor(Color.orange)
									.build())
							.queue();
				});
	}
}
