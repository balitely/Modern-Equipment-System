package com.modernequipment.util;

import com.moderndamage.control.api.ModDamagePart;
import com.moderndamage.control.api.ModDamageSubPart;
import com.moderndamage.control.config.ModClothConfig;
import com.modernequipment.api.attachment.AttachmentType;
import com.modernequipment.api.equipment.IModifiableEquipment;
import com.modernequipment.core.data.AttachmentData;
import com.modernequipment.core.data.CombatProperties;
import com.modernequipment.core.data.EquipmentData;
import com.modernequipment.core.item.EquipmentArmorItem;
import com.modernequipment.core.item.EquipmentItem;
import com.modernequipment.core.loader.EquipmentDataManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.*;

public class MESProtectionCalculator {

    private static final String ATTACHMENTS_DURABILITY_KEY = "AttachmentsDurability";

    public static int getTotalProtectionLevel(ItemStack equipment, IModifiableEquipment modifiable, ModDamagePart part) {
        List<Integer> levels = collectProtectionLevelsForPart(equipment, modifiable, part);
        return calculateStackedLevel(levels);
    }

    public static int getTotalSubProtectionLevel(ItemStack equipment, IModifiableEquipment modifiable, ModDamageSubPart subPart) {
        List<Integer> levels = collectProtectionLevelsForSubPart(equipment, modifiable, subPart);
        return calculateStackedLevel(levels);
    }

    public static int getTotalToughness(ItemStack equipment, IModifiableEquipment modifiable, ModDamagePart part) {
        int total = 0;
        // 本体韧性（应用动态缩放，耐久 ≤1 时归零）
        EquipmentData data = getEquipmentData(equipment);
        if (data != null && data.getCombat() != null && data.getCombat().getToughness() != null) {
            total += applyToughnessScaling(equipment, data.getCombat().getToughness().getOrDefault(part.name(), 0));
        }
        // 配件韧性
        Map<AttachmentType, ResourceLocation> attachments = modifiable.getAttachments(equipment);
        for (Map.Entry<AttachmentType, ResourceLocation> entry : attachments.entrySet()) {
            AttachmentType slot = entry.getKey();
            ResourceLocation attId = entry.getValue();
            AttachmentData attData = EquipmentDataManager.getAttachment(attId);
            if (attData == null) continue;
            CombatProperties effectiveCombat = getEffectiveCombat(attData, slot);
            if (effectiveCombat == null) continue;
            int toughness = 0;
            if (effectiveCombat.getToughness() != null) {
                toughness = effectiveCombat.getToughness().getOrDefault(part.name(), 0);
            }
            if (toughness > 0) {
                int maxDura = attData.getDurability();
                int currentDura = getAttachmentCurrentDura(equipment, slot, attId, maxDura);
                // 配件耐久 ≤1 时不提供任何属性（已在外部跳过，但保险再判断一次）
                if (currentDura <= 1) continue;
                float percent = maxDura > 0 ? (float) currentDura / maxDura : 1.0f;
                percent = Math.max(0.0f, Math.min(1.0f, percent));
                total += (int) Math.round(toughness * percent);
            }
        }
        return Math.min(total, 100);
    }

    public static int getTotalSubToughness(ItemStack equipment, IModifiableEquipment modifiable, ModDamageSubPart subPart) {
        int total = 0;
        EquipmentData data = getEquipmentData(equipment);
        if (data != null && data.getCombat() != null && data.getCombat().getToughnessSub() != null) {
            total += applyToughnessScaling(equipment, data.getCombat().getToughnessSub().getOrDefault(subPart.getSubKey(), 0));
        }
        Map<AttachmentType, ResourceLocation> attachments = modifiable.getAttachments(equipment);
        for (Map.Entry<AttachmentType, ResourceLocation> entry : attachments.entrySet()) {
            AttachmentType slot = entry.getKey();
            ResourceLocation attId = entry.getValue();
            AttachmentData attData = EquipmentDataManager.getAttachment(attId);
            if (attData == null) continue;
            CombatProperties effectiveCombat = getEffectiveCombat(attData, slot);
            if (effectiveCombat == null) continue;
            int toughness = 0;
            if (effectiveCombat.getToughnessSub() != null) {
                toughness = effectiveCombat.getToughnessSub().getOrDefault(subPart.getSubKey(), 0);
            }
            if (toughness > 0) {
                int maxDura = attData.getDurability();
                int currentDura = getAttachmentCurrentDura(equipment, slot, attId, maxDura);
                if (currentDura <= 1) continue;
                float percent = maxDura > 0 ? (float) currentDura / maxDura : 1.0f;
                percent = Math.max(0.0f, Math.min(1.0f, percent));
                total += (int) Math.round(toughness * percent);
            }
        }
        return Math.min(total, 100);
    }

