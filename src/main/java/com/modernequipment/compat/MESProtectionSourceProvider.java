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
import com.moderndamage.control.api.IProtectionSourceProvider;
import com.moderndamage.control.api.ModDamageSubPart;
import com.moderndamage.control.api.ProtectionSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

public class MESProtectionSourceProvider implements IProtectionSourceProvider {

    private static final String ATTACHMENTS_DURABILITY_KEY = "AttachmentsDurability";

    @Override
    public List<ProtectionSource> getAdditionalSources(ItemStack stack, LivingEntity target) {
        MESMod.LOGGER.debug("getAdditionalSources called for stack={}, target={}", stack, target);
        List<ProtectionSource> sources = new ArrayList<>();
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
                int effectiveDura = currentDura - 1;  // 耐久为 1 时比例为 0
                if (effectiveDura <= 0) continue;    // 双重保险
                durabilityPercent = (float) effectiveDura / (float) maxDura;
                durabilityPercent = Math.max(0.0f, Math.min(1.0f, durabilityPercent));
            }

            int primaryLevel = 0;
            if (effectiveCombat.getArmorLevels() != null && !effectiveCombat.getArmorLevels().isEmpty()) {
                primaryLevel = effectiveCombat.getArmorLevels().values().stream().max(Integer::compare).orElse(0);
            }
            int dynamicPrimaryLevel = (int) Math.round(primaryLevel * durabilityPercent);
            if (dynamicPrimaryLevel < 0) dynamicPrimaryLevel = 0;

            int primaryToughness = 0;
            if (effectiveCombat.getToughness() != null && !effectiveCombat.getToughness().isEmpty()) {
                primaryToughness = effectiveCombat.getToughness().values().stream().max(Integer::compare).orElse(0);
            }
            int dynamicPrimaryToughness = (int) Math.round(primaryToughness * durabilityPercent);
            if (dynamicPrimaryToughness < 0) dynamicPrimaryToughness = 0;

            float primaryRicochet = 0.0f;
            if (effectiveCombat.getRicochetChance() != null && !effectiveCombat.getRicochetChance().isEmpty()) {
                primaryRicochet = effectiveCombat.getRicochetChance().values().stream().max(Float::compare).orElse(0.0f);
            }
            float dynamicPrimaryRicochet = primaryRicochet * durabilityPercent;
            if (dynamicPrimaryRicochet < 0) dynamicPrimaryRicochet = 0;

            Map<ModDamageSubPart, Integer> scaledSubProtection = new HashMap<>();
            if (effectiveCombat.getArmorLevelsSub() != null) {
                for (Map.Entry<String, Integer> subEntry : effectiveCombat.getArmorLevelsSub().entrySet()) {
                    ModDamageSubPart subPart = ModDamageSubPart.bySubKey(subEntry.getKey());
                    if (subPart != null) {
                        int scaled = (int) Math.round(subEntry.getValue() * durabilityPercent);
                        if (scaled > 0) {
                            scaledSubProtection.put(subPart, scaled);
                        }
                    }
                }
            }

            Map<ModDamageSubPart, Integer> scaledSubToughness = new HashMap<>();
            if (effectiveCombat.getToughnessSub() != null) {
                for (Map.Entry<String, Integer> subEntry : effectiveCombat.getToughnessSub().entrySet()) {
                    ModDamageSubPart subPart = ModDamageSubPart.bySubKey(subEntry.getKey());
                    if (subPart != null) {
                        int scaled = (int) Math.round(subEntry.getValue() * durabilityPercent);
                        if (scaled > 0) {
                            scaledSubToughness.put(subPart, scaled);
                        }
                    }
                }
            }

            Map<ModDamageSubPart, Float> scaledSubRicochet = new HashMap<>();
            if (effectiveCombat.getRicochetSub() != null) {
                for (Map.Entry<String, Float> subEntry : effectiveCombat.getRicochetSub().entrySet()) {
                    ModDamageSubPart subPart = ModDamageSubPart.bySubKey(subEntry.getKey());
                    if (subPart != null) {
                        float scaled = subEntry.getValue() * durabilityPercent;
                        if (scaled > 0.01f) {
                            scaledSubRicochet.put(subPart, scaled);
                        }
                    }
                }
            }

            float materialFactor = 1.0f;
            if (effectiveCombat.getMaterialFactor() != null && !effectiveCombat.getMaterialFactor().isEmpty()) {
                materialFactor = effectiveCombat.getMaterialFactor().values().stream().findFirst().orElse(1.0f);
            }

            ItemStack virtualStack = new ItemStack(ForgeRegistries.ITEMS.getValue(attId));
            ProtectionSource source = new ProtectionSource(
                    virtualStack,
                    scaledSubProtection,
                    scaledSubToughness,
                    scaledSubRicochet,
                    materialFactor,
                    null,
                    dynamicPrimaryLevel,
                    dynamicPrimaryToughness,
                    dynamicPrimaryRicochet,
                    false
            );
            sources.add(source);

            if (MESMod.LOGGER.isDebugEnabled()) {
                MESMod.LOGGER.debug("Attachment {} in slot {}: durability={}/{}, primaryLevel {}->{}, primaryToughness {}->{}, primaryRicochet {}->{}",
                        attId, slot, currentDura, maxDura, primaryLevel, dynamicPrimaryLevel,
                        primaryToughness, dynamicPrimaryToughness, primaryRicochet, dynamicPrimaryRicochet);
            }
        }
        MESMod.LOGGER.debug("Returning {} protection sources (dynamic scaling applied)", sources.size());
        return sources;
    }
}