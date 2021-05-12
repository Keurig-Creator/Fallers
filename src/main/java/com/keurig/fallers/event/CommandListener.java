package com.keurig.fallers.event;

import com.keurig.fallers.Bot;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class CommandListener extends ListenerAdapter {

    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot() || event.isWebhookMessage()) return;

        Bot.getInstance().getCommandManager().runCommand(event);

    }
}