    public static float getTotalRicochetChance(ItemStack equipment, IModifiableEquipment modifiable, ModDamagePart part) {
        float max = 0;
        EquipmentData data = getEquipmentData(equipment);
        if (data != null && data.getCombat() != null && data.getCombat().getRicochetChance() != null) {
            max = Math.max(max, data.getCombat().getRicochetChance().getOrDefault(part.name(), 0f));
        }
        Map<AttachmentType, ResourceLocation> attachments = modifiable.getAttachments(equipment);
        for (Map.Entry<AttachmentType, ResourceLocation> entry : attachments.entrySet()) {
            AttachmentType slot = entry.getKey();
            ResourceLocation attId = entry.getValue();
            AttachmentData attData = EquipmentDataManager.getAttachment(attId);
            if (attData == null) continue;
            CombatProperties effectiveCombat = getEffectiveCombat(attData, slot);
            if (effectiveCombat == null) continue;
            float ric = 0;
            if (effectiveCombat.getRicochetChance() != null) {
                ric = effectiveCombat.getRicochetChance().getOrDefault(part.name(), 0f);
            }
            if (ric > 0) {
                int maxDura = attData.getDurability();
                int currentDura = getAttachmentCurrentDura(equipment, slot, attId, maxDura);
                if (currentDura <= 1) continue;
                float percent = maxDura > 0 ? (float) currentDura / maxDura : 1.0f;
                percent = Math.max(0.0f, Math.min(1.0f, percent));
                ric *= percent;
                if (ric > max) max = ric;
            }
        }
        return max;
    }

    // ==================== 内部收集方法 ====================
    private static List<Integer> collectProtectionLevelsForPart(ItemStack equipment, IModifiableEquipment modifiable, ModDamagePart part) {
        List<Integer> levels = new ArrayList<>();
        // 本体防护等级（应用动态缩放，耐久≤1时归零）
        EquipmentData data = getEquipmentData(equipment);
        if (data != null && data.getCombat() != null && data.getCombat().getArmorLevels() != null) {
            int baseLevel = data.getCombat().getArmorLevels().getOrDefault(part.name(), 0);
            if (baseLevel > 0) {
                int scaled = applyProtectionScaling(equipment, baseLevel);
                if (scaled > 0) levels.add(scaled);
            }
        }
        // 配件
        Map<AttachmentType, ResourceLocation> attachments = modifiable.getAttachments(equipment);
        for (Map.Entry<AttachmentType, ResourceLocation> entry : attachments.entrySet()) {
            AttachmentType slot = entry.getKey();
            ResourceLocation attId = entry.getValue();
            AttachmentData attData = EquipmentDataManager.getAttachment(attId);
            if (attData == null) continue;
            CombatProperties effectiveCombat = getEffectiveCombat(attData, slot);
            if (effectiveCombat == null) continue;
            if (effectiveCombat.getArmorLevels() != null) {
                int plateLevel = effectiveCombat.getArmorLevels().getOrDefault(part.name(), 0);
                if (plateLevel > 0) {
                    int maxDura = attData.getDurability();
                    int currentDura = getAttachmentCurrentDura(equipment, slot, attId, maxDura);
                    if (currentDura <= 1) continue;
                    float percent = maxDura > 0 ? (float) currentDura / maxDura : 1.0f;
                    percent = Math.max(0.0f, Math.min(1.0f, percent));
                    int dynamicLevel = (int) Math.round(plateLevel * percent);
                    if (dynamicLevel > 0) levels.add(dynamicLevel);
                }
            }
        }
        return levels;
    }

