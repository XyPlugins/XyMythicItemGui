package org.xyplugin.xymythicitemgui.manager;

import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter;
import io.lumine.xikage.mythicmobs.items.MythicItem;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.xyplugin.xymythicitemgui.XyMythicItemGui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class ItemCache {

    private static ItemCache instance;
    private List<MythicItem> mythicItems = new ArrayList<>();

    public static ItemCache getInstance() {
        if (instance == null) instance = new ItemCache();
        return instance;
    }

    /**
     * 重新加载 MythicMobs 物品列表，并排序
     */
    public void reload() {
        mythicItems.clear();
        mythicItems.addAll(MythicMobs.inst().getItemManager().getItems());
        sortItems();  // 排序
        int count = mythicItems.size();
        XyMythicItemGui.getInstance().getLogger().info("[MM物品库] 此处加载了 " + count + " 个物品");
    }

    /**
     * 根据配置对物品列表排序（按显示名或内部名，升序/降序）
     */
    private void sortItems() {
        ConfigManager config = ConfigManager.getInstance();
        if (!config.isSortEnabled()) return;

        String by = config.getSortBy();
        boolean asc = config.isSortAscending();

        Comparator<MythicItem> comparator;
        if ("internal".equalsIgnoreCase(by)) {
            comparator = Comparator.comparing(
                    MythicItem::getInternalName,
                    String.CASE_INSENSITIVE_ORDER
            );
        } else { // 默认按显示名
            comparator = Comparator.comparing(
                    item -> {
                        String display = item.getDisplayName();
                        if (display == null) return "";
                        // 移除颜色代码，仅比较纯文本
                        return ChatColor.stripColor(
                                org.bukkit.ChatColor.translateAlternateColorCodes('&', display)
                        );
                    },
                    String.CASE_INSENSITIVE_ORDER
            );
        }

        if (!asc) {
            comparator = comparator.reversed();
        }

        Collections.sort(mythicItems, comparator);
    }

    public List<MythicItem> getAllItems() {
        return new ArrayList<>(mythicItems);
    }

    public ItemStack getItemStack(String internalName) {
        Optional<MythicItem> opt = MythicMobs.inst().getItemManager().getItem(internalName);
        return opt.map(item -> BukkitAdapter.adapt(item.generateItemStack(1))).orElse(null);
    }

    public MythicItem getItem(String internalName) {
        return MythicMobs.inst().getItemManager().getItem(internalName).orElse(null);
    }
}