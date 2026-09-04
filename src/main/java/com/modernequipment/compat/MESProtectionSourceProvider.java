package com.modernequipment.compat;

import com.moderndamage.control.api.IProtectionSourceProvider;
import com.moderndamage.control.api.ModDamageSubPart;
import com.moderndamage.control.api.ProtectionSource;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntConsumer;

/** MDC 1.0.32 native protection provider for MES removable armor attachments. */
public class MESProtectionSourceProvider implements IProtectionSourceProvider {
    private static final String ATTACHMENTS_DURABILITY_KEY = "AttachmentsDurability";

    @Override
    public List<ProtectionSource> getAdditionalSources(ItemStack stack, LivingEntity target) {
        List<ProtectionSource> sources = new ArrayList<>();
        if (!(stack.getItem() instanceof IModifiableEquipment modifiable)) return sources;

        EquipmentData equipmentData = getEquipmentData(stack);
        if (equipmentData == null) return sources;

        CompoundTag root = stack.getOrCreateTag();
        CompoundTag durabilityTag = root.getCompound(ATTACHMENTS_DURABILITY_KEY);

        for (Map.Entry<AttachmentType, ResourceLocation> entry : modifiable.getAttachments(stack).entrySet()) {
            AttachmentType slot = entry.getKey();
            ResourceLocation attachmentId = entry.getValue();
            AttachmentData attachment = EquipmentDataManager.getAttachment(attachmentId);
            if (attachment == null) continue;

            CombatProperties combat = getEffectiveCombat(attachment, slot);
            if (combat == null) continue;

            int maxDurability = Math.max(0, attachment.getDurability());
            String durabilityKey = slot.name() + "_" + attachmentId;
            int currentDurability = maxDurability;
            if (maxDurability > 0) {
                if (durabilityTag.contains(durabilityKey)) {
                    currentDurability = Math.max(0, durabilityTag.getInt(durabilityKey));
                } else {
                    durabilityTag.putInt(durabilityKey, maxDurability);
                    root.put(ATTACHMENTS_DURABILITY_KEY, durabilityTag);
                }
                if (currentDurability <= 0) continue;
            }

            float durabilityRatio = maxDurability > 0
                    ? Math.max(0.0f, Math.min(1.0f, (float) currentDurability / (float) maxDurability))
                    : 1.0f;

            Map<ModDamageSubPart, Integer> protection = buildIntSubMap(
                    combat.getArmorLevels(), combat.getArmorLevelsSub(), durabilityRatio, true);
            Map<ModDamageSubPart, Integer> toughness = buildIntSubMap(
                    combat.getToughness(), combat.getToughnessSub(), durabilityRatio, false);
            Map<ModDamageSubPart, Float> ricochet = buildFloatSubMap(
                    combat.getRicochetChance(), combat.getRicochetSub(), durabilityRatio);

            if (protection.isEmpty() && toughness.isEmpty() && ricochet.isEmpty()) continue;

            float materialFactor = resolveMaterialFactor(combat);
            Item attachmentItem = ForgeRegistries.ITEMS.getValue(attachmentId);
            ItemStack sourceStack = attachmentItem == null ? stack : new ItemStack(attachmentItem);

            IntConsumer durabilityConsumer = null;
            if (maxDurability > 0) {
                durabilityConsumer = loss -> {
                    if (loss <= 0) return;
                    CompoundTag liveRoot = stack.getOrCreateTag();
                    CompoundTag liveDurability = liveRoot.getCompound(ATTACHMENTS_DURABILITY_KEY);
                    int oldValue = liveDurability.contains(durabilityKey)
                            ? liveDurability.getInt(durabilityKey)
                            : maxDurability;
                    liveDurability.putInt(durabilityKey, Math.max(0, oldValue - loss));
                    liveRoot.put(ATTACHMENTS_DURABILITY_KEY, liveDurability);
                };
            }

            sources.add(new ProtectionSource(
                    sourceStack,
                    protection,
                    toughness,
                    ricochet,
                    materialFactor,
                    durabilityConsumer));
        }
        return sources;
    }

    private static EquipmentData getEquipmentData(ItemStack stack) {
        if (stack.getItem() instanceof EquipmentArmorItem armorItem) return armorItem.getData();
        if (stack.getItem() instanceof EquipmentItem equipmentItem) return equipmentItem.getData();
        return null;
    }

    private static CombatProperties getEffectiveCombat(AttachmentData attachment, AttachmentType slot) {
        if (attachment.getMountEffects() != null) {
            CombatProperties mounted = attachment.getMountEffects().get(slot.name().toLowerCase());
            if (mounted != null) return mounted;
        }
        return attachment.getCombat();
    }

