package org.xyplugin.xymythicitemgui.manager;

import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.mobs.MythicMob;
import org.bukkit.ChatColor;
import org.xyplugin.xymythicitemgui.XyMythicItemGui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MobManager {

    private static MobManager instance;
    private final List<MythicMob> mythicMobs = new ArrayList<>();

    public static MobManager getInstance() {
        if (instance == null) instance = new MobManager();
        return instance;
    }

    public void reload() {
        mythicMobs.clear();
        try {
            Collection<String> mobNamesCollection = MythicMobs.inst().getMobManager().getMobNames();
            if (mobNamesCollection == null) {
                XyMythicItemGui.getInstance().getLogger().warning("[MM怪物库] getMobNames() 返回 null！");
                return;
            }

            for (String name : mobNamesCollection) {
                try {
                    MythicMob mob = MythicMobs.inst().getMobManager().getMythicMob(name);
                    if (mob != null) {
                        mythicMobs.add(mob);
                    } else {
                        XyMythicItemGui.getInstance().getLogger().warning("[MM怪物库] 无法获取怪物对象: " + name);
                    }
                } catch (Exception e) {
                    XyMythicItemGui.getInstance().getLogger().warning("[MM怪物库] 获取怪物 " + name + " 时异常: " + e.getMessage());
                }
            }

            sortMobs();
            XyMythicItemGui.getInstance().getLogger().info("[MM怪物库] 成功加载 " + mythicMobs.size() + " 个怪物");
        } catch (Exception e) {
            XyMythicItemGui.getInstance().getLogger().severe("[MM怪物库] 加载怪物时发生严重异常: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void sortMobs() {
        ConfigManager config = ConfigManager.getInstance();
        if (!config.isMobSortEnabled()) return;

        String by = config.getMobSortBy();
        boolean asc = config.isMobSortAscending();

        Comparator<MythicMob> comparator;
        if ("internal".equalsIgnoreCase(by)) {
            comparator = Comparator.comparing(
                    MythicMob::getInternalName,
                    String.CASE_INSENSITIVE_ORDER
            );
        } else {
            comparator = Comparator.comparing(
                    mob -> {
                        String display = mob.getDisplayName() == null ? "" : mob.getDisplayName().toString();
                        return ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', display));
                    },
                    String.CASE_INSENSITIVE_ORDER
            );
        }

        if (!asc) {
            comparator = comparator.reversed();
        }

        Collections.sort(mythicMobs, comparator);
    }

    public List<MythicMob> getAllMobs() {
        return new ArrayList<>(mythicMobs);
    }

    public MythicMob getMob(String internalName) {
        try {
            return MythicMobs.inst().getMobManager().getMythicMob(internalName);
        } catch (Exception e) {
            return null;
        }
    }
}
