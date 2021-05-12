package com.keurig.fallers.ticket.storage;

import com.google.gson.JsonObject;
import com.keurig.fallers.Bot;
import net.dv8tion.jda.api.entities.Message;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TicketFile {

    public File file;
    private String id;

    private boolean locked = false;

    private long messageId;
    private long channelId;
    private List<Message> messages;

    private String ticketMode = "NONE";

    public TicketFile(String id) {
        messages = new ArrayList<>();
        this.id = id;

        File tickets = new File("tickets");

        if (!tickets.exists()) {
            tickets.mkdir();
        }

        file = new File("tickets/" + id + ".json");
    }

    public void saveTicket() {
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        JsonObject json = new JsonObject();
        json.addProperty("messageId", getMessageId());
        json.addProperty("channelId", getChannelId());
        json.addProperty("ticketMode", getTicketMode());
        json.addProperty("locked", isLocked());

        try {
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(json.toString());
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public long getMessageId() {
        return messageId;
    }

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }

    public String getTicketMode() {
        return ticketMode;
    }

    public void setTicketMode(String ticketMode) {
        this.ticketMode = ticketMode;
    }

    public long getChannelId() {
        return channelId;
    }

    public void setChannelId(long channelId) {
        this.channelId = channelId;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void delete() {
        file.delete();
        TicketFileManager.tickets.remove(Bot.getInstance().ticketManager.getFile(id));
    }

    public void addMessage(Message message) {
        messages.add(message);
    }
}
