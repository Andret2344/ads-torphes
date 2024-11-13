package eu.andret.bot.discord.torphes.command;

import eu.andret.bot.discord.torphes.Torphes;
import eu.andret.bot.discord.torphes.entity.Holiday;
import eu.andret.bot.discord.torphes.entity.HolidayResponse;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;

public class HolidayCommand extends ListenerAdapter {
	@Override
	public void onSlashCommandInteraction(@NotNull final SlashCommandInteractionEvent event) {
		if (!event.getName().equals("holiday")) {
			return;
		}
		event.deferReply().queue();
		executeRequest()
				.thenAccept(holidayResponse -> {
					final Holiday holiday = holidayResponse.randomHoliday();
					final LocalDate localDate = LocalDate.of(Year.now().getValue(), holidayResponse.month(), holidayResponse.day());
					final String ddMmmm = localDate.format(DateTimeFormatter.ofPattern("MMMM, dd"));
					event.getHook().editOriginal("")
							.setEmbeds(new EmbedBuilder()
									.setAuthor("Unusual Holiday Calendar - " + ddMmmm)
									.setTitle(holiday.name())
									.setDescription(holiday.description())
									.build())
							.queue();
				});
	}

	@NotNull
	public CompletableFuture<HolidayResponse> executeRequest() {
		final HttpClient client = HttpClient.newHttpClient();
		final LocalDate now = LocalDate.now();
		final HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(String.format("https://api.unusualcalendar.net/v2/holiday/pl/day/%d/%d", now.getMonthValue(), now.getDayOfMonth())))
				.build();
		return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
				.thenApply(HttpResponse::body)
				.thenApply(responseBody -> Torphes.GSON.fromJson(responseBody, HolidayResponse.class));

	}
}
