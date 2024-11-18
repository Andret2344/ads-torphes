package eu.andret.ads.torphes.guild;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class GuildsCountListener extends ListenerAdapter {
	@Override
	public void onGuildJoin(@NotNull final GuildJoinEvent event) {
		setPresence(event);
	}

	@Override
	public void onGuildLeave(@NotNull final GuildLeaveEvent event) {
		setPresence(event);
	}

	@Override
	public void onGuildReady(@NotNull final GuildReadyEvent event) {
		setPresence(event);
	}

	private void setPresence(@NotNull final Event event) {
		final JDA jda = event.getJDA();
		final Activity activity = Activity.listening(String.format("%d servers!", jda.getGuildCache().size()));
		jda.getPresence().setPresence(activity, false);
	}
}
