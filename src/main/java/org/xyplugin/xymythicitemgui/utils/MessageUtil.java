package org.xyplugin.xymythicitemgui.utils;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.xyplugin.xymythicitemgui.manager.MessageManager;

public class MessageUtil {

    public static void send(CommandSender sender, String key, String... replacements) {
        String msg = MessageManager.getInstance().getMessage(key);
        if (msg == null || msg.isEmpty()) return;
        for (int i = 0; i < replacements.length - 1; i += 2) {
            msg = msg.replace(replacements[i], replacements[i + 1]);
        }
        sender.sendMessage(MessageManager.getInstance().getPrefix() + msg);
    }

    public static void send(Player player, String key, String... replacements) {
        send((CommandSender) player, key, replacements);
    }

    public static void sendRaw(CommandSender sender, String message) {
        if (message == null || message.isEmpty()) return;
        sender.sendMessage(MessageManager.getInstance().getPrefix()
                + ChatColor.translateAlternateColorCodes('&', message));
    }
}
