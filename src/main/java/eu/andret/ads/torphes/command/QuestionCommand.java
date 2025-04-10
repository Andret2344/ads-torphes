package eu.andret.ads.torphes.command;

import com.google.gson.reflect.TypeToken;
import eu.andret.ads.torphes.entity.Advancement;
import eu.andret.ads.torphes.entity.Answer;
import eu.andret.ads.torphes.entity.Question;
import eu.andret.ads.torphes.util.Requestor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class QuestionCommand extends ListenerAdapter {
	private static final Logger LOGGER = LoggerFactory.getLogger(QuestionCommand.class);
	private static final Random RANDOM = new Random();
	private static final String REPORT = "report";
	private static final String DESCRIPTION = "description";
	private static final String QUESTION = "question";
	private static final String USER_ID = "185829743134769153";

	private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
	private final Map<String, Question> messages = new HashMap<>();

	private final JDA jda;
	private final Requestor requestor;

	public QuestionCommand(@NotNull final JDA jda, @NotNull final Requestor requestor) {
		this.jda = jda;
		this.requestor = requestor;
	}

	@Override
	public void onSlashCommandInteraction(@NotNull final SlashCommandInteractionEvent event) {
		LOGGER.debug("Slash command: QuestionCommand");
		if (!event.getName().equals(QUESTION)) {
			return;
		}
		LOGGER.info("Executed command: /question");
		event.deferReply().queue();
		final String url = "https://public.andret.eu/questions.json";
		LOGGER.debug("Requesting URL: {}", url);
		final TypeToken<List<Question>> typeToken = (TypeToken<List<Question>>) TypeToken.getParameterized(List.class, Question.class);
		requestor.executeRequest(url, typeToken)
				.thenAccept(questions -> {
					LOGGER.debug("Response: {} questions", questions.size());
					final String advancement = event.getOption("advancement", null, OptionMapping::getAsString);
					final String category = event.getOption("category", null, OptionMapping::getAsString);
					LOGGER.debug("advancement: {}, category: {}", advancement, category);
					final List<Question> questionList = getQuestion(questions, advancement, category);
					LOGGER.debug("Filtered: {} questions", questionList.size());
					final Question question = questionList.get(RANDOM.nextInt(questionList.size()));
					event.getHook().editOriginal("")
							.setEmbeds(getQuestionEmbed(question))
							.setComponents(ActionRow.of(getComponents()))
							.queue(message -> {
								messages.put(message.getId(), question);
								executorService.schedule(() -> {
									event.getHook()
											.editOriginal("")
											.setComponents()
											.queue();
									messages.remove(message.getId());
								}, 2, TimeUnit.HOURS);
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
		if (id.equals(REPORT)) {
			LOGGER.debug("Reporting question: {}", question.id());
			final TextInput descriptionInput = TextInput.create(DESCRIPTION, "Description", TextInputStyle.PARAGRAPH)
					.setRequired(false)
					.setPlaceholder("What's wrong with the question?")
					.setMaxLength(1000)
					.build();
			final TextInput questionInput = TextInput.create(QUESTION, "Question (DO NOT MODIFY)", TextInputStyle.SHORT)
					.setRequired(true)
					.setValue(String.valueOf(question.id()))
					.setPlaceholder("And why did you remove it?")
					.setMaxLength(3)
					.build();

			final Modal modal = Modal.create(REPORT, "Question report")
					.addComponents(ActionRow.of(descriptionInput), ActionRow.of(questionInput))
					.build();

			event.replyModal(modal).queue();
			return;
		}
		final Answer answer = question.answers().get(id.charAt(0) - 'A');
		LOGGER.debug("Clicked answer: {}", answer);
		if (event.getButton().getStyle().equals(ButtonStyle.PRIMARY)) {
			event.editComponents(
							ActionRow.of(getComponents(id, String.valueOf((char) ('A' + question.answers().indexOf(question.correctAnswer()))))),
							ActionRow.of(Button.danger(REPORT, "Report")))
					.queue();
		} else {
			event.deferEdit().queue();
		}
		final String message = String.format(" > %s. %s", id, answer.explanation());
		event.getChannel().sendMessage(message).queue();
	}

	@NotNull
	private static List<Question> getQuestion(@NotNull final List<Question> questions, @Nullable final String advancementOption, @Nullable final String categoryOption) {
		final Advancement advancement = Arrays.stream(Advancement.values())
				.filter(adv -> adv.name().equalsIgnoreCase(advancementOption))
				.findFirst()
				.orElse(null);
		LOGGER.debug("Decoded advancement: {}", advancement);
		return questions.stream()
				.filter(question -> advancement == null || question.advancement().equals(advancement)
						&& categoryOption == null || question.category().equalsIgnoreCase(categoryOption))
				.toList();
	}

	@NotNull
	private static MessageEmbed getQuestionEmbed(@NotNull final Question question) {
		final StringBuilder stringBuilder = new StringBuilder();
		if (question.code() != null) {
			stringBuilder.append("```java\n")
					.append(question.code())
					.append("\n```\n");
		}
		for (int i = 0; i < question.answers().size(); i++) {
			stringBuilder.append("* ")
					.append((char) ('A' + i))
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
		return getComponents(null, null);
	}

	@NotNull
	private static List<Button> getComponents(@Nullable final String selectedId, @Nullable final String correct) {
		if (selectedId == null) {
			return Stream.of("A", "B", "C", "D")
					.map(id -> Button.primary(id, id))
					.toList();
		}
		return Stream.of("A", "B", "C", "D")
				.map(id -> {
					if (id.equals(correct)) {
						return Button.success(id, id);
					}
					if (id.equals(selectedId)) {
						return Button.danger(id, id);
					}
					return Button.secondary(id, id);
				})
				.toList();
	}

	@Override
	public void onCommandAutoCompleteInteraction(@NotNull final CommandAutoCompleteInteractionEvent event) {
		if (event.getName().equals(QUESTION)) {
			event.replyChoices(getQuestionAutoComplete(event)).queue();
		}
	}

	@NotNull
	private List<Command.Choice> getQuestionAutoComplete(@NotNull final CommandAutoCompleteInteractionEvent event) {
		if (event.getFocusedOption().getName().equals("advancement")) {
			return Stream.of(Advancement.values())
					.map(Enum::name)
					.filter(word -> word.toUpperCase(Locale.ROOT).startsWith(event.getFocusedOption().getValue().toUpperCase(Locale.ROOT)))
					.map(word -> new Command.Choice(word, word))
					.toList();
		}
		if (event.getFocusedOption().getName().equals("category")) {
			return Stream.of("General", "Design patterns", "Java language", "Java software", "Spring")
					.filter(word -> word.toUpperCase(Locale.ROOT).startsWith(event.getFocusedOption().getValue().toUpperCase(Locale.ROOT)))
					.map(word -> new Command.Choice(word, word))
					.toList();
		}
		return Collections.emptyList();
	}

	@Override
	public void onModalInteraction(@NotNull final ModalInteractionEvent event) {
		LOGGER.debug("Modal submitted for id: {}", event.getId());
		if (event.getModalId().equals(REPORT)) {
			final String question = Optional.ofNullable(event.getValue(QUESTION)).map(ModalMapping::getAsString).orElse(null);
			final List<MessageEmbed> description = Optional.ofNullable(event.getValue(DESCRIPTION))
					.map(ModalMapping::getAsString)
					.filter(Predicate.not(String::isBlank))
					.map(string -> new EmbedBuilder().setDescription(string).build())
					.stream()
					.toList();

			jda.openPrivateChannelById(USER_ID)
					.complete()
					.sendMessage(String.format("Zgłoszone pytanie o id: `%s`", question))
					.addEmbeds(description)
					.queue();

			event.reply("Zgłoszenie zostało wysłane").setEphemeral(true).queue();
		}
	}
}
