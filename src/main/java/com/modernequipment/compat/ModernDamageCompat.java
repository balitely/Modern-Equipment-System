package com.modernequipment.compat;

import com.modernequipment.MESMod;
import net.minecraftforge.fml.ModList;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.*;

/**
 * ModernDamage 软兼容层。所有直接依赖 MDC 类的操作都集中在这里，
 * 通过反射调用，确保 MDC 不存在时不会引发 ClassNotFoundException。
 */
public class ModernDamageCompat {

    private static final boolean loaded = ModList.get() != null && ModList.get().isLoaded("moderndamage");
    private static final String PART_CLASS = "com.moderndamage.control.api.ModDamagePart";
    private static final String SUB_PART_CLASS = "com.moderndamage.control.api.ModDamageSubPart";
    private static final String CONFIG_CLASS = "com.moderndamage.control.config.ModClothConfig";
    private static final String ROMAN_HELPER_CLASS = "com.moderndamage.control.util.RomanNumberHelper";
    private static final String ARMOR_DATA_CLASS = "com.moderndamage.control.armor.ArmorData";
    private static final String ARMOR_DATA_LOADER_CLASS = "com.moderndamage.control.armor.ArmorDataLoader";
    private static final String PROVIDER_REGISTRY_CLASS = "com.moderndamage.control.api.ProtectionSourceProviderRegistry";

    public static boolean isLoaded() {
        return loaded;
    }

    // ==================== ModDamagePart enum 操作 ====================

    @Nullable
    public static Object getPart(String name) {
        if (!loaded) return null;
        try {
            Class<?> clazz = Class.forName(PART_CLASS);
            return Enum.valueOf((Class<Enum>) clazz, name);
        } catch (Exception e) {
            return null;
        }
    }

    public static List<Object> getAllParts() {
        List<Object> parts = new ArrayList<>();
        if (!loaded) return parts;
        try {
            Class<?> clazz = Class.forName(PART_CLASS);
            Object[] constants = clazz.getEnumConstants();
            if (constants != null) Collections.addAll(parts, constants);
        } catch (Exception ignored) {}
        return parts;
    }

    // ==================== ModDamageSubPart enum 操作 ====================

    @Nullable
    public static Object getSubPart(String name) {
        if (!loaded) return null;
        try {
            Class<?> clazz = Class.forName(SUB_PART_CLASS);
            return Enum.valueOf((Class<Enum>) clazz, name);
        } catch (Exception e) {
            return null;
        }
    }

    public static List<Object> getAllSubParts() {
        List<Object> parts = new ArrayList<>();
        if (!loaded) return parts;
        try {
            Class<?> clazz = Class.forName(SUB_PART_CLASS);
            Object[] constants = clazz.getEnumConstants();
            if (constants != null) Collections.addAll(parts, constants);
        } catch (Exception ignored) {}
        return parts;
    }

    public static String getSubPartKey(Object subPart) {
        if (!loaded || subPart == null) return "";
        try {
            Method m = subPart.getClass().getMethod("getSubKey");
            return (String) m.invoke(subPart);
        } catch (Exception e) {
            return "";
        }
    }

    /** 通过 subKey 反向查找 ModDamageSubPart */
    @Nullable
    public static Object getSubPartByKey(String key) {
        if (!loaded) return null;
        try {
            Class<?> clazz = Class.forName(SUB_PART_CLASS);
            Method m = clazz.getMethod("bySubKey", String.class);
            return m.invoke(null, key);
        } catch (Exception e) {
            return null;
        }
    }

    // ==================== ModClothConfig 操作 ====================

    @Nullable
    public static Object getConfig() {
        if (!loaded) return null;
        try {
            Class<?> clazz = Class.forName(CONFIG_CLASS);
            return clazz.getMethod("get").invoke(null);
        } catch (Exception e) {
            return null;
        }
    }

    /** 获取 armorStackingFactor，默认 0.5 */
    public static float getArmorStackingFactor() {
        if (!loaded) return 0.5f;
        try {
            Object config = getConfig();
            if (config == null) return 0.5f;
            return config.getClass().getField("armorStackingFactor").getFloat(config);
        } catch (Exception e) {
            return 0.5f;
        }
    }

    /** 获取 armorCap，默认 0（无上限） */
    public static int getArmorCap() {
        if (!loaded) return 0;
        try {
            Object config = getConfig();
            if (config == null) return 0;
            return config.getClass().getField("armorCap").getInt(config);
        } catch (Exception e) {
            return 0;
        }
    }

    /** 判断是否启用精确命中框（hardcore + precise） */
    public static boolean isPreciseMode() {
        if (!loaded) return false;
        try {
            Object config = getConfig();
            if (config == null) return false;
            boolean precise = config.getClass().getField("enablePreciseHitbox").getBoolean(config);
            Object damageModel = config.getClass().getField("damageModel").get(config);
            String dmName = damageModel.toString();
            return precise && "HARDCORE".equals(dmName);
        } catch (Exception e) {
            return false;
        }
    }

    // ==================== RomanNumberHelper ====================

