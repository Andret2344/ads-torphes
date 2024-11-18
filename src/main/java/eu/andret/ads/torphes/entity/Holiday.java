package eu.andret.ads.torphes.entity;

import org.jetbrains.annotations.NotNull;

public record Holiday(int id, boolean usual, @NotNull String name, @NotNull String description, @NotNull String url) {
}
