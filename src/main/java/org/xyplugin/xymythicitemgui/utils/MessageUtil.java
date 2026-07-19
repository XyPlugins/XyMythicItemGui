package org.xyplugin.xymythicitemgui.utils;

import org.bukkit.entity.Player;
import org.xyplugin.xymythicitemgui.manager.MessageManager;

public class MessageUtil {

    public static void send(Player player, String key, String... replacements) {
        String msg = MessageManager.getInstance().getMessage(key);
        if (msg == null || msg.isEmpty()) return;
        for (int i = 0; i < replacements.length; i += 2) {
            msg = msg.replace(replacements[i], replacements[i + 1]);
        }
        player.sendMessage(MessageManager.getInstance().getPrefix() + msg);
    }
}