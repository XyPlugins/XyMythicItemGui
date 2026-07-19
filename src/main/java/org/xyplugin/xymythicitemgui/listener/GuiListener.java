package org.xyplugin.xymythicitemgui.listener;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.xyplugin.xymythicitemgui.XyMythicItemGui;
import org.xyplugin.xymythicitemgui.gui.MainGui;
import org.xyplugin.xymythicitemgui.gui.MenuGui;
import org.xyplugin.xymythicitemgui.gui.MobGui;
import org.xyplugin.xymythicitemgui.gui.SearchGui;
import org.xyplugin.xymythicitemgui.manager.ConfigManager;
import org.xyplugin.xymythicitemgui.manager.SessionManager;
import org.xyplugin.xymythicitemgui.utils.MessageUtil;

import java.util.List;

public class GuiListener implements Listener {

    private final ConfigManager config = ConfigManager.getInstance();

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        boolean isMenu = title.equals(config.getMenuGuiTitle());
        boolean isMainOrSearch = title.startsWith(config.getGuiTitle());
        boolean isMob = title.equals(config.getMobGuiTitle());
        if (!isMenu && !isMainOrSearch && !isMob) return;
        if (event.getClickedInventory() != event.getView().getTopInventory()) return;
        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        if (isMenu) {
            handleMenuClick(player, clicked);
            return;
        }

        String guiType = SessionManager.getInstance().getGuiType(player);

        if (clicked.getType() == Material.COMPASS) {
            if ("mob".equals(guiType)) {
                MessageUtil.send(player, "mob-search-not-supported");
                return;
            }
            player.closeInventory();
            MessageUtil.send(player, "search-prompt");
            SessionManager.getInstance().startWaitingSearch(player);
            return;
        }

        if (clicked.getType() == Material.BARRIER) {
            MainGui.getInstance().open(player, 0);
            return;
        }

        if (clicked.getType() == Material.ARROW) {
            handlePageClick(player, clicked, guiType);
            return;
        }

        if (clicked.getType() == config.getBorderMaterial()) return;

        if ("mob".equals(guiType)) {
            giveMobEgg(player, clicked, event.getClick());
        } else {
            giveItem(player, clicked, event.getClick());
        }
    }

    private void handleMenuClick(Player player, ItemStack clicked) {
        if (clicked.getType() == Material.CHEST) {
            MainGui.getInstance().open(player, 0);
        } else if (clicked.getType() == Material.MONSTER_EGG) {
            MobGui.getInstance().open(player, 0);
        }
    }

    private void handlePageClick(Player player, ItemStack clicked, String guiType) {
        if (!clicked.hasItemMeta() || !clicked.getItemMeta().hasDisplayName()) return;
        String display = clicked.getItemMeta().getDisplayName();
        int currentPage = SessionManager.getInstance().getPage(player);
        String keyword = SessionManager.getInstance().getSearchKeyword(player);
        if (display.equals("§e上一页")) {
            if ("mob".equals(guiType)) {
                MobGui.getInstance().open(player, currentPage - 1);
            } else if (keyword == null) {
                MainGui.getInstance().open(player, currentPage - 1);
            } else {
                SearchGui.getInstance().open(player, keyword, currentPage - 1);
            }
        } else if (display.equals("§e下一页")) {
            if ("mob".equals(guiType)) {
                MobGui.getInstance().open(player, currentPage + 1);
            } else if (keyword == null) {
                MainGui.getInstance().open(player, currentPage + 1);
            } else {
                SearchGui.getInstance().open(player, keyword, currentPage + 1);
            }
        }
    }

    private void giveItem(Player player, ItemStack item, ClickType click) {
        ItemStack give = item.clone();
        int amount = config.getItemAmount(click);
        if (amount <= 0) return;
        give.setAmount(amount);

        if (player.getInventory().firstEmpty() == -1) {
            MessageUtil.send(player, "inventory-full");
            return;
        }
        player.getInventory().addItem(give);
        String itemName = item.hasItemMeta() && item.getItemMeta().hasDisplayName()
                ? item.getItemMeta().getDisplayName()
                : item.getType().name();
        MessageUtil.send(player, "item-received",
                "%item%", itemName,
                "%amount%", String.valueOf(amount));
    }

    private void giveMobEgg(Player player, ItemStack eggItem, ClickType click) {
        ItemMeta meta = eggItem.getItemMeta();
        if (meta == null) return;

        String internalName = extractMobInternalName(meta);
        if (internalName == null || internalName.isEmpty()) {
            internalName = ChatColor.stripColor(meta.getDisplayName());
        }

        int amount = config.getItemAmount(click);
        if (amount <= 0) return;

        if (player.getInventory().firstEmpty() == -1) {
            MessageUtil.send(player, "inventory-full");
            return;
        }

        String command = "mm egg give " + player.getName() + " " + internalName + " " + amount;
        XyMythicItemGui.getInstance().getLogger().info("Dispatch MythicMobs egg command: /" + command);
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);

        String mobName = meta.hasDisplayName() ? meta.getDisplayName() : internalName;
        MessageUtil.send(player, "mob-received",
                "%mob%", mobName,
                "%amount%", String.valueOf(amount));
    }

    private String extractMobInternalName(ItemMeta meta) {
        List<String> lore = meta.getLore();
        if (lore == null) return null;
        for (String line : lore) {
            String plain = ChatColor.stripColor(line);
            String marker = "内部名:";
            int index = plain.indexOf(marker);
            if (index >= 0) {
                return plain.substring(index + marker.length()).trim();
            }
        }
        return null;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (SessionManager.getInstance().isWaitingSearch(player)) {
            event.setCancelled(true);
            String keyword = event.getMessage();
            SessionManager.getInstance().clearWaitingSearch(player);
            XyMythicItemGui.getInstance().getServer().getScheduler().runTask(
                    XyMythicItemGui.getInstance(),
                    () -> {
                        if (keyword.equalsIgnoreCase("cancel")) {
                            MenuGui.getInstance().open(player, 0);
                            MessageUtil.send(player, "search-cancelled");
                            return;
                        }
                        SearchGui.getInstance().open(player, keyword, 0);
                        MessageUtil.send(player, "search-result", "%keyword%", keyword);
                    }
            );
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();
        String title = event.getView().getTitle();
        if (title.startsWith(config.getGuiTitle()) || title.equals(config.getMobGuiTitle()) || title.equals(config.getMenuGuiTitle())) {
            SessionManager.getInstance().setSearchKeyword(player, null);
        }
    }
}
