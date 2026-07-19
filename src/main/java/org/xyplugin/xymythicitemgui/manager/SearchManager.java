package org.xyplugin.xymythicitemgui.manager;

import io.lumine.xikage.mythicmobs.items.MythicItem;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SearchManager {

    private static SearchManager instance;
    private final ConfigManager config = ConfigManager.getInstance();
    private final ItemCache cache = ItemCache.getInstance();

    public static SearchManager getInstance() {
        if (instance == null) instance = new SearchManager();
        return instance;
    }

    public List<ItemStack> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllItemStacks();
        }
        String lower = keyword.toLowerCase().trim();
        List<MythicItem> matched = cache.getAllItems().stream()
                .filter(item -> matches(item, lower))
                .collect(Collectors.toList());

        List<ItemStack> result = new ArrayList<>();
        for (MythicItem item : matched) {
            ItemStack stack = cache.getItemStack(item.getInternalName());
            if (stack != null) {
                // 添加Lore（如果配置开启）
                if (config.isShowLore()) {
                    // 可以在此添加Lore，但为了保持简洁，略过（主界面已处理）
                }
                result.add(stack);
            }
        }
        return result;
    }

    private boolean matches(MythicItem item, String keyword) {
        if (config.isSearchByInternalName()) {
            if (item.getInternalName().toLowerCase().contains(keyword)) return true;
        }
        if (config.isSearchByDisplayName()) {
            String display = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', item.getDisplayName()));
            if (display.toLowerCase().contains(keyword)) return true;
        }
        if (config.isSearchByLore()) {
            List<String> lore = item.getLore();
            if (lore != null) {
                for (String line : lore) {
                    if (ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', line)).toLowerCase().contains(keyword))
                        return true;
                }
            }
        }
        return false;
    }

    private List<ItemStack> getAllItemStacks() {
        List<ItemStack> stacks = new ArrayList<>();
        for (MythicItem item : cache.getAllItems()) {
            ItemStack stack = cache.getItemStack(item.getInternalName());
            if (stack != null) stacks.add(stack);
        }
        return stacks;
    }
}