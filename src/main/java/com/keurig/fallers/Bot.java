package com.keurig.fallers;

import com.google.gson.Gson;
import com.keurig.fallers.appeal.AppealCreateListener;
import com.keurig.fallers.command.CommandManager;
import com.keurig.fallers.event.CommandListener;
import com.keurig.fallers.storage.Config;
import com.keurig.fallers.ticket.TicketCreateListener;
import com.keurig.fallers.ticket.TicketListener;
import com.keurig.fallers.ticket.storage.TicketFileManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

import javax.security.auth.login.LoginException;
import java.util.EnumSet;

public class Bot {

    private static Bot instance;

    public Gson gson;

    public JDA api;

    private CommandManager commandManager;

    public TicketFileManager ticketManager;

    protected void start() throws LoginException, InterruptedException {

        instance = this;
        gson = new Gson();
        commandManager = new CommandManager();

        bot();

        ticketManager = new TicketFileManager(this);
    }

    private void bot() throws LoginException, InterruptedException {
        api = JDABuilder.createDefault(Config.getInstance().getProperty("token"))
                .addEventListeners(
                        new CommandListener(),
                        new TicketCreateListener(),
                        new TicketListener(),
                        new AppealCreateListener()
                )
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .enableIntents(EnumSet.of(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES))
                .build();
        api.awaitReady();
    }

    public static Bot getInstance() {
        return instance;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }
}
