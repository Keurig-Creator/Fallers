package com.keurig.fallers.command;

import com.keurig.fallers.Bot;
import com.keurig.fallers.storage.Config;
import com.keurig.fallers.ticket.storage.TicketFile;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class CloseTicket extends ListenerAdapter implements Command {

    private JDA jda = Bot.getInstance().api;
    private TicketFile ticketConfig;
    private Message message;

    @Override
    public void onCommand(MessageReceivedEvent event, String[] args) {
        MessageChannel channel = event.getChannel();
        Member member = event.getMember();
        Message message = event.getMessage();

        message.delete().queue();

        EmbedBuilder close = new EmbedBuilder();
        close.setColor(Color.decode("#a83232"));
        close.setTitle("Close Ticket");
        close.setDescription(member.getUser().getName() + " Are you sure you want to close this ticket?");

        ticketConfig = Bot.getInstance().ticketManager.getFile(channel.getIdLong());
        if (ticketConfig.file.exists()) {
            String[] split = channel.getName().split("-");

            if (split[0].equals("ticket")) {
                channel.sendMessage(close.build()).queue(msg -> {
                    msg.addReaction("✅").queue();
                    msg.addReaction("❎").queue();

                    channel.getJDA().addEventListener(this);
                    this.message = msg;
                });
            }
        }
    }

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        MessageChannel channel = event.getChannel();
        Member member = event.getMember();

        EmbedBuilder close = new EmbedBuilder();
        close.setColor(Color.decode("#a83232"));
        close.setTitle("Closing Ticket");
        close.setDescription("Closing ticket in 5 seconds");

        if (event.getUser().isBot()) return;

        if (!event.getChannel().equals(channel)) return;
        if (!event.getMessageId().equals(message.getId())) return;

        if (event.getReactionEmote().getName().equals("✅")) {
            message.editMessage(close.build()).queue(message -> {
                Objects.requireNonNull(event.getJDA().getGuildById(Config.getInstance().getProperty("guild-id")).getTextChannelById(channel.getId())).delete().queueAfter(5, TimeUnit.SECONDS, unused -> {
                    event.getJDA().removeEventListener(this);
                    ticketConfig.delete();
                });
            });
        } else if (event.getReactionEmote().getName().equals("❎")) {
            message.delete().queue();
        }
    }

    @Override
    public String getName() {
        return "close";
    }

    @Override
    public String getInfo() {
        return "Closes a open ticket";
    }

    @Override
    public List<Role> getRoles() {
        JDA jda = Bot.getInstance().api;
        return Arrays.asList(jda.getRoleById(833908418087354368L), jda.getRoleById(833908418087354369L), jda.getRoleById(834236400597205022L));
    }

    @Override
    public List<Permission> getPermissions() {
        return Arrays.asList(Permission.MANAGE_CHANNEL);
    }
}
