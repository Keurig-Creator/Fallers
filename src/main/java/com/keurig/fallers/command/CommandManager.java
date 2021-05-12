package com.keurig.fallers.command;

import com.keurig.fallers.storage.Config;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class CommandManager {

    public static Set<Command> commands;

    public CommandManager() {
        System.out.println("Initiating CommandManager");
        commands = new HashSet<>();
        commands.add(new HelpCommand());
        commands.add(new CloseTicket());
        commands.add(new AddCommand());
        commands.add(new RemoveCommand());
    }

    public void runCommand(MessageReceivedEvent event) {
        Message message = event.getMessage();
        Member member = event.getMember();
        String content = message.getContentRaw();

        if (content.equals("")) return;

        if (!content.startsWith(Config.getInstance().getProperty("prefix"))) return;

        content = content.substring(Config.getInstance().getProperty("prefix").length());

        String[] split = content.split(" ");

        String label = split[0];

        String[] args = Arrays.copyOfRange(split, 1, split.length);

        Command command = getCommand(label);

        if (command == null) {
            EmbedBuilder noCommand = new EmbedBuilder();
            noCommand.setTitle("Error");
            noCommand.setColor(Color.decode("#a83232"));
            noCommand.setDescription(member.getAsMention() + " Command " + label.toLowerCase() + " does not exist. Type !help for the list of available commands.");
            message.delete().queueAfter(10, TimeUnit.SECONDS);
            message.getChannel().sendMessage(noCommand.build()).queue(msg -> msg.delete().queueAfter(10, TimeUnit.SECONDS));
            return;
        }

        System.out.println(member.hasPermission(command.getPermissions()));
        System.out.println(!Collections.disjoint(member.getRoles(), command.getRoles()));
        if (!(!Collections.disjoint(member.getRoles(), command.getRoles()) || member.hasPermission(command.getPermissions()))) {
            EmbedBuilder noPermission = new EmbedBuilder();
            noPermission.setTitle("Error");
            noPermission.setColor(Color.decode("#a83232"));
            noPermission.setDescription(member.getAsMention() + " You do not have permission to use " + command.getName() + " command.");

            message.delete().queueAfter(10, TimeUnit.SECONDS);
            message.getChannel().sendMessage(noPermission.build()).queue(msg -> msg.delete().queueAfter(10, TimeUnit.SECONDS));
            return;
        }
        command.onCommand(event, args);
    }

    public Command getCommand(String label) {
        for (Command command : commands) {
            if (command.getName().equals(label) || command.getAliases().contains(label)) {
                return command;
            }
        }

        return null;
    }
}
