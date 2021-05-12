package com.keurig.fallers.command;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Collections;
import java.util.List;

public interface Command {

    void onCommand(MessageReceivedEvent event, String[] args);

    String getName();

    String getInfo();

    default List<Permission> getPermissions() {
        return Collections.emptyList();
    }

    default List<Role> getRoles() {
        return Collections.emptyList();
    }

    default List<String> getAliases() {
        return Collections.emptyList();
    }
}
