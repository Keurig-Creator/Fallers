package com.keurig.fallers.ticket;

import com.keurig.fallers.Bot;
import com.keurig.fallers.storage.Config;
import com.keurig.fallers.ticket.storage.TicketFile;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateNameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class TicketListener extends ListenerAdapter {

    private final String guildId = Config.getInstance().getProperty("guild-id");

    @Override
    public void onUserUpdateName(@NotNull UserUpdateNameEvent event) {
        Guild guild = event.getJDA().getGuildById(guildId);
        User user = event.getUser();

        if (user.isBot()) return;

        TicketFile file = Bot.getInstance().ticketManager.getFile(user.getId());

        if (file == null) return;
        if (guild.getTextChannelById(file.getChannelId()) == null) return;


        guild.getTextChannelById(file.getChannelId()).getManager().setName("ticket-" + event.getNewName()).queue();
    }

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {

        Guild guild = event.getJDA().getGuildById(guildId);
        MessageChannel channel = event.getChannel();
        Member member = event.getMember();
        User user = event.getUser();

        if (user.isBot()) return;

        TicketFile file = Bot.getInstance().ticketManager.getFile(event.getChannel().getIdLong());

        if (file == null) return;
        if (file.getChannelId() != channel.getIdLong()) return;

        long messageId = file.getMessageId();
        String ticketMode = file.getTicketMode();

        event.getReaction().removeReaction(user).queue();

        if (!(member.hasPermission((GuildChannel) channel, Permission.MANAGE_CHANNEL) || member.getRoles().contains(guild.getRoleById(833908418087354368L))))
            return;

        if (!ticketMode.equals("NONE")) {
            if (event.getReactionEmote().getName().equals("❎")) {
                channel.retrieveMessageById(messageId).queue(message -> {
                    message.clearReactions().queue();
                    message.addReaction("\uD83D\uDEAA").queueAfter(1, TimeUnit.MILLISECONDS, reaction -> {
                        if (file.isLocked()) {
                            message.addReaction("\uD83D\uDD13").queue();
                        } else {
                            message.addReaction("\uD83D\uDD12").queue();
                        }
                    });

                    file.setTicketMode("NONE");
                    file.saveTicket();
                });
            }
        }

        if (ticketMode.equals("NONE")) {

            if (event.getReactionEmote().getName().equals("\uD83D\uDEAA")) {
                channel.retrieveMessageById(messageId).queue(message -> {
                    message.removeReaction("\uD83D\uDD12").queue();
                    message.addReaction("✅").queue();
                    message.addReaction("❎").queue();

                    file.setTicketMode("CLOSE");
                    file.saveTicket();

                });

            } else if (event.getReactionEmote().getName().equals("\uD83D\uDD12") && !file.isLocked()) {
                channel.retrieveMessageById(messageId).queue(message -> {
                    message.removeReaction("\uD83D\uDEAA").queue();
                    message.addReaction("✅").queue();
                    message.addReaction("❎").queue();

                    file.setTicketMode("LOCK");
                    file.saveTicket();
                });
            } else if (event.getReactionEmote().getName().equals("\uD83D\uDD13") && file.isLocked()) {
                channel.retrieveMessageById(messageId).queue(message -> {
                    message.removeReaction("\uD83D\uDEAA").queue();
                    message.addReaction("✅").queue();
                    message.addReaction("❎").queue();

                    file.setTicketMode("UNLOCK");
                    file.saveTicket();
                });
            }

        } else if (ticketMode.equals("CLOSE")) {

            EmbedBuilder close = new EmbedBuilder();
            close.setColor(Color.decode("#a83232"));
            close.setTitle("Closing Ticket");
            close.setDescription("Closing ticket in 5 seconds!");

            if (event.getReactionEmote().getName().equals("✅")) {

                channel.retrieveMessageById(messageId).queue(message -> {
                    message.editMessage(close.build()).queue(msg -> {
                        Objects.requireNonNull(event.getJDA().getGuildById(guildId).getTextChannelById(channel.getId())).delete().queueAfter(5, TimeUnit.SECONDS, unused -> {
                            file.delete();
                        });
                    });
                });
            }
        } else if (ticketMode.equals("LOCK")) {
            EmbedBuilder lock = new EmbedBuilder();
            lock.setColor(Color.decode("#fcb603"));
            lock.setTitle("Ticket Locked");
            lock.setDescription("This ticket is now locked!");

            if (!member.hasPermission((GuildChannel) channel, Permission.MANAGE_CHANNEL)) return;

            if (event.getReactionEmote().getName().equals("✅")) {

                channel.retrieveMessageById(messageId).queue(message -> {
                    message.editMessage(lock.build()).queue();
                });

                guild.retrieveMemberById(file.getId()).queue(mem -> {
                    Objects.requireNonNull(guild.getTextChannelById(file.getChannelId())).upsertPermissionOverride(mem).setDeny(Permission.MESSAGE_WRITE).setAllow(Permission.VIEW_CHANNEL).queue();
                });

                channel.retrieveMessageById(messageId).queue(message -> {
                    message.clearReactions().queue();
                    message.addReaction("\uD83D\uDEAA").queueAfter(5, TimeUnit.MILLISECONDS, reaction -> {
                        message.addReaction("\uD83D\uDD13").queue();
                    });

                    file.setTicketMode("NONE");
                    file.setLocked(true);
                    file.saveTicket();
                });
            }
        } else if (ticketMode.equals("UNLOCK")) {
            EmbedBuilder ticketEmbed = new EmbedBuilder();
            ticketEmbed.setColor(Color.decode("#32a852"));
            ticketEmbed.setTitle("Support Ticket");
            ticketEmbed.setDescription(guild.getMemberById(file.getId()).getUser().getName() + " this is your support ticket!");

            if (!member.hasPermission((GuildChannel) channel, Permission.MANAGE_CHANNEL)) return;

            if (event.getReactionEmote().getName().equals("✅")) {

                channel.retrieveMessageById(messageId).queue(message -> {
                    message.editMessage(ticketEmbed.build()).queue();
                });

                guild.retrieveMemberById(file.getId()).queue(mem -> {
                    Objects.requireNonNull(guild.getTextChannelById(file.getChannelId())).upsertPermissionOverride(mem).setAllow(Permission.VIEW_CHANNEL).queue();
                });

                channel.retrieveMessageById(messageId).queue(message -> {
                    message.clearReactions().queue();
                    message.addReaction("\uD83D\uDEAA").queueAfter(5, TimeUnit.MILLISECONDS, reaction -> {
                        message.addReaction("\uD83D\uDD12").queue();
                    });

                    file.setTicketMode("NONE");
                    file.setLocked(false);
                    file.saveTicket();
                });
            }
        }

    }
}
