package org.xyplugin.xymythicitemgui.manager;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.xyplugin.xymythicitemgui.XyMythicItemGui;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class MessageManager {

    private static MessageManager instance;
    private File messageFile;
    private FileConfiguration messageConfig;
    private String prefix;
    private final Map<String, String> messages = new HashMap<>();

    public static MessageManager getInstance() {
        if (instance == null) instance = new MessageManager();
        return instance;
    }

    public void load() {
        XyMythicItemGui plugin = XyMythicItemGui.getInstance();
        messageFile = new File(plugin.getDataFolder(), "Message.yml");

        if (!messageFile.exists()) {
            plugin.saveResource("Message.yml", false);
        }

        messageConfig = YamlConfiguration.loadConfiguration(messageFile);
        YamlConfiguration defConfig = null;
        InputStream defStream = plugin.getResource("Message.yml");
        if (defStream != null) {
            defConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defStream, StandardCharsets.UTF_8));
            messageConfig.setDefaults(defConfig);
        }

        prefix = ChatColor.translateAlternateColorCodes('&', messageConfig.getString("prefix", "&7[&bXyGui&7] "));

        messages.clear();
        if (defConfig != null) {
            loadMessages(defConfig);
        }
        loadMessages(messageConfig);
    }

    private void loadMessages(FileConfiguration source) {
        ConfigurationSection section = source.getConfigurationSection("messages");
        if (section == null) return;
        for (String key : section.getKeys(false)) {
            String value = source.getString("messages." + key);
            if (value != null) {
                messages.put(key, ChatColor.translateAlternateColorCodes('&', value));
            }
        }
    }

    public void reload() {
        load();
        XyMythicItemGui.getInstance().getLogger().info("Message.yml 已重载");
    }

    public String getPrefix() {
        return prefix;
    }

    public String getMessage(String key) {
        return messages.getOrDefault(key, "§c未知消息: " + key);
    }
}
