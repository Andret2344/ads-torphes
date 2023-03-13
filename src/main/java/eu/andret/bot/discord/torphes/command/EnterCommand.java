package eu.andret.bot.discord.torphes.command;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EnterCommand extends ListenerAdapter {
	@Override
	public void onSlashCommandInteraction(@NotNull final SlashCommandInteractionEvent event) {
		if (!event.getName().equals("enter")) {
			event.reply("Huh?").queue();
			return;
		}
		event.deferReply().queue();
		final List<Message> messages = getTodayMessages(event);
		if (messages.isEmpty()) {
			event.getHook().sendMessage("No messages to count today!").queue();
			return;
		}
		final String mostMessages = getMostMessages(messages);
		final String mostSubsequentMessages = getMostSubsequentMessages(messages);
		final String format = "=== TOP 10 USERS — MOST MESSAGES TODAY ===```\n%s\n```\n=== TOP 10 USERS — MOST SUBSEQUENT MESSAGES TODAY ===```\n%s\n```";
		final String message = String.format(format, mostMessages, mostSubsequentMessages);
		event.getHook().sendMessage(message).queue();
	}

	@NotNull
	private String getMostMessages(@NotNull final List<Message> messages) {
		final Map<String, Long> map = messages.stream()
				.collect(Collectors.groupingBy(this::formatUserFromMessage, Collectors.counting()));
		return formatMapToText(map);
	}

	@NotNull
	private static String getMostSubsequentMessages(@NotNull final List<Message> messages) {
		final Map<String, Long> result = new HashMap<>();
		User current = null;
		long count = 0;
		for (final Message message : messages) {
			final User author = message.getAuthor();
			if (author.equals(current)) {
				count++;
			} else {
				if (current != null && result.getOrDefault(current.getAsTag(), 0L) < count) {
					result.put(current.getAsTag(), count);
				}
				current = author;
				count = 1;
			}
		}
		if (current != null && result.getOrDefault(current.getAsTag(), 0L) < count) {
			result.put(current.getAsTag(), count);
		}
		return formatMapToText(result);
	}

	@NotNull
	private static String formatMapToText(@NotNull final Map<String, Long> result) {
		return result.entrySet()
				.stream()
				.limit(10)
				.sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
				.map(entry -> String.format("%05d\t%s", entry.getValue(), entry.getKey()))
				.collect(Collectors.joining("\n"));
	}

	@NotNull
	private List<Message> getTodayMessages(@NotNull final SlashCommandInteractionEvent event) {
		return event.getChannel()
				.getIterableHistory()
				.stream()
				.filter(message -> message.getTimeCreated().isAfter(getDate()))
				.filter(message -> !message.getAuthor().getId().equals("606928324970938389"))
				.toList();
	}

	@NotNull
	public String formatUserFromMessage(@NotNull final Message message) {
		return message.getAuthor().getAsTag();
	}

	@NotNull
	@Contract(" -> new")
	private OffsetDateTime getDate() {
		return OffsetDateTime.of(LocalDate.now().atStartOfDay(), OffsetDateTime.now().getOffset());
	}
}
