package org.xyplugin.xymythicitemgui.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.xyplugin.xymythicitemgui.manager.ConfigManager;
import org.xyplugin.xymythicitemgui.utils.ItemBuilder;

public abstract class BaseGui {

    protected final ConfigManager config = ConfigManager.getInstance();
    protected final int size = config.getGuiSize();
    protected final int itemsPerPage = config.getItemsPerPage();

    public abstract void open(Player player, int page);

    protected Inventory createInventory(String title) {
        return Bukkit.createInventory(null, size, title);
    }

    protected void drawBorder(Inventory inv) {
        Material borderMat = config.getBorderMaterial();
        short data = config.getBorderData();
        String name = config.getBorderName();
        ItemStack border = new ItemBuilder(borderMat, 1, data)
                .setDisplayName(name)
                .build();

        for (int i = 0; i < 9; i++) {
            inv.setItem(i, border);
            inv.setItem(size - 9 + i, border);
        }
        for (int i = 0; i < size; i += 9) {
            inv.setItem(i, border);
            inv.setItem(i + 8, border);
        }
    }

    protected void drawPaginationButtons(Inventory inv, int currentPage, int totalPages, String searchKeyword) {
        if (currentPage > 0) {
            ItemStack prev = new ItemBuilder(Material.ARROW)
                    .setDisplayName("§e上一页")
                    .build();
            inv.setItem(45, prev);
        }
        if (currentPage < totalPages - 1) {
            ItemStack next = new ItemBuilder(Material.ARROW)
                    .setDisplayName("§e下一页")
                    .build();
            inv.setItem(53, next);
        }
    }

    protected void drawSearchButton(Inventory inv) {
        ItemStack search = new ItemBuilder(Material.COMPASS)
                .setDisplayName("§e搜索物品")
                .setLore("§7点击输入关键词搜索")
                .build();
        inv.setItem(49, search);
    }

    protected void drawCancelButton(Inventory inv) {
        ItemStack cancel = new ItemBuilder(Material.BARRIER)
                .setDisplayName("§c返回主界面")
                .setLore("§7点击返回")
                .build();
        inv.setItem(47, cancel);
    }
}