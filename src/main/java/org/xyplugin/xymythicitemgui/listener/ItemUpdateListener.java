package org.xyplugin.xymythicitemgui.listener;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.xyplugin.xymythicitemgui.XyMythicItemGui;
import org.xyplugin.xymythicitemgui.manager.ConfigManager;
import org.xyplugin.xymythicitemgui.manager.ItemUpdateManager;

public class ItemUpdateListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!ConfigManager.getInstance().isItemUpdateEnabled()) return;
        if (!ConfigManager.getInstance().isItemUpdateScanOnJoin()) return;

        long delay = Math.max(1L, ConfigManager.getInstance().getItemUpdateJoinDelayTicks());
        Bukkit.getScheduler().runTaskLater(
                XyMythicItemGui.getInstance(),
                () -> ItemUpdateManager.getInstance().queuePlayer(event.getPlayer()),
                delay
        );
    }
}
