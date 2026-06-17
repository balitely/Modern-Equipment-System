package com.modernequipment.util;

import com.modernequipment.core.data.AttachmentData;
import com.modernequipment.core.data.CombatProperties;
import com.modernequipment.core.data.ModifierProperties;

import java.util.HashMap;
import java.util.Map;

public class AttachmentDynamicStatsHelper {

    public static CombatProperties getCombatForSlot(AttachmentData attData, String slotName) {
        if (attData == null) return null;
        if (attData.getMountEffects() != null && attData.getMountEffects().containsKey(slotName)) {
            return attData.getMountEffects().get(slotName);
        }
        return attData.getCombat();
    }

    public static ModifierProperties getModifiersForSlot(AttachmentData attData, String slotName) {
        if (attData == null) return null;
        CombatProperties combat = getCombatForSlot(attData, slotName);
        if (combat != null && combat.getModifiers() != null) {
            return combat.getModifiers();
        }
        return attData.getModifiers();
    }

    public static CombatProperties getRepresentativeCombat(AttachmentData attData) {
        if (attData == null) return null;
        if (attData.getMountEffects() != null && !attData.getMountEffects().isEmpty()) {
            return attData.getMountEffects().values().iterator().next();
        }
        return attData.getCombat();
    }

    public static Map<String, Integer> getProtectionForSlot(AttachmentData attData, String slotName, int currentDura, int maxDura, boolean precise) {
        Map<String, Integer> result = new HashMap<>();
        CombatProperties combat = getCombatForSlot(attData, slotName);
        if (combat == null) return result;
        float percent = getDurabilityPercent(currentDura, maxDura);
        Map<String, Integer> sourceMap = precise ? combat.getArmorLevelsSub() : combat.getArmorLevels();
        if (sourceMap == null) return result;
        for (Map.Entry<String, Integer> entry : sourceMap.entrySet()) {
            int scaled = (int) Math.round(entry.getValue() * percent);
            if (scaled > 0) result.put(entry.getKey(), scaled);
        }
        return result;
    }

    public static Map<String, Integer> getToughnessForSlot(AttachmentData attData, String slotName, int currentDura, int maxDura, boolean precise) {
        Map<String, Integer> result = new HashMap<>();
        CombatProperties combat = getCombatForSlot(attData, slotName);
        if (combat == null) return result;
        float percent = getDurabilityPercent(currentDura, maxDura);
        Map<String, Integer> sourceMap = precise ? combat.getToughnessSub() : combat.getToughness();
        if (sourceMap == null) return result;
        for (Map.Entry<String, Integer> entry : sourceMap.entrySet()) {
            int scaled = (int) Math.round(entry.getValue() * percent);
            if (scaled > 0) result.put(entry.getKey(), scaled);
        }
        return result;
    }

    private static float getDurabilityPercent(int currentDura, int maxDura) {
        if (maxDura <= 0) return 1.0f;
        if (currentDura <= 1) return 0.0f; // 耐久 <=1 时完全失去防护
        float percent = (float) currentDura / (float) maxDura;
        return Math.max(0.01f, Math.min(1.0f, percent));
    }

    public static class DynamicStats {
        public int primaryLevel = 0;
        public int primaryToughness = 0;
        public float primaryRicochet = 0f;
        public Map<String, Integer> primaryLevelsMap = new HashMap<>();
        public Map<String, Integer> primaryToughnessMap = new HashMap<>();
        public Map<String, Integer> subLevels = new HashMap<>();
        public Map<String, Integer> subToughness = new HashMap<>();
        public Map<String, Float> subRicochet = new HashMap<>();
    }
}