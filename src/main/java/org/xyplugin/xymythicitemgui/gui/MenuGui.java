package org.xyplugin.xymythicitemgui.gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.xyplugin.xymythicitemgui.manager.SessionManager;
import org.xyplugin.xymythicitemgui.utils.ItemBuilder;

public class MenuGui extends BaseGui {

    private static MenuGui instance;

    public static MenuGui getInstance() {
        if (instance == null) instance = new MenuGui();
        return instance;
    }

    @Override
    public void open(Player player, int page) {
        Inventory inv = createInventory(config.getMenuGuiTitle());
        drawBorder(inv);

        ItemStack itemGui = new ItemBuilder(Material.CHEST)
                .setDisplayName("§a§lMythicMobs 物品")
                .setLore("§7点击打开物品 GUI", "§8命令: /xygui i open")
                .build();
        inv.setItem(21, itemGui);

        ItemStack mobGui = new ItemBuilder(Material.MONSTER_EGG)
                .setDisplayName("§c§lMythicMobs 怪物")
                .setLore("§7点击打开怪物蛋 GUI", "§8命令: /xygui e open")
                .build();
        inv.setItem(23, mobGui);

        player.openInventory(inv);
        SessionManager.getInstance().setPage(player, 0);
        SessionManager.getInstance().setSearchKeyword(player, null);
        SessionManager.getInstance().setGuiType(player, "menu");
    }
}
