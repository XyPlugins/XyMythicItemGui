package org.xyplugin.xymythicitemgui.listener;

import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.xyplugin.xymythicitemgui.manager.ReloadManager;

import java.util.Locale;

public class MythicReloadListener implements Listener {

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        if (isMythicReloadCommand(event.getMessage())) {
            ReloadManager.getInstance().scheduleXyGuiReloadAfterMythic(event.getPlayer());
        }
    }

    @EventHandler
    public void onServerCommand(ServerCommandEvent event) {
        if (isMythicReloadCommand(event.getCommand())) {
            CommandSender sender = event.getSender();
            ReloadManager.getInstance().scheduleXyGuiReloadAfterMythic(sender);
        }
    }

    private boolean isMythicReloadCommand(String command) {
        if (command == null) return false;
        String normalized = command.trim();
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1).trim();
        }
        normalized = normalized.replaceAll("\\s+", " ").toLowerCase(Locale.ENGLISH);
        return normalized.equals("mm reload")
                || normalized.startsWith("mm reload ")
                || normalized.equals("mythicmobs reload")
                || normalized.startsWith("mythicmobs reload ");
    }
}
