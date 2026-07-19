package org.xyplugin.xymythicitemgui.manager;

import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {

    private static SessionManager instance;
    private final Map<UUID, Integer> pageMap = new ConcurrentHashMap<>();
    private final Map<UUID, String> searchKeywordMap = new ConcurrentHashMap<>();
    private final Map<UUID, Long> waitingSearchMap = new ConcurrentHashMap<>();
    private final Map<UUID, String> guiTypeMap = new ConcurrentHashMap<>(); // "main" 或 "mob"

    public static SessionManager getInstance() {
        if (instance == null) instance = new SessionManager();
        return instance;
    }

    // === 页码 ===
    public int getPage(Player player) {
        return pageMap.getOrDefault(player.getUniqueId(), 0);
    }

    public void setPage(Player player, int page) {
        pageMap.put(player.getUniqueId(), page);
    }

    // === 搜索关键词 ===
    public String getSearchKeyword(Player player) {
        return searchKeywordMap.get(player.getUniqueId());
    }

    public void setSearchKeyword(Player player, String keyword) {
        if (keyword == null) searchKeywordMap.remove(player.getUniqueId());
        else searchKeywordMap.put(player.getUniqueId(), keyword);
    }

    // === 等待搜索状态 ===
    public void startWaitingSearch(Player player) {
        waitingSearchMap.put(player.getUniqueId(), System.currentTimeMillis());
    }

    public boolean isWaitingSearch(Player player) {
        return waitingSearchMap.containsKey(player.getUniqueId());
    }

    public void clearWaitingSearch(Player player) {
        waitingSearchMap.remove(player.getUniqueId());
    }

    public void cleanExpiredSessions() {
        long now = System.currentTimeMillis();
        long timeout = ConfigManager.getInstance().getSearchTimeout() * 1000;
        waitingSearchMap.entrySet().removeIf(entry -> now - entry.getValue() > timeout);
    }

    // === GUI 类型 ===
    public void setGuiType(Player player, String type) {
        guiTypeMap.put(player.getUniqueId(), type);
    }

    public String getGuiType(Player player) {
        return guiTypeMap.getOrDefault(player.getUniqueId(), "main");
    }

    // === 清理 ===
    public void removePlayer(Player player) {
        UUID uuid = player.getUniqueId();
        pageMap.remove(uuid);
        searchKeywordMap.remove(uuid);
        waitingSearchMap.remove(uuid);
        guiTypeMap.remove(uuid);
    }

    public void clearAll() {
        pageMap.clear();
        searchKeywordMap.clear();
        waitingSearchMap.clear();
        guiTypeMap.clear();
    }
}