package com.keurig.fallers.ticket.storage;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.keurig.fallers.Bot;
import com.keurig.fallers.storage.Config;
import net.dv8tion.jda.api.entities.Guild;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileReader;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class TicketFileManager {

    private Bot bot;

    public static Set<TicketFile> tickets;

    private final String guildId = Config.getInstance().getProperty("guild-id");

    public TicketFileManager(Bot bot) {
        this.bot = bot;
        tickets = new HashSet<>();

        Guild guild = bot.api.getGuildById(guildId);

        File path = new File("tickets");
        Collection<File> files = FileUtils.listFiles(path, new String[]{"json"}, true);

        files.forEach(file -> {
            try {
                String memberId = file.getName().substring(0, file.getName().lastIndexOf('.'));
                TicketFile ticketFile = new TicketFile(memberId);

                FileReader fileReader = new FileReader(file);

                JsonObject obj = JsonParser.parseReader(fileReader).getAsJsonObject();
                ticketFile.setMessageId(obj.get("messageId").getAsLong());
                ticketFile.setChannelId(obj.get("channelId").getAsLong());
                ticketFile.setTicketMode(obj.get("ticketMode").getAsString());
                ticketFile.setLocked(obj.get("locked").getAsBoolean());

                fileReader.close();

                if (guild.getTextChannelById(ticketFile.getChannelId()) == null) {
                    file.delete();
                    System.out.println("Removed " + file.getName());
                    return;
                }

                tickets.add(ticketFile);

                guild.retrieveMemberById(memberId).queue(member -> System.out.println("Loaded ticket-" + member.getUser().getName().toLowerCase()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public TicketFile getFile(long channelId) {
        for (TicketFile ticket : tickets) {
            if (ticket.getChannelId() == channelId) {
                return ticket;
            }
        }

        return null;
    }

    public TicketFile getFile(String userId) {
        for (TicketFile ticket : tickets) {
            String memberId = ticket.file.getName().substring(0, ticket.file.getName().lastIndexOf('.'));
            if (memberId.equals(userId)) {
                return ticket;
            }
        }

        return null;
    }
}
