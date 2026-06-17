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

    public ModifierProperties getModifiers() { return modifiers; }
    public void setModifiers(ModifierProperties modifiers) { this.modifiers = modifiers; }
}