    public static String toRomanGrade(int level) {
        if (!loaded) return fallbackRomanGrade(level);
        try {
            Class<?> clazz = Class.forName(ROMAN_HELPER_CLASS);
            Method m = clazz.getMethod("toRomanGrade", int.class);
            return (String) m.invoke(null, level);
        } catch (Exception e) {
            return fallbackRomanGrade(level);
        }
    }

    private static String fallbackRomanGrade(int level) {
        if (level <= 0) return "None";
        String[] grades = {"I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};
        int index = Math.min(level / 10, 9);
        return grades[index];
    }

    // ==================== ArmorData / ArmorDataLoader ====================

    @Nullable
    public static Object getArmorData(Object item) {
        if (!loaded || item == null) return null;
        try {
            Class<?> loaderClazz = Class.forName(ARMOR_DATA_LOADER_CLASS);
            Method m = loaderClazz.getMethod("getArmorData", net.minecraft.world.item.Item.class);
            return m.invoke(null, item);
        } catch (Exception e) {
            return null;
        }
    }

    public static float getMaterialFactor(Object armorData, String partName) {
        if (!loaded || armorData == null) return 1.0f;
        try {
            Object part = getPart(partName);
            if (part == null) return 1.0f;
            Method m = armorData.getClass().getMethod("getMaterialFactor", part.getClass());
            return (float) m.invoke(armorData, part);
        } catch (Exception e) {
            return 1.0f;
        }
    }

    public static void reloadArmorData() {
        if (!loaded) return;
        try {
            Class<?> clazz = Class.forName(ARMOR_DATA_LOADER_CLASS);
            clazz.getMethod("load").invoke(null);
        } catch (Exception e) {
            MESMod.LOGGER.error("Failed to reload MDC ArmorData", e);
        }
    }

    // ==================== ProtectionSourceProvider 注册 ====================

    public static void registerProtectionSourceProvider(Object provider) {
        if (!loaded || provider == null) return;
        try {
            Class<?> registryClazz = Class.forName(PROVIDER_REGISTRY_CLASS);
            Method m = registryClazz.getMethod("register",
                    Class.forName("com.moderndamage.control.api.IProtectionSourceProvider"));
            m.invoke(null, provider);
            MESMod.LOGGER.info("Registered MES ProtectionSourceProvider via compat layer");
        } catch (Exception e) {
            MESMod.LOGGER.error("Failed to register MES ProtectionSourceProvider", e);
        }
    }

    // ==================== ArmorHitEvent 判断 ====================

    /** 判断事件是否为 ArmorHitEvent 并提取子部位/部位信息 */
    @Nullable
    public static ArmorHitEventInfo extractArmorHitInfo(Object event) {
        if (!loaded) return null;
        try {
            Class<?> eventClass = Class.forName("com.moderndamage.control.api.event.ArmorHitEvent");
            if (!eventClass.isInstance(event)) return null;

            ArmorHitEventInfo info = new ArmorHitEventInfo();

            // getSubPart()
            try {
                Method getSubPart = eventClass.getMethod("getSubPart");
                Object subPart = getSubPart.invoke(event);
                if (subPart != null) {
                    info.subPartName = ((Enum<?>) subPart).name();
                    Method getSubKey = subPart.getClass().getMethod("getSubKey");
                    info.subPartKey = (String) getSubKey.invoke(subPart);
                }
            } catch (Exception ignored) {}

            // getHitPart()
            try {
                Method getHitPart = eventClass.getMethod("getHitPart");
                Object hitPart = getHitPart.invoke(event);
                if (hitPart != null) {
                    info.hitPartName = ((Enum<?>) hitPart).name();
                }
            } catch (Exception ignored) {}

            // getFinalDamage()
            try {
                Method getFinalDamage = eventClass.getMethod("getFinalDamage");
                info.finalDamage = (float) getFinalDamage.invoke(event);
            } catch (Exception ignored) {}

            // getEntity()
            try {
                Method getEntity = eventClass.getMethod("getEntity");
                info.entity = (net.minecraft.world.entity.LivingEntity) getEntity.invoke(event);
            } catch (Exception ignored) {}

            return info;
        } catch (Exception e) {
            return null;
        }
    }

    public static class ArmorHitEventInfo {
        @Nullable public String subPartName;
        @Nullable public String subPartKey;
        @Nullable public String hitPartName;
        public float finalDamage;
        @Nullable public net.minecraft.world.entity.LivingEntity entity;
    }

    // ==================== 防护等级堆叠（代替 ModClothConfig 直接调用） ====================

    /**
     * 计算堆叠后的防护值，MDC 可用时走 MDC 配置，否则使用默认值。
     */
    public static int calculateStackedLevel(List<Integer> levels) {
        if (levels.isEmpty()) return 0;
        levels.sort(Collections.reverseOrder());
        int highest = levels.get(0);
        if (levels.size() == 1) return highest;
        int otherSum = 0;
        for (int i = 1; i < levels.size(); i++) otherSum += levels.get(i);
        float factor = getArmorStackingFactor();
        int total = highest + Math.round(otherSum * factor);
        int cap = getArmorCap();
        if (cap > 0 && total > cap) total = cap;
        return total;
    }
}