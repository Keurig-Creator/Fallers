package com.keurig.fallers.command;

import com.keurig.fallers.Bot;
import com.keurig.fallers.ticket.storage.TicketFile;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class AddCommand implements Command {

    public MessageReceivedEvent event;

    @Override
    public void onCommand(MessageReceivedEvent event, String[] args) {

        Guild guild = event.getGuild();
        MessageChannel channel = event.getChannel();
        Message message = event.getMessage();
        User user = event.getMember().getUser();

        if (user.isBot()) return;

        TicketFile file = Bot.getInstance().ticketManager.getFile(channel.getIdLong());

        if (file == null) return;
        if (file.getChannelId() != channel.getIdLong()) return;

        long messageId = file.getMessageId();

        channel.sendMessage("ok").queue();

        if (args.length == 1) {
            System.out.println(args[0]);
            Member memberById = message.getMentionedMembers().get(0);

            if (memberById.getId().equals(file.getId())) {
                channel.sendMessage("You cannot add this person").queue();
                return;
            }

            Objects.requireNonNull(guild.getTextChannelById(file.getChannelId())).upsertPermissionOverride(memberById).setAllow(Permission.MESSAGE_WRITE, Permission.VIEW_CHANNEL).queue();

            channel.sendMessage("added user to ticket ").queue();
        }
    }

    @Override
    public String getName() {
        return "add";
    }

    @Override
    public String getInfo() {
        return "Adds a user to a ticket";
    }

    @Override
    public List<Role> getRoles() {
        JDA jda = Bot.getInstance().api;
        return Arrays.asList(jda.getRoleById(833908418078048288L));
    }

    @Override
    public List<Permission> getPermissions() {
        return Arrays.asList(Permission.MANAGE_CHANNEL);
    }
}