    private static Map<ModDamageSubPart, Integer> buildIntSubMap(
            Map<String, Integer> parentValues,
            Map<String, Integer> subValues,
            float durabilityRatio,
            boolean minimumOneWhenPositive) {
        Map<ModDamageSubPart, Integer> result = new EnumMap<>(ModDamageSubPart.class);
        if (parentValues != null) {
            parentValues.forEach((key, value) -> {
                if (value == null || value <= 0) return;
                int scaled = scaleInt(value, durabilityRatio, minimumOneWhenPositive);
                for (ModDamageSubPart sub : childrenOf(key)) result.put(sub, scaled);
            });
        }
        if (subValues != null) {
            subValues.forEach((key, value) -> {
                if (value == null || value <= 0) return;
                ModDamageSubPart sub = ModDamageSubPart.bySubKey(key == null ? null : key.toLowerCase(java.util.Locale.ROOT));
                if (sub != null) result.put(sub, scaleInt(value, durabilityRatio, minimumOneWhenPositive));
            });
        }
        return result;
    }

    private static Map<ModDamageSubPart, Float> buildFloatSubMap(
            Map<String, Float> parentValues,
            Map<String, Float> subValues,
            float durabilityRatio) {
        Map<ModDamageSubPart, Float> result = new EnumMap<>(ModDamageSubPart.class);
        if (parentValues != null) {
            parentValues.forEach((key, value) -> {
                if (value == null || value <= 0.0f) return;
                float scaled = clamp01(value * durabilityRatio);
                for (ModDamageSubPart sub : childrenOf(key)) result.put(sub, scaled);
            });
        }
        if (subValues != null) {
            subValues.forEach((key, value) -> {
                if (value == null || value <= 0.0f) return;
                ModDamageSubPart sub = ModDamageSubPart.bySubKey(key == null ? null : key.toLowerCase(java.util.Locale.ROOT));
                if (sub != null) result.put(sub, clamp01(value * durabilityRatio));
            });
        }
        return result;
    }

    private static int scaleInt(int value, float ratio, boolean minimumOneWhenPositive) {
        int scaled = Math.round(value * ratio);
        return minimumOneWhenPositive && value > 0 && ratio > 0.0f ? Math.max(1, scaled) : Math.max(0, scaled);
    }

    private static float clamp01(float value) {
        return Math.max(0.0f, Math.min(1.0f, value));
    }

    private static float resolveMaterialFactor(CombatProperties combat) {
        Map<String, Float> values = combat.getMaterialFactor();
        float value = averagePositive(values);
        if (!Float.isNaN(value)) return value;

        // MDC 1.0.32 ProtectionSource exposes one material factor per source, not
        // a sub-part map.  Still parse material_factor_sub and use its average as
        // a compatibility fallback when no parent material_factor was supplied.
        value = averagePositive(combat.getMaterialFactorSub());
        return Float.isNaN(value) ? 1.0f : value;
    }

    private static float averagePositive(Map<String, Float> values) {
        if (values == null || values.isEmpty()) return Float.NaN;
        float total = 0.0f;
        int count = 0;
        for (Float value : values.values()) {
            if (value != null && value > 0.0f) {
                total += value;
                count++;
            }
        }
        return count == 0 ? Float.NaN : total / count;
    }

    private static List<ModDamageSubPart> childrenOf(String parentKey) {
        if (parentKey == null) return List.of();
        return switch (parentKey.toLowerCase()) {
            case "head" -> List.of(ModDamageSubPart.HEAD_TOP, ModDamageSubPart.HEAD_FACE, ModDamageSubPart.HEAD_NECK);
            case "chest" -> List.of(ModDamageSubPart.CHEST_FRONT, ModDamageSubPart.CHEST_BACK);
            case "stomach", "abdomen" -> List.of(ModDamageSubPart.STOMACH_FRONT, ModDamageSubPart.STOMACH_BACK);
            case "left_arm" -> List.of(ModDamageSubPart.LEFT_SHOULDER, ModDamageSubPart.LEFT_FOREARM);
            case "right_arm" -> List.of(ModDamageSubPart.RIGHT_SHOULDER, ModDamageSubPart.RIGHT_FOREARM);
            case "left_leg" -> List.of(ModDamageSubPart.LEFT_THIGH, ModDamageSubPart.LEFT_CALF, ModDamageSubPart.LEFT_FOOT);
            case "right_leg" -> List.of(ModDamageSubPart.RIGHT_THIGH, ModDamageSubPart.RIGHT_CALF, ModDamageSubPart.RIGHT_FOOT);
            default -> List.of();
        };
    }
}
