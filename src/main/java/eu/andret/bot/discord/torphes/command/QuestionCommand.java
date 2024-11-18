package eu.andret.bot.discord.torphes.command;

import com.google.gson.reflect.TypeToken;
import eu.andret.bot.discord.torphes.entity.Advancement;
import eu.andret.bot.discord.torphes.entity.Answer;
import eu.andret.bot.discord.torphes.entity.Question;
import eu.andret.bot.discord.torphes.util.Requestor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class QuestionCommand extends ListenerAdapter {
	private static final Logger LOGGER = LoggerFactory.getLogger(QuestionCommand.class);
	public static final Random RANDOM = new Random();

	private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
	private final Map<String, Question> messages = new HashMap<>();

	private final Requestor requestor;

	public QuestionCommand(@NotNull final Requestor requestor) {
		this.requestor = requestor;
	}

	@Override
	public void onSlashCommandInteraction(@NotNull final SlashCommandInteractionEvent event) {
		LOGGER.debug("Slash command: QuestionCommand");
		if (!event.getName().equals("question")) {
			return;
		}
		LOGGER.info("Executed command: /question");
		event.deferReply().queue();
		final String url = "https://public.andret.eu/questions.json";
		LOGGER.debug("Requesting URL: {}", url);
		final TypeToken<List<Question>> typeToken = (TypeToken<List<Question>>) TypeToken.getParameterized(List.class, Question.class);
		requestor.executeRequest(url, typeToken)
				.thenAccept(questions -> {
					LOGGER.debug("Response: {}", questions);
					final List<Question> questionList = getQuestion(event, questions);
					final Question question = questionList.get(RANDOM.nextInt(questionList.size()));
					event.getHook().editOriginal("")
							.setEmbeds(getQuestionEmbed(question))
							.setComponents(ActionRow.of(getComponents()))
							.queue(message -> {
								messages.put(message.getId(), question);
								executorService.schedule(() -> messages.remove(message.getId()), 2, TimeUnit.HOURS);
							});
				});
	}

	@Override
	public void onButtonInteraction(@NotNull final ButtonInteractionEvent event) {
		LOGGER.debug("Button clicked");
		final String id = event.getComponentId();
		LOGGER.debug("Button id: {}", id);
		final Question question = messages.get(event.getMessageId());
		LOGGER.debug("Question: {}", question);
		if (question == null) {
			event.deferReply().addEmbeds(new EmbedBuilder().setColor(Color.RED).setDescription("The question has expired.").build()).queue();
			return;
		}
		final Answer answer = question.answers().get(id.charAt(0) - 'A');
		LOGGER.debug("Answer: {}", answer);
		if (event.getButton().getStyle().equals(ButtonStyle.PRIMARY)) {
			event.editComponents(ActionRow.of(getComponents(id, answer.correct()))).queue();
		} else {
			event.deferEdit().queue();
		}
		final String message = String.format(" > %s. %s", id, answer.explanation());
		event.getChannel().sendMessage(message).queue();
	}

	@NotNull
	private static List<Question> getQuestion(@NotNull final SlashCommandInteractionEvent event, @NotNull final List<Question> questions) {
		final String option = event.getOption("advancement", null, OptionMapping::getAsString);
		if (option == null) {
			return questions;
		}
		final Advancement advancement = Arrays.stream(Advancement.values())
				.filter(x -> option.equals(x.name()))
				.findFirst()
				.orElse(null);
		if (advancement == null) {
			return questions;
		}
		return questions.stream()
				.filter(x -> x.advancement().equals(advancement))
				.toList();
	}

	@NotNull
	private static MessageEmbed getQuestionEmbed(@NotNull final Question question) {
		final StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < question.answers().size(); i++) {
			stringBuilder.append((char) ('A' + i))
					.append(". ")
					.append(question.answers().get(i).text())
					.append("\n");
		}
		return new EmbedBuilder()
				.setTitle(String.format("%d. %s", question.id(), question.text()))
				.setDescription(stringBuilder.toString())
				.build();
	}

	@NotNull
	private static List<Button> getComponents() {
		return getComponents(null, false);
	}

	@NotNull
	private static List<Button> getComponents(@Nullable final String selectedId, final boolean correct) {
		if (selectedId == null) {
			return Stream.of("A", "B", "C", "D")
					.map(id -> Button.primary(id, id))
					.toList();
		}
		return Stream.of("A", "B", "C", "D")
				.map(id -> {
					if (!selectedId.equals(id)) {
						return Button.secondary(id, id);
					}
					if (correct) {
						return Button.success(id, id);
					}
					return Button.danger(id, id);
				})
				.toList();
	}
}
