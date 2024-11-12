package eu.andret.bot.discord.torphes.command;

import com.google.gson.Gson;
import eu.andret.bot.discord.torphes.entity.QuoteResponse;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;

public class DailyQuoteCommand extends ListenerAdapter {
	private static final Gson GSON = new Gson();
	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

	@Override
	public void onSlashCommandInteraction(@NotNull final SlashCommandInteractionEvent event) {
		if (!event.getName().equals("quote")) {
			return;
		}
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
				.uri(URI.create(String.format("https://dailyquote.andret.eu/pl/json/%s", now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))))
				.build();
		return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
				.thenApply(HttpResponse::body)
				.thenApply(responseBody -> GSON.fromJson(responseBody, QuoteResponse.class));

	}
}
