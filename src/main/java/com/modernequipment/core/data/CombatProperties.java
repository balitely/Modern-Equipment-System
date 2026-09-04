package com.modernequipment.core.data;

import com.google.gson.annotations.SerializedName;
import java.util.HashMap;
import java.util.Map;

public class CombatProperties {
    @SerializedName("armor_levels")
    private Map<String, Integer> armorLevels = new HashMap<>();
    private Map<String, Integer> toughness = new HashMap<>();
    @SerializedName("material_factor")
    private Map<String, Float> materialFactor = new HashMap<>();
    @SerializedName("ricochet_chance")
    private Map<String, Float> ricochetChance = new HashMap<>();

    @SerializedName("armor_levels_sub")
    private Map<String, Integer> armorLevelsSub = new HashMap<>();
    @SerializedName("toughness_sub")
    private Map<String, Integer> toughnessSub = new HashMap<>();
    @SerializedName("ricochet_sub")
    private Map<String, Float> ricochetSub = new HashMap<>();
    @SerializedName("material_factor_sub")
    private Map<String, Float> materialFactorSub = new HashMap<>();

    private ModifierProperties modifiers;

    public Map<String, Integer> getArmorLevels() { return armorLevels; }
    public void setArmorLevels(Map<String, Integer> armorLevels) { this.armorLevels = armorLevels; }
    public Map<String, Integer> getToughness() { return toughness; }
    public void setToughness(Map<String, Integer> toughness) { this.toughness = toughness; }
    public Map<String, Float> getMaterialFactor() { return materialFactor; }
    public void setMaterialFactor(Map<String, Float> materialFactor) { this.materialFactor = materialFactor; }
    public Map<String, Float> getRicochetChance() { return ricochetChance; }
    public void setRicochetChance(Map<String, Float> ricochetChance) { this.ricochetChance = ricochetChance; }

    public Map<String, Integer> getArmorLevelsSub() { return armorLevelsSub; }
    public void setArmorLevelsSub(Map<String, Integer> armorLevelsSub) { this.armorLevelsSub = armorLevelsSub; }
    public Map<String, Integer> getToughnessSub() { return toughnessSub; }
    public void setToughnessSub(Map<String, Integer> toughnessSub) { this.toughnessSub = toughnessSub; }
    public Map<String, Float> getRicochetSub() { return ricochetSub; }
    public void setRicochetSub(Map<String, Float> ricochetSub) { this.ricochetSub = ricochetSub; }
    public Map<String, Float> getMaterialFactorSub() { return materialFactorSub; }
    public void setMaterialFactorSub(Map<String, Float> materialFactorSub) { this.materialFactorSub = materialFactorSub; }

    /** Case-insensitive lookup for body-part/sub-part JSON keys. */
    public static <T> T getIgnoreCase(Map<String, T> map, String key, T fallback) {
        if (map == null || map.isEmpty() || key == null) return fallback;
        T direct = map.get(key);
        if (direct != null) return direct;
        for (Map.Entry<String, T> entry : map.entrySet()) {
            if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(key) && entry.getValue() != null) {
                return entry.getValue();
            }
        }
        return fallback;
    }

    public int getArmorLevel(String key) { return getIgnoreCase(armorLevels, key, 0); }
    public int getArmorLevelSub(String key) { return getIgnoreCase(armorLevelsSub, key, 0); }
    public int getToughnessValue(String key) { return getIgnoreCase(toughness, key, 0); }
    public int getToughnessSubValue(String key) { return getIgnoreCase(toughnessSub, key, 0); }
    public float getMaterialFactorValue(String key) { return getIgnoreCase(materialFactor, key, 1.0f); }
    public float getMaterialFactorSubValue(String key) { return getIgnoreCase(materialFactorSub, key, Float.NaN); }
    public float getRicochetChanceValue(String key) { return getIgnoreCase(ricochetChance, key, 0.0f); }
    public float getRicochetSubValue(String key) { return getIgnoreCase(ricochetSub, key, 0.0f); }

    public ModifierProperties getModifiers() { return modifiers; }
    public void setModifiers(ModifierProperties modifiers) { this.modifiers = modifiers; }
}