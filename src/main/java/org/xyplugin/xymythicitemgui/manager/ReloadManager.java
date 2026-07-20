package org.xyplugin.xymythicitemgui.manager;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.xyplugin.xymythicitemgui.XyMythicItemGui;
import org.xyplugin.xymythicitemgui.utils.MessageUtil;

public class ReloadManager {

    private static ReloadManager instance;
    private long ignoreMythicReloadHookUntil;

    public static ReloadManager getInstance() {
        if (instance == null) instance = new ReloadManager();
        return instance;
    }

    public void reloadXyGui(CommandSender sender) {
        XyMythicItemGui plugin = XyMythicItemGui.getInstance();
        plugin.reloadConfig();
        ConfigManager.getInstance().loadConfig();
        ItemCache.getInstance().reload();
        MobManager.getInstance().reload();
        MessageManager.getInstance().reload();

        if (sender != null) {
            MessageUtil.send(sender, "reload-success");
        }
    }

    public void reloadMythicThenXyGui(CommandSender sender) {
        XyMythicItemGui plugin = XyMythicItemGui.getInstance();
        ignoreMythicReloadHookUntil = System.currentTimeMillis() + 5000L;
        MessageUtil.send(sender, "reload-mythic-start");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mm reload");
        Bukkit.getScheduler().runTaskLater(plugin, () -> reloadXyGui(sender), 20L);
    }

    public void scheduleXyGuiReloadAfterMythic(CommandSender sender) {
        if (System.currentTimeMillis() < ignoreMythicReloadHookUntil) return;
        XyMythicItemGui plugin = XyMythicItemGui.getInstance();
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            reloadXyGui(sender);
            if (sender != null) {
                MessageUtil.send(sender, "reload-synced-after-mythic");
            }
        }, 20L);
    }
}
