package com.modernequipment.compat;

import com.modernequipment.MESMod;
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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

/**
 * MES 保护来源数据提供器。
 * 不再直接实现 MDC 的 IProtectionSourceProvider 接口，
 * 而是作为一个纯数据类，由 ModernDamageCompat 通过反射包装后注册到 MDC。
 */
public class MESProtectionSourceProvider {

    private static final String ATTACHMENTS_DURABILITY_KEY = "AttachmentsDurability";

    /**
     * 收集额外的 ProtectionSource 数据。
     * 返回的结构体列表将被转换为 MDC 的 ProtectionSource 对象。
     */
    public List<ProtectionSourceEntry> getAdditionalSources(ItemStack stack, LivingEntity target) {
        MESMod.LOGGER.debug("getAdditionalSources called for stack={}, target={}", stack, target);
        List<ProtectionSourceEntry> sources = new ArrayList<>();
        if (!(stack.getItem() instanceof IModifiableEquipment modifiable)) {
            return sources;
        }

        EquipmentData equipmentData = null;
        if (stack.getItem() instanceof EquipmentItem eqItem) {
            equipmentData = eqItem.getData();
        } else if (stack.getItem() instanceof EquipmentArmorItem armorItem) {
            equipmentData = armorItem.getData();
        }
        if (equipmentData == null) return sources;

        CompoundTag equipmentTag = stack.getOrCreateTag();
        CompoundTag durabilityTag = equipmentTag.getCompound(ATTACHMENTS_DURABILITY_KEY);

        Map<AttachmentType, ResourceLocation> attachments = modifiable.getAttachments(stack);
        for (Map.Entry<AttachmentType, ResourceLocation> entry : attachments.entrySet()) {
            AttachmentType slot = entry.getKey();
            ResourceLocation attId = entry.getValue();
            AttachmentData attData = EquipmentDataManager.getAttachment(attId);
            if (attData == null) continue;

            int maxDura = attData.getDurability();
            String durabilityKey = slot.name() + "_" + attId.toString();
            int currentDura = maxDura;
            if (durabilityTag.contains(durabilityKey)) {
                currentDura = durabilityTag.getInt(durabilityKey);
            } else if (maxDura > 0) {
                durabilityTag.putInt(durabilityKey, maxDura);
                currentDura = maxDura;
                equipmentTag.put(ATTACHMENTS_DURABILITY_KEY, durabilityTag);
            }

            if (currentDura <= 1) continue;

            CombatProperties effectiveCombat = null;
            if (attData.getMountEffects() != null && attData.getMountEffects().containsKey(slot.name().toLowerCase())) {
                effectiveCombat = attData.getMountEffects().get(slot.name().toLowerCase());
            } else if (attData.getCombat() != null) {
                effectiveCombat = attData.getCombat();
            }
            if (effectiveCombat == null) continue;

            float durabilityPercent = 1.0f;
            if (maxDura > 0) {
                int effectiveDura = currentDura - 1;
                if (effectiveDura <= 0) continue;
                durabilityPercent = (float) effectiveDura / (float) maxDura;
                durabilityPercent = Math.max(0.0f, Math.min(1.0f, durabilityPercent));
            }

            ProtectionSourceEntry entryData = new ProtectionSourceEntry();
            entryData.attachmentId = attId;
            entryData.armorLevels = effectiveCombat.getArmorLevels();
            entryData.toughness = effectiveCombat.getToughness();
            entryData.ricochetChance = effectiveCombat.getRicochetChance();
            entryData.armorLevelsSub = effectiveCombat.getArmorLevelsSub();
            entryData.toughnessSub = effectiveCombat.getToughnessSub();
            entryData.ricochetSub = effectiveCombat.getRicochetSub();
            entryData.materialFactor = effectiveCombat.getMaterialFactor();
            entryData.durabilityPercent = durabilityPercent;
            sources.add(entryData);

            if (MESMod.LOGGER.isDebugEnabled()) {
                MESMod.LOGGER.debug("Attachment {} in slot {}: durability={}/{}",
                        attId, slot, currentDura, maxDura);
            }
        }
        MESMod.LOGGER.debug("Returning {} protection source entries (dynamic scaling applied)", sources.size());
        return sources;
    }

    public static class ProtectionSourceEntry {
        public ResourceLocation attachmentId;
        /** 主部位防护等级 Map<部位名, 等级> */
        public Map<String, Integer> armorLevels;
        /** 主部位韧性 */
        public Map<String, Integer> toughness;
        /** 主部位跳弹 */
        public Map<String, Float> ricochetChance;
        /** 子部位防护等级 Map<subKey, 等级> */
        public Map<String, Integer> armorLevelsSub;
        /** 子部位韧性 */
        public Map<String, Integer> toughnessSub;
        /** 子部位跳弹 */
        public Map<String, Float> ricochetSub;
        /** 材质系数 Map<部位, 系数> */
        public Map<String, Float> materialFactor;
        /** 耐久比例 0~1 */
        public float durabilityPercent = 1.0f;
    }
}