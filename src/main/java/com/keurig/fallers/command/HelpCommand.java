package com.keurig.fallers.command;

import com.keurig.fallers.Bot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class HelpCommand implements Command {

    @Override
    public void onCommand(MessageReceivedEvent event, String[] args) {

        Member member = event.getMember();
        Message message = event.getMessage();

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("help - ").append(Bot.getInstance().getCommandManager().getCommand("help").getInfo()).append("\n");

        if (CommandManager.commands.stream().noneMatch(command -> (member.hasPermission(command.getPermissions()) || !Collections.disjoint(member.getRoles(), command.getRoles())) && !command.getName().equals("help"))) {
            EmbedBuilder noCommands = new EmbedBuilder();
            noCommands.setColor(Color.decode("#f5b042"));
            noCommands.setTitle("No Commands");
            noCommands.setDescription("I could not find any commands that you have access to.");

            message.delete().queueAfter(20, TimeUnit.SECONDS);
            message.getChannel().sendMessage(noCommands.build()).queue(msg -> msg.delete().queueAfter(20, TimeUnit.SECONDS));
            return;
        }
        CommandManager.commands.stream().filter(command -> (member.hasPermission(command.getPermissions()) || !Collections.disjoint(member.getRoles(), command.getRoles())) && !command.getName().equals("help"))
                .forEach(command -> stringBuilder
                        .append(command.getName())
                        .append(" - ")
                        .append(Bot.getInstance().getCommandManager().getCommand(command.getName()).getInfo())
                        .append("\n"));

        EmbedBuilder help = new EmbedBuilder();
        help.setColor(Color.decode("#32a852"));
        help.setTitle("Commands");
        help.setDescription(stringBuilder);
        message.delete().queueAfter(20, TimeUnit.SECONDS);
        message.getChannel().sendMessage(help.build()).queue(msg -> msg.delete().queueAfter(20, TimeUnit.SECONDS));
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getInfo() {
        return "Show the list of commands";
    }

}
