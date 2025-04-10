package eu.andret.ads.torphes.entity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public record Question(int id, @NotNull Advancement advancement, @NotNull String category, @NotNull String text,
					   @Nullable String code, @NotNull List<Answer> answers
) {
	public Question {
		Collections.shuffle(answers);
	}

	@NotNull
	public Answer correctAnswer() {
		return answers.stream()
				.filter(Answer::correct)
				.findFirst()
				.orElseThrow();
	}
}
