package org.xyplugin.xymythicitemgui.gui;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.xyplugin.xymythicitemgui.manager.ItemCache;
import org.xyplugin.xymythicitemgui.manager.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class MainGui extends BaseGui {

    private static MainGui instance;

    public static MainGui getInstance() {
        if (instance == null) instance = new MainGui();
        return instance;
    }

    @Override
    public void open(Player player, int page) {
        String title = config.getGuiTitle();
        Inventory inv = createInventory(title);
        drawBorder(inv);
        List<ItemStack> items = getAllDisplayItems();
        drawItems(inv, items, page);
        int totalPages = getTotalPages(items.size());
        drawPaginationButtons(inv, page, totalPages, null);
        drawSearchButton(inv);
        player.openInventory(inv);
        SessionManager.getInstance().setPage(player, page);
        SessionManager.getInstance().setSearchKeyword(player, null);
        SessionManager.getInstance().setGuiType(player, "item");
    }

    private void drawItems(Inventory inv, List<ItemStack> items, int page) {
        int start = page * itemsPerPage;
        int end = Math.min(start + itemsPerPage, items.size());
        int slot = 10;
        for (int i = start; i < end; i++) {
            if (slot % 9 == 8) slot += 2;
            if (slot >= size - 9) break;
            inv.setItem(slot, items.get(i));
            slot++;
        }
    }

    private List<ItemStack> getAllDisplayItems() {
        List<ItemStack> result = new ArrayList<>();
        for (io.lumine.xikage.mythicmobs.items.MythicItem mythicItem : ItemCache.getInstance().getAllItems()) {
            ItemStack item = ItemCache.getInstance().getItemStack(mythicItem.getInternalName());
            if (item == null) continue;
            if (config.isShowLore()) {
                ItemMeta meta = item.getItemMeta();
                List<String> lore = mythicItem.getLore();
                if (meta != null && lore != null && !lore.isEmpty()) {
                    List<String> colored = new ArrayList<>();
                    for (String line : lore) {
                        colored.add(org.bukkit.ChatColor.translateAlternateColorCodes('&', line));
                    }
                    meta.setLore(colored);
                    item.setItemMeta(meta);
                }
            }
            result.add(item);
        }
        return result;
    }

    private int getTotalPages(int total) {
        return Math.max(1, (int) Math.ceil((double) total / itemsPerPage));
    }
}
