package eu.andret.ads.torphes.command;

import com.google.gson.reflect.TypeToken;
import eu.andret.ads.torphes.entity.Holiday;
import eu.andret.ads.torphes.entity.HolidayResponse;
import eu.andret.ads.torphes.util.Requestor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.Year;
import java.time.format.DateTimeFormatter;

public class HolidayCommand extends ListenerAdapter {
	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("EEEE, dd LLLL");
	private static final Logger LOGGER = LoggerFactory.getLogger(HolidayCommand.class);

	private final Requestor requestor;

	public HolidayCommand(@NotNull final Requestor requestor) {
		this.requestor = requestor;
	}

	@Override
	public void onSlashCommandInteraction(@NotNull final SlashCommandInteractionEvent event) {
		LOGGER.debug("Slash command: HolidayCommand");
		if (!event.getName().equals("holiday")) {
			return;
		}
		LOGGER.info("Executed command: /holiday");
		event.deferReply().queue();
		final LocalDate now = LocalDate.now();
		final String url = String.format("https://api.unusualcalendar.net/v2/holiday/pl/day/%d/%d", now.getMonthValue(), now.getDayOfMonth());
		LOGGER.debug("Requesting URL: {}", url);
		requestor.executeRequest(url, TypeToken.get(HolidayResponse.class))
				.thenAccept(holidayResponse -> {
					LOGGER.debug("Response: {}", holidayResponse);
					final LocalDate localDate = LocalDate.of(Year.now().getValue(), holidayResponse.month(), holidayResponse.day());
					final String date = localDate.format(FORMATTER);
					final Holiday holiday = holidayResponse.randomHoliday();
					event.getHook().editOriginal("")
							.setEmbeds(new EmbedBuilder()
									.setAuthor("Unusual Holiday Calendar")
									.setTitle(holiday.name())
									.setDescription(holiday.description())
									.setFooter(date)
									.build())
							.queue();
				});
	}
}
