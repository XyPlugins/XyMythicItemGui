package org.xyplugin.xymythicitemgui.utils;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class NbtItemUtil {

    private static final String CRAFT_ITEM_STACK = "org.bukkit.craftbukkit.%s.inventory.CraftItemStack";
    private static final String NBT_TAG_COMPOUND = "net.minecraft.server.%s.NBTTagCompound";
    private static final String ITEM_STACK = "net.minecraft.server.%s.ItemStack";

    private static Class<?> craftItemStackClass;
    private static Class<?> nmsItemStackClass;
    private static Class<?> nbtTagCompoundClass;
    private static Method asNmsCopy;
    private static Method asBukkitCopy;
    private static Method hasTag;
    private static Method getTag;
    private static Method setTag;
    private static Method save;
    private static Method setString;
    private static Method getString;
    private static Method hasKey;
    private static Method remove;
    private static Constructor<?> nbtConstructor;
    private static boolean initialized;

    private static void init() {
        if (initialized) return;
        try {
            String version = Bukkit.getServer().getClass().getPackage().getName();
            version = version.substring(version.lastIndexOf('.') + 1);

            craftItemStackClass = Class.forName(String.format(CRAFT_ITEM_STACK, version));
            nmsItemStackClass = Class.forName(String.format(ITEM_STACK, version));
            nbtTagCompoundClass = Class.forName(String.format(NBT_TAG_COMPOUND, version));

            asNmsCopy = craftItemStackClass.getMethod("asNMSCopy", ItemStack.class);
            asBukkitCopy = craftItemStackClass.getMethod("asBukkitCopy", nmsItemStackClass);
            hasTag = nmsItemStackClass.getMethod("hasTag");
            getTag = nmsItemStackClass.getMethod("getTag");
            setTag = nmsItemStackClass.getMethod("setTag", nbtTagCompoundClass);
            save = nmsItemStackClass.getMethod("save", nbtTagCompoundClass);

            setString = nbtTagCompoundClass.getMethod("setString", String.class, String.class);
            getString = nbtTagCompoundClass.getMethod("getString", String.class);
            hasKey = nbtTagCompoundClass.getMethod("hasKey", String.class);
            remove = nbtTagCompoundClass.getMethod("remove", String.class);
            nbtConstructor = nbtTagCompoundClass.getConstructor();
            initialized = true;
        } catch (Exception e) {
            throw new IllegalStateException("Unable to initialize NBT reflection for this server version.", e);
        }
    }

    public static ItemStack setString(ItemStack item, String key, String value) {
        if (item == null || key == null || value == null) return item;
        try {
            init();
            Object nms = asNmsCopy.invoke(null, item);
            Object tag = getOrCreateTag(nms);
            setString.invoke(tag, key, value);
            setTag.invoke(nms, tag);
            return (ItemStack) asBukkitCopy.invoke(null, nms);
        } catch (Exception e) {
            return item;
        }
    }

    public static String getString(ItemStack item, String key) {
        if (item == null || key == null) return "";
        try {
            init();
            Object nms = asNmsCopy.invoke(null, item);
            if (!(Boolean) hasTag.invoke(nms)) return "";
            Object tag = getTag.invoke(nms);
            if (tag == null || !(Boolean) hasKey.invoke(tag, key)) return "";
            return (String) getString.invoke(tag, key);
        } catch (Exception e) {
            return "";
        }
    }

    public static ItemStack removeKeys(ItemStack item, String... keys) {
        if (item == null || keys == null || keys.length == 0) return item;
        try {
            init();
            Object nms = asNmsCopy.invoke(null, item);
            if (!(Boolean) hasTag.invoke(nms)) return item;
            Object tag = getTag.invoke(nms);
            if (tag == null) return item;
            for (String key : keys) {
                if (key != null && (Boolean) hasKey.invoke(tag, key)) {
                    remove.invoke(tag, key);
                }
            }
            setTag.invoke(nms, tag);
            return (ItemStack) asBukkitCopy.invoke(null, nms);
        } catch (Exception e) {
            return item;
        }
    }

    public static String toNbtString(ItemStack item) {
        if (item == null) return "";
        try {
            init();
            Object nms = asNmsCopy.invoke(null, item);
            Object compound = nbtConstructor.newInstance();
            Object saved = save.invoke(nms, compound);
            return saved == null ? compound.toString() : saved.toString();
        } catch (Exception e) {
            return "";
        }
    }

    private static Object getOrCreateTag(Object nms) throws Exception {
        if ((Boolean) hasTag.invoke(nms)) {
            Object tag = getTag.invoke(nms);
            if (tag != null) return tag;
        }
        return nbtConstructor.newInstance();
    }
}
