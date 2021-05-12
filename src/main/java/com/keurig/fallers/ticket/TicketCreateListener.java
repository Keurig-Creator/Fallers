package com.keurig.fallers.ticket;

import com.keurig.fallers.storage.Config;
import com.keurig.fallers.ticket.storage.TicketFile;
import com.keurig.fallers.ticket.storage.TicketFileManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.EnumSet;
import java.util.concurrent.TimeUnit;

public class TicketCreateListener extends ListenerAdapter {

    private final String guildId = Config.getInstance().getProperty("guild-id");
    private final String messageId = Config.getInstance().getProperty("message-id");
    private final String channelId = Config.getInstance().getProperty("text-channel-id");
    private final String categoryId = Config.getInstance().getProperty("category-id");

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        JDA jda = event.getJDA();
        jda.getGuildById(guildId)
                .getTextChannelById(channelId)
                .retrieveMessageById(messageId)
                .queue(message -> {
                    message.clearReactions().queue(after -> {
                        message.addReaction("\uD83C\uDFAB").queue();
                    });
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
        if (!event.getReactionEmote().getName().equals("\uD83C\uDFAB")) return;
        
        event.getReaction().removeReaction(user).queue();

        TicketFile ticketConfig = new TicketFile(event.getUserId());

        EmbedBuilder ticketEmbed = new EmbedBuilder();
        ticketEmbed.setColor(Color.decode("#32a852"));
        ticketEmbed.setTitle("Support Ticket");
        ticketEmbed.setDescription(user.getName() + " this is your support ticket!");

        if (!ticketConfig.file.exists()) {
            guild.createTextChannel("ticket-" + event.getUser().getName(), guild.getCategoryById(categoryId))
                    .addPermissionOverride(member, EnumSet.of(Permission.VIEW_CHANNEL), null)
                    .addPermissionOverride(guild.getRoleById(833908418078048288L), EnumSet.of(Permission.VIEW_CHANNEL), null)
                    .addPermissionOverride(guild.getPublicRole(), EnumSet.of(Permission.NICKNAME_CHANGE), EnumSet.of(Permission.VIEW_CHANNEL))
                    .queue(textChannel -> {
                        textChannel.sendMessage(member.getAsMention()).queue(message -> message.delete().queueAfter(1, TimeUnit.MILLISECONDS));
                        textChannel.sendMessage(ticketEmbed.build()).queue(message -> {
                            message.addReaction("\uD83D\uDEAA").queue();
                            message.addReaction("\uD83D\uDD12").queue();

                            ticketConfig.setMessageId(message.getIdLong());
                            ticketConfig.setChannelId(textChannel.getIdLong());
                            ticketConfig.saveTicket();

                            TicketFileManager.tickets.add(ticketConfig);

                            System.out.println("Created " + textChannel.getName());
                        });
                    });

        } else {
            channel.sendMessage(member.getAsMention() + " You already have a ticket open.").queue(message -> message.delete().queueAfter(5, TimeUnit.SECONDS));
        }

    }
}
