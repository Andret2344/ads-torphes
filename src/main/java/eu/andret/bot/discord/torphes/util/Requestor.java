package eu.andret.bot.discord.torphes.util;

import com.google.gson.Gson;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class Requestor {
	public final Gson gson = new Gson();

	@NotNull
	public <T> CompletableFuture<T> executeRequest(@NotNull final String url, @NotNull final Class<T> targetClass) {
		try (final HttpClient client = HttpClient.newHttpClient()) {
			final HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create(url))
					.build();
			return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
					.thenApply(HttpResponse::body)
					.thenApply(responseBody -> gson.fromJson(responseBody, targetClass));
		}
	}
}
