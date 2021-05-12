package com.keurig.fallers.appeal;

import com.keurig.fallers.storage.Config;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class AppealCreateListener extends ListenerAdapter {

    private final String guildId = Config.getInstance().getProperty("guild-id");
    private final String messageId = Config.getInstance().getProperty("appeal-message-id");
    private final String channelId = Config.getInstance().getProperty("appeal-text-channel-id");
    private final String categoryId = Config.getInstance().getProperty("appeal-category-id");

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        JDA jda = event.getJDA();
        jda.getGuildById(guildId)
                .getTextChannelById(channelId)
                .retrieveMessageById(messageId)
                .queue(message -> {
                    message.clearReactions().queue(after -> message.addReaction("\uD83C\uDFE6").queue());
                });
    }

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        Guild guild = event.getJDA().getGuildById(guildId);
        MessageChannel channel = event.getChannel();
        Member member = event.getMember();
        User user = event.getUser();

        if (user.isBot()) return;
        if (!event.getMessageId().equals(messageId)) return;
        if (!event.getReactionEmote().getName().equals("\uD83C\uDFE6")) return;

        event.getReaction().removeReaction(user).queue();

    }

}
