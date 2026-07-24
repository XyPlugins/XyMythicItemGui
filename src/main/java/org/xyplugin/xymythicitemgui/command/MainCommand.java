package org.xyplugin.xymythicitemgui.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.xyplugin.xymythicitemgui.gui.MainGui;
import org.xyplugin.xymythicitemgui.gui.MenuGui;
import org.xyplugin.xymythicitemgui.gui.MobGui;
import org.xyplugin.xymythicitemgui.gui.SearchGui;
import org.xyplugin.xymythicitemgui.manager.ItemCache;
import org.xyplugin.xymythicitemgui.manager.ItemUpdateManager;
import org.xyplugin.xymythicitemgui.manager.ReloadManager;
import org.xyplugin.xymythicitemgui.utils.MessageUtil;

public class MainCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player && !sender.hasPermission("xygui.admin")) {
            MessageUtil.send(sender, "no-permission");
            return true;
        }

        if (!(sender instanceof Player)) {
            if (args.length == 0) {
                sendHelp(sender);
                return true;
            }
            if (!args[0].equalsIgnoreCase("reload")
                    && !args[0].equalsIgnoreCase("give")
                    && !args[0].equalsIgnoreCase("update")) {
                MessageUtil.send(sender, "console-limited");
                return true;
            }
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "open":
                Player menuPlayer = requirePlayer(sender);
                if (menuPlayer == null) return true;
                MenuGui.getInstance().open(menuPlayer, 0);
                break;

            case "i":
            case "item":
            case "items":
                handleItemCommand(sender, args);
                break;

            case "e":
            case "entity":
            case "mob":
            case "mobs":
                handleMobCommand(sender, args);
                break;

            case "search":
                Player searchPlayer = requirePlayer(sender);
                if (searchPlayer == null) return true;
                if (args.length < 2) {
                    MessageUtil.send(searchPlayer, "search-usage");
                    return true;
                }
                String keyword = String.join(" ", args).substring(args[0].length() + 1);
                SearchGui.getInstance().open(searchPlayer, keyword, 0);
                break;

            case "reload":
                ReloadManager.getInstance().reloadMythicThenXyGui(sender);
                break;

            case "give":
                handleGive(sender, args);
                break;

            case "update":
                handleUpdateCommand(sender, args);
                break;

            default:
                sendHelp(sender);
                break;
        }
        return true;
    }

    private void handleUpdateCommand(CommandSender sender, String[] args) {
        if (args.length < 3) {
            MessageUtil.send(sender, "update-usage");
            return;
        }

        Player target = Bukkit.getPlayer(args[2]);
        if (target == null) {
            MessageUtil.send(sender, "player-not-found");
            return;
        }

        if (args[1].equalsIgnoreCase("check")) {
            int amount = ItemUpdateManager.getInstance().scanPlayerNow(target, false);
            MessageUtil.send(sender, "update-check-result",
                    "%player%", target.getName(),
                    "%amount%", String.valueOf(amount));
            return;
        }

        if (args[1].equalsIgnoreCase("scan")) {
            int amount = ItemUpdateManager.getInstance().scanPlayerNow(target, true);
            MessageUtil.send(sender, "update-scan-result",
                    "%player%", target.getName(),
                    "%amount%", String.valueOf(amount));
            return;
        }

        MessageUtil.send(sender, "update-usage");
    }

    private void handleItemCommand(CommandSender sender, String[] args) {
        Player player = requirePlayer(sender);
        if (player == null) return;
        if (args.length < 2 || args[1].equalsIgnoreCase("open")) {
            MainGui.getInstance().open(player, parsePage(args, 2));
            return;
        }
        if (args[1].equalsIgnoreCase("search")) {
            if (args.length < 3) {
                MessageUtil.send(player, "search-usage");
                return;
            }
            String keyword = joinFrom(args, 2);
            SearchGui.getInstance().open(player, keyword, 0);
            return;
        }
        sendHelp(sender);
    }

    private void handleMobCommand(CommandSender sender, String[] args) {
        Player player = requirePlayer(sender);
        if (player == null) return;
        if (args.length < 2 || args[1].equalsIgnoreCase("open")) {
            MobGui.getInstance().open(player, parsePage(args, 2));
            return;
        }
        sendHelp(sender);
    }

    private void handleGive(CommandSender sender, String[] args) {
        if (args.length < 3) {
            MessageUtil.send(sender, "give-usage");
            return;
        }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            MessageUtil.send(sender, "player-not-found");
            return;
        }
        String itemName = args[2];
        int amount = 1;
        if (args.length > 3) {
            try { amount = Integer.parseInt(args[3]); } catch (NumberFormatException ignored) {}
        }
        io.lumine.xikage.mythicmobs.items.MythicItem mmItem = ItemCache.getInstance().getItem(itemName);
        if (mmItem == null) {
            MessageUtil.send(sender, "item-not-found", "%item%", itemName);
            return;
        }
        ItemStack stack = ItemCache.getInstance().getItemStack(itemName);
        if (stack == null) {
            MessageUtil.send(sender, "item-generate-failed");
            return;
        }
        stack.setAmount(amount);
        if (target.getInventory().firstEmpty() == -1) {
            if (sender instanceof Player) MessageUtil.send((Player) sender, "inventory-full");
            else MessageUtil.send(sender, "inventory-full");
            return;
        }
        target.getInventory().addItem(stack);
        MessageUtil.send(target, "item-received",
                "%item%", mmItem.getDisplayName(),
                "%amount%", String.valueOf(amount));
        MessageUtil.send(sender, "give-success",
                "%amount%", String.valueOf(amount),
                "%item%", itemName,
                "%player%", target.getName());
    }

    private Player requirePlayer(CommandSender sender) {
        if (!(sender instanceof Player)) {
            MessageUtil.send(sender, "only-player");
            return null;
        }
        return (Player) sender;
    }

    private int parsePage(String[] args, int index) {
        if (args.length <= index) return 0;
        try { return Math.max(0, Integer.parseInt(args[index])); } catch (NumberFormatException ignored) { return 0; }
    }

    private String joinFrom(String[] args, int index) {
        StringBuilder builder = new StringBuilder();
        for (int i = index; i < args.length; i++) {
            if (builder.length() > 0) builder.append(' ');
            builder.append(args[i]);
        }
        return builder.toString();
    }

    private void sendHelp(CommandSender sender) {
        MessageUtil.sendRaw(sender, "§6§l=== XyMythicItemGui 帮助 ===");
        MessageUtil.sendRaw(sender, "§e/xygui open §7打开总 GUI");
        MessageUtil.sendRaw(sender, "§e/xygui i open [页码] §7打开物品 GUI");
        MessageUtil.sendRaw(sender, "§e/xygui i search <关键词> §7搜索物品");
        MessageUtil.sendRaw(sender, "§e/xygui e open [页码] §7打开怪物蛋 GUI");
        MessageUtil.sendRaw(sender, "§e/xygui give <玩家> <物品名> [数量] §7直接给予物品");
        MessageUtil.sendRaw(sender, "§e/xygui update check <玩家> §7预览可更新物品数量");
        MessageUtil.sendRaw(sender, "§e/xygui update scan <玩家> §7立即更新玩家身上物品");
        MessageUtil.sendRaw(sender, "§e/xygui reload §7重载 MythicMobs 并同步刷新 XyGui");
    }
}

