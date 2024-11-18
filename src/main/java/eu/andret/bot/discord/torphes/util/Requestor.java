package eu.andret.bot.discord.torphes.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class Requestor {
	public final Gson gson = new Gson();

	@NotNull
	public <T> CompletableFuture<T> executeRequest(@NotNull final String url, @NotNull final TypeToken<T> typeToken) {
		try (final HttpClient client = HttpClient.newHttpClient()) {
			final HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create(url))
					.build();
			return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
					.thenApply(HttpResponse::body)
					.thenApply(responseBody -> gson.fromJson(responseBody, typeToken));
		}
	}
}
