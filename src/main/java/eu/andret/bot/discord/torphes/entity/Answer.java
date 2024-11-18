package eu.andret.bot.discord.torphes.entity;

import org.jetbrains.annotations.NotNull;

public record Answer(@NotNull String text, boolean correct, @NotNull String explanation) {
}