    private static List<Integer> collectProtectionLevelsForSubPart(ItemStack equipment, IModifiableEquipment modifiable, ModDamageSubPart subPart) {
        List<Integer> levels = new ArrayList<>();
        EquipmentData data = getEquipmentData(equipment);
        if (data != null && data.getCombat() != null && data.getCombat().getArmorLevelsSub() != null) {
            int baseLevel = data.getCombat().getArmorLevelsSub().getOrDefault(subPart.getSubKey(), 0);
            if (baseLevel > 0) {
                int scaled = applyProtectionScaling(equipment, baseLevel);
                if (scaled > 0) levels.add(scaled);
            }
        }
        Map<AttachmentType, ResourceLocation> attachments = modifiable.getAttachments(equipment);
        for (Map.Entry<AttachmentType, ResourceLocation> entry : attachments.entrySet()) {
            AttachmentType slot = entry.getKey();
            ResourceLocation attId = entry.getValue();
            AttachmentData attData = EquipmentDataManager.getAttachment(attId);
            if (attData == null) continue;
            CombatProperties effectiveCombat = getEffectiveCombat(attData, slot);
            if (effectiveCombat == null) continue;
            if (effectiveCombat.getArmorLevelsSub() != null) {
                int plateLevel = effectiveCombat.getArmorLevelsSub().getOrDefault(subPart.getSubKey(), 0);
                if (plateLevel > 0) {
                    int maxDura = attData.getDurability();
                    int currentDura = getAttachmentCurrentDura(equipment, slot, attId, maxDura);
                    if (currentDura <= 1) continue;
                    float percent = maxDura > 0 ? (float) currentDura / maxDura : 1.0f;
                    percent = Math.max(0.0f, Math.min(1.0f, percent));
                    int dynamicLevel = (int) Math.round(plateLevel * percent);
                    if (dynamicLevel > 0) levels.add(dynamicLevel);
                }
            }
        }
        return levels;
    }

    // ==================== 动态缩放辅助方法 ====================
    /**
     * 防护等级动态缩放，遵循 MDC 公式 multiplier = 0.5 + 0.5 * percent。
     * 但增加规则：如果当前耐久 <= 1，则直接返回 0（彻底损坏）。
     * 缩放结果四舍五入后允许为 0。
     */
    private static int applyProtectionScaling(ItemStack stack, int originalLevel) {
        if (originalLevel <= 0) return 0;
        int maxDura = stack.getMaxDamage();
        int curDura = maxDura - stack.getDamageValue();
        if (maxDura <= 0) return originalLevel;  // 无耐久物品，不缩放
        if (curDura <= 1) return 0;               // 耐久 ≤1，完全失效
        float percent = (float) curDura / maxDura;
        float multiplier = 0.5f + 0.5f * percent;
        int scaled = Math.round(originalLevel * multiplier);
        return Math.max(0, scaled);               // 允许 0，不再强制为 1
    }

    private static int applyToughnessScaling(ItemStack stack, int originalToughness) {
        if (originalToughness <= 0) return 0;
        int maxDura = stack.getMaxDamage();
        int curDura = maxDura - stack.getDamageValue();
        if (maxDura <= 0) return originalToughness;
        if (curDura <= 1) return 0;
        float percent = (float) curDura / maxDura;
        return (int) Math.round(originalToughness * percent);
    }

    // ==================== 通用辅助方法 ====================
    private static CombatProperties getEffectiveCombat(AttachmentData attData, AttachmentType slot) {
        if (attData.getMountEffects() != null && attData.getMountEffects().containsKey(slot.name().toLowerCase())) {
            return attData.getMountEffects().get(slot.name().toLowerCase());
        }
        return attData.getCombat();
    }

    private static int getAttachmentCurrentDura(ItemStack equipment, AttachmentType slot, ResourceLocation attId, int maxDura) {
        if (maxDura <= 0) return maxDura;
        CompoundTag tag = equipment.getTag();
        if (tag == null) return maxDura;
        CompoundTag durTag = tag.getCompound(ATTACHMENTS_DURABILITY_KEY);
        String key = slot.name() + "_" + attId.toString();
        if (durTag.contains(key)) {
            return durTag.getInt(key);
        }
        return maxDura;
    }

    private static int calculateStackedLevel(List<Integer> levels) {
        if (levels.isEmpty()) return 0;
        levels.sort(Collections.reverseOrder());
        int highest = levels.get(0);
        if (levels.size() == 1) return highest;
        int otherSum = 0;
        for (int i = 1; i < levels.size(); i++) otherSum += levels.get(i);
        ModClothConfig config = ModClothConfig.get();
        float factor = config.armorStackingFactor;
        int total = highest + Math.round(otherSum * factor);
        int cap = config.armorCap;
        if (cap > 0 && total > cap) total = cap;
        return total;
    }

    private static EquipmentData getEquipmentData(ItemStack stack) {
        if (stack.getItem() instanceof EquipmentItem eq) return eq.getData();
        if (stack.getItem() instanceof EquipmentArmorItem armor) return armor.getData();
        return null;
    }
}