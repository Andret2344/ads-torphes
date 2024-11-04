package eu.andret.bot.discord.torphes.entity;

import java.util.List;
import java.util.Random;

public record HolidayResponse(String id, int day, int month, List<Holiday> holidays) {
	private static final Random RANDOM = new Random();

	public Holiday randomHoliday() {
		return holidays.get(RANDOM.nextInt(holidays.size()));
	}
}
