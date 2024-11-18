package eu.andret.ads.torphes.entity;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public record Question(int id, @NotNull Advancement advancement, @NotNull String category, @NotNull String text,
					   @NotNull List<Answer> answers
) {
	public Question {
		Collections.shuffle(answers);
	}
}
