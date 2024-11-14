package eu.andret.bot.discord.torphes.entity;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;

public record HolidayResponse(@NotNull String id, int day, int month, @NotNull List<Holiday> holidays) {
	private static final Random RANDOM = new Random();

	public Holiday randomHoliday() {
		return holidays.get(RANDOM.nextInt(holidays.size()));
	}
}
