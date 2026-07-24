package org.xyplugin.xymythicitemgui;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.xyplugin.xymythicitemgui.command.MainCommand;
import org.xyplugin.xymythicitemgui.listener.GuiListener;
import org.xyplugin.xymythicitemgui.listener.ItemUpdateListener;
import org.xyplugin.xymythicitemgui.listener.MythicReloadListener;
import org.xyplugin.xymythicitemgui.manager.ConfigManager;
import org.xyplugin.xymythicitemgui.manager.ItemCache;
import org.xyplugin.xymythicitemgui.manager.ItemUpdateManager;
import org.xyplugin.xymythicitemgui.manager.MessageManager;
import org.xyplugin.xymythicitemgui.manager.MobManager;
import org.xyplugin.xymythicitemgui.manager.SessionManager;

public class XyMythicItemGui extends JavaPlugin {

    private static XyMythicItemGui instance;

    @Override
    public void onEnable() {
        instance = this;

        if (Bukkit.getPluginManager().getPlugin("MythicMobs") == null) {
            getLogger().severe("未找到 MythicMobs！插件已禁用。");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        saveDefaultConfig();
        ConfigManager.getInstance().loadConfig();
        MessageManager.getInstance().load();

        ItemCache.getInstance().reload();
        MobManager.getInstance().reload();
        ItemUpdateManager.getInstance().reload();

        getCommand("xygui").setExecutor(new MainCommand());
        Bukkit.getPluginManager().registerEvents(new GuiListener(), this);
        Bukkit.getPluginManager().registerEvents(new ItemUpdateListener(), this);
        Bukkit.getPluginManager().registerEvents(new MythicReloadListener(), this);

        Bukkit.getScheduler().runTaskTimerAsynchronously(this,
                () -> SessionManager.getInstance().cleanExpiredSessions(), 0L, 600L);

        getLogger().info("XyMythicItemGui 已启用！");
    }

    @Override
    public void onDisable() {
        SessionManager.getInstance().clearAll();
        getLogger().info("XyMythicItemGui 已禁用。");
    }

    public static XyMythicItemGui getInstance() {
        return instance;
    }
}
