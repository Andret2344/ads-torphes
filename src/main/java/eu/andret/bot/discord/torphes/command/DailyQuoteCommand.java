package eu.andret.bot.discord.torphes.command;

import eu.andret.bot.discord.torphes.Torphes;
import eu.andret.bot.discord.torphes.entity.QuoteResponse;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;

public class DailyQuoteCommand extends ListenerAdapter {
	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private static final Logger LOGGER = LoggerFactory.getLogger(DailyQuoteCommand.class);

	@Override
	public void onSlashCommandInteraction(@NotNull final SlashCommandInteractionEvent event) {
		if (!event.getName().equals("quote")) {
			return;
		}
		LOGGER.info("Executed command: /quote");
		event.deferReply().queue();
		executeRequest()
				.thenAccept(quoteResponse -> {
					final LocalDate localDate = LocalDate.now();
					final String date = localDate.format(FORMATTER);
					event.getHook().editOriginal("")
							.setEmbeds(new EmbedBuilder()
									.setTitle(quoteResponse.content())
									.setDescription("Daily Quote • " + date)
									.setColor(Color.orange)
									.build())
							.queue();
				});
	}

	@NotNull
	public CompletableFuture<QuoteResponse> executeRequest() {
		final HttpClient client = HttpClient.newHttpClient();
		final LocalDate now = LocalDate.now();
		final HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(String.format("https://dailyquote.andret.eu/pl/json/%s", now.format(FORMATTER))))
				.build();
		return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
				.thenApply(HttpResponse::body)
				.thenApply(responseBody -> Torphes.GSON.fromJson(responseBody, QuoteResponse.class));
	}
}
