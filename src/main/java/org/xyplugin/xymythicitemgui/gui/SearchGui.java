package org.xyplugin.xymythicitemgui.gui;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.xyplugin.xymythicitemgui.manager.SearchManager;
import org.xyplugin.xymythicitemgui.manager.SessionManager;

import java.util.List;

public class SearchGui extends BaseGui {

    private static SearchGui instance;

    public static SearchGui getInstance() {
        if (instance == null) instance = new SearchGui();
        return instance;
    }

    public void open(Player player, String keyword, int page) {
        String title = config.getGuiTitle() + " §7- 搜索: " + keyword;
        Inventory inv = createInventory(title);
        drawBorder(inv);
        List<ItemStack> results = SearchManager.getInstance().search(keyword);
        int totalPages = Math.max(1, (int) Math.ceil((double) results.size() / itemsPerPage));
        drawItems(inv, results, page);
        drawPaginationButtons(inv, page, totalPages, keyword);
        drawSearchButton(inv);
        drawCancelButton(inv);
        player.openInventory(inv);
        SessionManager.getInstance().setPage(player, page);
        SessionManager.getInstance().setSearchKeyword(player, keyword);
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

    @Override
    public void open(Player player, int page) {
        MainGui.getInstance().open(player, page);
    }
}
