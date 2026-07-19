package org.xyplugin.xymythicitemgui.gui;

import io.lumine.xikage.mythicmobs.mobs.MythicMob;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.xyplugin.xymythicitemgui.manager.MobManager;
import org.xyplugin.xymythicitemgui.manager.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class MobGui extends BaseGui {

    private static MobGui instance;

    public static MobGui getInstance() {
        if (instance == null) instance = new MobGui();
        return instance;
    }

    @Override
    public void open(Player player, int page) {
        List<ItemStack> mobItems = getAllMobDisplayItems();
        int totalPages = Math.max(1, (int) Math.ceil((double) mobItems.size() / itemsPerPage));
        int safePage = Math.max(0, Math.min(page, totalPages - 1));

        Inventory inv = createInventory(config.getMobGuiTitle());
        drawBorder(inv);
        drawMobItems(inv, mobItems, safePage);
        drawPaginationButtons(inv, safePage, totalPages, null);
        player.openInventory(inv);
        SessionManager.getInstance().setPage(player, safePage);
        SessionManager.getInstance().setSearchKeyword(player, null);
        SessionManager.getInstance().setGuiType(player, "mob");
    }

    private void drawMobItems(Inventory inv, List<ItemStack> mobItems, int page) {
        int start = page * itemsPerPage;
        int end = Math.min(start + itemsPerPage, mobItems.size());
        int slot = 10;
        for (int i = start; i < end; i++) {
            if (slot % 9 == 8) slot += 2;
            if (slot >= size - 9) break;
            inv.setItem(slot, mobItems.get(i));
            slot++;
        }
    }

    private List<ItemStack> getAllMobDisplayItems() {
        List<ItemStack> result = new ArrayList<>();
        for (MythicMob mob : MobManager.getInstance().getAllMobs()) {
            ItemStack egg = createMobEgg(mob);
            if (egg != null) {
                result.add(egg);
            }
        }
        return result;
    }

    private ItemStack createMobEgg(MythicMob mob) {
        String entityTypeName = mob.getEntityType();
        Material eggMat = Material.getMaterial(entityTypeName + "_SPAWN_EGG");
        if (eggMat == null) {
            eggMat = Material.getMaterial("PIG_SPAWN_EGG");
            if (eggMat == null) {
                eggMat = Material.getMaterial("MONSTER_EGG");
                if (eggMat == null) {
                    eggMat = Material.STONE;
                }
            }
        }

        ItemStack egg = new ItemStack(eggMat, 1);
        ItemMeta meta = egg.getItemMeta();
        if (meta == null) return egg;

        String display = mob.getDisplayName() == null ? null : mob.getDisplayName().toString();
        if (display == null || display.isEmpty()) {
            display = mob.getInternalName();
        } else {
            display = org.bukkit.ChatColor.translateAlternateColorCodes('&', display);
        }
        meta.setDisplayName(display);

        List<String> lore = new ArrayList<>();
        lore.add("§7内部名: §f" + mob.getInternalName());
        lore.add("§8点击领取 MythicMobs 怪物蛋");
        meta.setLore(lore);
        egg.setItemMeta(meta);
        return egg;
    }
}
