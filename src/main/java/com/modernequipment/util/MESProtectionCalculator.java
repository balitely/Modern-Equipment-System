package com.modernequipment.util;

import com.modernequipment.api.attachment.AttachmentType;
import com.modernequipment.api.equipment.IModifiableEquipment;
import com.modernequipment.compat.ModernDamageCompat;
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

/**
 * 防护计算器。不再直接依赖 MDC 类型，改用 String 部位名，
 * 内部通过 ModernDamageCompat 处理 MDC 相关逻辑。
 */
public class MESProtectionCalculator {

    private static final String ATTACHMENTS_DURABILITY_KEY = "AttachmentsDurability";

    /** 获取指定部位的堆叠防护等级 */
    public static int getTotalProtectionLevel(ItemStack equipment, IModifiableEquipment modifiable, String partName) {
        List<Integer> levels = collectProtectionLevelsForPart(equipment, modifiable, partName);
        return ModernDamageCompat.calculateStackedLevel(levels);
    }

    /** 获取指定子部位的堆叠防护等级 */
    public static int getTotalSubProtectionLevel(ItemStack equipment, IModifiableEquipment modifiable, String subPartKey) {
        List<Integer> levels = collectProtectionLevelsForSubPart(equipment, modifiable, subPartKey);
        return ModernDamageCompat.calculateStackedLevel(levels);
    }

    /** 获取指定部位的韧性 */
    public static int getTotalToughness(ItemStack equipment, IModifiableEquipment modifiable, String partName) {
        int total = 0;
        EquipmentData data = getEquipmentData(equipment);
        if (data != null && data.getCombat() != null && data.getCombat().getToughness() != null) {
            total += applyToughnessScaling(equipment, data.getCombat().getToughness().getOrDefault(partName, 0));
        }
        total += getAttachmentToughness(equipment, modifiable, partName, false);
        return Math.min(total, 100);
    }

    /** 获取指定子部位的韧性 */
    public static int getTotalSubToughness(ItemStack equipment, IModifiableEquipment modifiable, String subPartKey) {
        int total = 0;
        EquipmentData data = getEquipmentData(equipment);
        if (data != null && data.getCombat() != null && data.getCombat().getToughnessSub() != null) {
            total += applyToughnessScaling(equipment, data.getCombat().getToughnessSub().getOrDefault(subPartKey, 0));
        }
        total += getAttachmentToughness(equipment, modifiable, subPartKey, true);
        return Math.min(total, 100);
    }

    /** 获取指定部位的跳弹概率 */
    public static float getTotalRicochetChance(ItemStack equipment, IModifiableEquipment modifiable, String partName) {
        float max = 0;
        EquipmentData data = getEquipmentData(equipment);
        if (data != null && data.getCombat() != null && data.getCombat().getRicochetChance() != null) {
            max = Math.max(max, data.getCombat().getRicochetChance().getOrDefault(partName, 0f));
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
                ric = effectiveCombat.getRicochetChance().getOrDefault(partName, 0f);
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

    /** 收集主部位防护等级 */
    private static List<Integer> collectProtectionLevelsForPart(ItemStack equipment, IModifiableEquipment modifiable, String partName) {
        List<Integer> levels = new ArrayList<>();
        EquipmentData data = getEquipmentData(equipment);
        if (data != null && data.getCombat() != null && data.getCombat().getArmorLevels() != null) {
            int baseLevel = data.getCombat().getArmorLevels().getOrDefault(partName, 0);
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
            if (effectiveCombat.getArmorLevels() != null) {
                int plateLevel = effectiveCombat.getArmorLevels().getOrDefault(partName, 0);
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

    /** 收集子部位防护等级 */
    private static List<Integer> collectProtectionLevelsForSubPart(ItemStack equipment, IModifiableEquipment modifiable, String subPartKey) {
        List<Integer> levels = new ArrayList<>();
        EquipmentData data = getEquipmentData(equipment);
        if (data != null && data.getCombat() != null && data.getCombat().getArmorLevelsSub() != null) {
            int baseLevel = data.getCombat().getArmorLevelsSub().getOrDefault(subPartKey, 0);
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
                int plateLevel = effectiveCombat.getArmorLevelsSub().getOrDefault(subPartKey, 0);
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

    /** 收集配件韧性（主部位或子部位） */
    private static int getAttachmentToughness(ItemStack equipment, IModifiableEquipment modifiable, String key, boolean sub) {
        int total = 0;
        Map<AttachmentType, ResourceLocation> attachments = modifiable.getAttachments(equipment);
        for (Map.Entry<AttachmentType, ResourceLocation> entry : attachments.entrySet()) {
            AttachmentType slot = entry.getKey();
            ResourceLocation attId = entry.getValue();
            AttachmentData attData = EquipmentDataManager.getAttachment(attId);
            if (attData == null) continue;
            CombatProperties effectiveCombat = getEffectiveCombat(attData, slot);
            if (effectiveCombat == null) continue;
            int toughness = 0;
            if (sub && effectiveCombat.getToughnessSub() != null) {
                toughness = effectiveCombat.getToughnessSub().getOrDefault(key, 0);
            } else if (!sub && effectiveCombat.getToughness() != null) {
                toughness = effectiveCombat.getToughness().getOrDefault(key, 0);
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
        return total;
    }

    // ==================== 缩放辅助 ====================

    private static int applyProtectionScaling(ItemStack stack, int originalLevel) {
        if (originalLevel <= 0) return 0;
        int maxDura = stack.getMaxDamage();
        int curDura = maxDura - stack.getDamageValue();
        if (maxDura <= 0) return originalLevel;
        if (curDura <= 1) return 0;
        float percent = (float) curDura / maxDura;
        float multiplier = 0.5f + 0.5f * percent;
        int scaled = Math.round(originalLevel * multiplier);
        return Math.max(0, scaled);
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

    // ==================== 通用辅助 ====================

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

    private static EquipmentData getEquipmentData(ItemStack stack) {
        if (stack.getItem() instanceof EquipmentItem eq) return eq.getData();
        if (stack.getItem() instanceof EquipmentArmorItem armor) return armor.getData();
        return null;
    }
}