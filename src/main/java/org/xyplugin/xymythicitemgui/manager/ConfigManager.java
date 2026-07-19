package org.xyplugin.xymythicitemgui.manager;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.inventory.ClickType;
import org.xyplugin.xymythicitemgui.XyMythicItemGui;

public class ConfigManager {

    private static ConfigManager instance;
    private FileConfiguration config;

    public static ConfigManager getInstance() {
        if (instance == null) instance = new ConfigManager();
        return instance;
    }

    public void loadConfig() {
        config = XyMythicItemGui.getInstance().getConfig();
    }

    public String getMenuGuiTitle() {
        return ChatColor.translateAlternateColorCodes('&', config.getString("gui.menu-title", "&6&lXyGui 总菜单"));
    }

    public String getGuiTitle() {
        return ChatColor.translateAlternateColorCodes('&', config.getString("gui.title", "&6&l物品展示"));
    }

    public String getMobGuiTitle() {
        return ChatColor.translateAlternateColorCodes('&', config.getString("gui.mob-title", "&6&l怪物图鉴"));
    }

    public Material getBorderMaterial() {
        String matName = config.getString("gui.border.material", "STAINED_GLASS_PANE");
        Material mat = Material.getMaterial(matName);
        if (mat == null) {
            XyMythicItemGui.getInstance().getLogger().warning("未知边框材质: " + matName + "，使用默认 STAINED_GLASS_PANE");
            return Material.STAINED_GLASS_PANE;
        }
        return mat;
    }

    public short getBorderData() {
        return (short) config.getInt("gui.border.data", 7);
    }

    public String getBorderName() {
        return ChatColor.translateAlternateColorCodes('&', config.getString("gui.border.name", " "));
    }

    public int getItemAmount(ClickType click) {
        String path;
        switch (click) {
            case LEFT: path = "click-amounts.left"; break;
            case RIGHT: path = "click-amounts.right"; break;
            case MIDDLE: path = "click-amounts.middle"; break;
            case SHIFT_LEFT: path = "click-amounts.shift_left"; break;
            case SHIFT_RIGHT: path = "click-amounts.shift_right"; break;
            default: return 0;
        }
        return config.getInt(path, 1);
    }

    public boolean isShowLore() {
        return config.getBoolean("gui.item-display.show-lore", true);
    }

    public int getItemsPerPage() {
        return config.getInt("gui.items-per-page", 28);
    }

    public int getGuiSize() {
        return config.getInt("gui.size", 54);
    }

    public long getSearchTimeout() {
        return config.getLong("search-timeout-seconds", 30);
    }

    public boolean isSearchByDisplayName() {
        return config.getBoolean("search.by-display-name", true);
    }

    public boolean isSearchByInternalName() {
        return config.getBoolean("search.by-internal-name", true);
    }

    public boolean isSearchByLore() {
        return config.getBoolean("search.by-lore", false);
    }

    public boolean isSortEnabled() {
        return config.getBoolean("sort.enabled", true);
    }

    public String getSortBy() {
        return config.getString("sort.by", "display");
    }

    public boolean isSortAscending() {
        String order = config.getString("sort.order", "asc");
        return order.equalsIgnoreCase("asc");
    }

    public boolean isMobSortEnabled() {
        return config.getBoolean("sort.mob.enabled", true);
    }

    public String getMobSortBy() {
        return config.getString("sort.mob.by", "display");
    }

    public boolean isMobSortAscending() {
        String order = config.getString("sort.mob.order", "asc");
        return order.equalsIgnoreCase("asc");
    }
}
