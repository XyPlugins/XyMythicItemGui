package org.xyplugin.xymythicitemgui.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.xyplugin.xymythicitemgui.XyMythicItemGui;
import org.xyplugin.xymythicitemgui.gui.MainGui;
import org.xyplugin.xymythicitemgui.gui.MenuGui;
import org.xyplugin.xymythicitemgui.gui.MobGui;
import org.xyplugin.xymythicitemgui.gui.SearchGui;
import org.xyplugin.xymythicitemgui.manager.ConfigManager;
import org.xyplugin.xymythicitemgui.manager.ItemCache;
import org.xyplugin.xymythicitemgui.manager.MessageManager;
import org.xyplugin.xymythicitemgui.manager.MobManager;
import org.xyplugin.xymythicitemgui.utils.MessageUtil;

public class MainCommand implements CommandExecutor {

    private final ConfigManager config = ConfigManager.getInstance();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player && !sender.hasPermission("xygui.admin")) {
            sender.sendMessage("§c你没有权限使用此命令");
            return true;
        }

        if (!(sender instanceof Player)) {
            if (args.length == 0) {
                sendHelp(sender);
                return true;
            }
            if (!args[0].equalsIgnoreCase("reload") && !args[0].equalsIgnoreCase("give")) {
                sender.sendMessage("§c控制台只能使用 /xygui reload 和 /xygui give");
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
                XyMythicItemGui.getInstance().reloadConfig();
                config.loadConfig();
                ItemCache.getInstance().reload();
                MobManager.getInstance().reload();
                MessageManager.getInstance().reload();
                sender.sendMessage("§a配置、物品缓存、怪物缓存和消息文件已重载");
                break;

            case "give":
                handleGive(sender, args);
                break;

            default:
                sendHelp(sender);
                break;
        }
        return true;
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
            sender.sendMessage("§c用法: /xygui give <玩家名> <物品名> [数量]");
            return;
        }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§c目标玩家不在线或不存在");
            return;
        }
        String itemName = args[2];
        int amount = 1;
        if (args.length > 3) {
            try { amount = Integer.parseInt(args[3]); } catch (NumberFormatException ignored) {}
        }
        io.lumine.xikage.mythicmobs.items.MythicItem mmItem = ItemCache.getInstance().getItem(itemName);
        if (mmItem == null) {
            sender.sendMessage("§c找不到名为 '" + itemName + "' 的 MythicMobs 物品");
            return;
        }
        ItemStack stack = ItemCache.getInstance().getItemStack(itemName);
        if (stack == null) {
            sender.sendMessage("§c生成物品失败，请检查物品配置");
            return;
        }
        stack.setAmount(amount);
        if (target.getInventory().firstEmpty() == -1) {
            if (sender instanceof Player) MessageUtil.send((Player) sender, "inventory-full");
            else sender.sendMessage("§c目标玩家背包已满！");
            return;
        }
        target.getInventory().addItem(stack);
        MessageUtil.send(target, "item-received",
                "%item%", mmItem.getDisplayName(),
                "%amount%", String.valueOf(amount));
        sender.sendMessage("§a成功将 §6" + amount + " 个 §6" + itemName + " §a给予 §6" + target.getName());
    }

    private Player requirePlayer(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c该命令仅玩家可用");
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
        sender.sendMessage("§6§l=== XyMythicItemGui 帮助 ===");
        sender.sendMessage("§e/xygui open §7打开总 GUI");
        sender.sendMessage("§e/xygui i open [页码] §7打开物品 GUI");
        sender.sendMessage("§e/xygui i search <关键词> §7搜索物品");
        sender.sendMessage("§e/xygui e open [页码] §7打开怪物蛋 GUI");
        sender.sendMessage("§e/xygui give <玩家> <物品名> [数量] §7直接给予物品");
        sender.sendMessage("§e/xygui reload §7重载配置和缓存");
    }
}
