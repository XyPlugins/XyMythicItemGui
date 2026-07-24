package org.xyplugin.xymythicitemgui.manager;

import io.lumine.xikage.mythicmobs.items.MythicItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.xyplugin.xymythicitemgui.XyMythicItemGui;
import org.xyplugin.xymythicitemgui.utils.NbtItemUtil;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;

public class ItemUpdateManager {

    public static final String TAG_ITEM_ID = "xygui_mythic_id";
    public static final String TAG_HASH = "xygui_template_hash";
    public static final String TAG_UPDATE = "xygui_update_enabled";
    public static final String TAG_MODE = "xygui_update_mode";
    public static final String TAG_MARK_VERSION = "xygui_mark_version";

    private static final String[] UPDATE_TAGS = {
            TAG_ITEM_ID,
            TAG_HASH,
            TAG_UPDATE,
            TAG_MODE,
            TAG_MARK_VERSION
    };

    private static ItemUpdateManager instance;

    private final Map<String, UpdateRule> updateRules = new HashMap<>();
    private final Queue<Player> scanQueue = new ArrayDeque<>();
    private int taskId = -1;

    public static ItemUpdateManager getInstance() {
        if (instance == null) instance = new ItemUpdateManager();
        return instance;
    }

    public void reload() {
        updateRules.clear();
        if (!ConfigManager.getInstance().isItemUpdateEnabled()) {
            stopQueue();
            return;
        }

        for (MythicItem item : ItemCache.getInstance().getAllItems()) {
            UpdateRule rule = readRule(item);
            if (rule != null && rule.enabled) {
                updateRules.put(item.getInternalName(), rule);
            }
        }
        XyMythicItemGui.getInstance().getLogger().info(
                "[ItemUpdate] Loaded " + updateRules.size() + " update-enabled MythicMobs items.");
    }

    public boolean isUpdateEnabled(String internalName) {
        return updateRules.containsKey(internalName);
    }

    public ItemStack markIfEnabled(String internalName, ItemStack item) {
        if (item == null || internalName == null) return item;
        UpdateRule rule = updateRules.get(internalName);
        if (rule == null || !rule.enabled) return item;
        String hash = getCurrentHash(internalName);
        if (hash.isEmpty()) return item;
        return applyMarker(item, internalName, hash, rule.mode);
    }

    public void queueOnlinePlayers() {
        if (!ConfigManager.getInstance().isItemUpdateEnabled()) return;
        for (Player player : Bukkit.getOnlinePlayers()) {
            queuePlayer(player);
        }
    }

    public void queuePlayer(Player player) {
        if (player == null || !player.isOnline()) return;
        if (!ConfigManager.getInstance().isItemUpdateEnabled()) return;
        if (!scanQueue.contains(player)) {
            scanQueue.add(player);
        }
        ensureQueueTask();
    }

    public int scanPlayerNow(Player player, boolean apply) {
        if (player == null || !player.isOnline()) return 0;
        if (!ConfigManager.getInstance().isItemUpdateEnabled()) return 0;
        return scanInventory(player, apply);
    }

    private int scanInventory(Player player, boolean apply) {
        PlayerInventory inv = player.getInventory();
        int updated = 0;
        int storageSlots = Math.min(36, inv.getSize());
        for (int slot = 0; slot < storageSlots; slot++) {
            UpdateResult result = updateStack(inv.getItem(slot), apply);
            if (result.updated) {
                updated++;
                if (apply) inv.setItem(slot, result.item);
            }
        }

        ItemStack[] armor = inv.getArmorContents();
        for (int slot = 0; slot < armor.length; slot++) {
            UpdateResult result = updateStack(armor[slot], apply);
            if (result.updated) {
                updated++;
                if (apply) armor[slot] = result.item;
            }
        }
        if (apply) inv.setArmorContents(armor);
        return updated;
    }

