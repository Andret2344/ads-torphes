package eu.andret.torphes.command;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.async.Callback;
import com.mashape.unirest.http.exceptions.UnirestException;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.ChannelType;

import java.awt.Color;

public class CatCommand extends Command {
	public CatCommand() {
		name = "cat";
		help = "shows a random cat";
		botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
		guildOnly = false;
	}

	@Override
	protected void execute(CommandEvent event) {
		// use Unirest to poll an API
		Unirest.get("http://aws.random.cat/meow").asJsonAsync(new Callback<JsonNode>() {

			// The API call was successful
			@Override
			public void completed(HttpResponse<JsonNode> hr) {
				event.reply(new EmbedBuilder()
						.setColor(event.isFromType(ChannelType.TEXT) ? event.getSelfMember().getColor() : Color.GREEN)
						.setImage(hr.getBody().getObject().getString("file"))
						.build());
			}

			// The API call failed
			@Override
			public void failed(UnirestException ue) {
				event.reactError();
			}

			// The API call was cancelled (this should never happen)
			@Override
			public void cancelled() {
				event.reactError();
			}
		});
	}
}