    private UpdateResult updateStack(ItemStack oldItem, boolean apply) {
        if (oldItem == null || oldItem.getType() == Material.AIR) return UpdateResult.unchanged(oldItem);

        String itemId = NbtItemUtil.getString(oldItem, TAG_ITEM_ID);
        String updateFlag = NbtItemUtil.getString(oldItem, TAG_UPDATE);
        String oldHash = NbtItemUtil.getString(oldItem, TAG_HASH);
        if (itemId.isEmpty() || !"true".equalsIgnoreCase(updateFlag) || oldHash.isEmpty()) {
            return UpdateResult.unchanged(oldItem);
        }

        UpdateRule rule = updateRules.get(itemId);
        if (rule == null || !rule.enabled) return UpdateResult.unchanged(oldItem);

        String newHash = getCurrentHash(itemId);
        if (newHash.isEmpty() || newHash.equals(oldHash)) return UpdateResult.unchanged(oldItem);
        if (!apply) return UpdateResult.updated(oldItem);

        ItemStack fresh = ItemCache.getInstance().getItemStack(itemId);
        if (fresh == null || fresh.getType() == Material.AIR) return UpdateResult.unchanged(oldItem);
        fresh.setAmount(oldItem.getAmount());
        return UpdateResult.updated(applyMarker(fresh, itemId, newHash, rule.mode));
    }

    private String getCurrentHash(String internalName) {
        ItemStack template = ItemCache.getInstance().getRawItemStack(internalName);
        if (template == null || template.getType() == Material.AIR) return "";
        template.setAmount(1);
        template = NbtItemUtil.removeKeys(template, UPDATE_TAGS);
        String serialized = NbtItemUtil.toNbtString(template);
        if (serialized.isEmpty()) return "";
        return sha256(serialized);
    }

    private ItemStack applyMarker(ItemStack item, String internalName, String hash, String mode) {
        ItemStack marked = item;
        marked = NbtItemUtil.setString(marked, TAG_ITEM_ID, internalName);
        marked = NbtItemUtil.setString(marked, TAG_HASH, hash);
        marked = NbtItemUtil.setString(marked, TAG_UPDATE, "true");
        marked = NbtItemUtil.setString(marked, TAG_MODE, mode);
        marked = NbtItemUtil.setString(marked, TAG_MARK_VERSION, "1");
        return marked;
    }

    private UpdateRule readRule(MythicItem item) {
        boolean enabled = item.getConfig().getBoolean("XyUpdate.Enabled", false);
        if (!enabled && item.getConfig().isSet("XyUpdate")) {
            enabled = item.getConfig().getBoolean("XyUpdate", false);
        }
        if (!enabled) return null;
        String mode = item.getConfig().getString("XyUpdate.Mode", "replace");
        mode = mode == null ? "replace" : mode.toLowerCase(Locale.ENGLISH);
        if (!mode.equals("replace")) {
            XyMythicItemGui.getInstance().getLogger().warning(
                    "[ItemUpdate] " + item.getInternalName() + " uses unsupported mode '" + mode
                            + "'. Falling back to replace.");
            mode = "replace";
        }
        return new UpdateRule(true, mode);
    }

    private void ensureQueueTask() {
        if (taskId != -1) return;
        taskId = Bukkit.getScheduler().runTaskTimer(
                XyMythicItemGui.getInstance(),
                this::processQueue,
                1L,
                Math.max(1, ConfigManager.getInstance().getItemUpdateQueueIntervalTicks())
        ).getTaskId();
    }

    private void processQueue() {
        int maxPlayers = Math.max(1, ConfigManager.getInstance().getItemUpdatePlayersPerTick());
        int processed = 0;
        while (processed < maxPlayers && !scanQueue.isEmpty()) {
            Player player = scanQueue.poll();
            if (player != null && player.isOnline()) {
                int updated = scanPlayerNow(player, true);
                if (updated > 0 && ConfigManager.getInstance().isItemUpdateNotifyPlayer()) {
                    org.xyplugin.xymythicitemgui.utils.MessageUtil.send(player,
                            "update-items-updated",
                            "%amount%",
                            String.valueOf(updated));
                }
            }
            processed++;
        }

        if (scanQueue.isEmpty()) {
            stopQueue();
        }
    }

    private void stopQueue() {
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }
        scanQueue.clear();
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte b : hash) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (Exception e) {
            return "";
        }
    }

    private static class UpdateRule {
        private final boolean enabled;
        private final String mode;

        private UpdateRule(boolean enabled, String mode) {
            this.enabled = enabled;
            this.mode = mode;
        }
    }

    private static class UpdateResult {
        private final ItemStack item;
        private final boolean updated;

        private UpdateResult(ItemStack item, boolean updated) {
            this.item = item;
            this.updated = updated;
        }

        private static UpdateResult unchanged(ItemStack item) {
            return new UpdateResult(item, false);
        }

        private static UpdateResult updated(ItemStack item) {
            return new UpdateResult(item, true);
        }
    }
}
