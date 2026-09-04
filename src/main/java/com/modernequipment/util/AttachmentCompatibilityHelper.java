package com.modernequipment.util;

import com.modernequipment.core.data.AttachmentData;
import com.modernequipment.core.data.AttachmentData.Compatible;
import com.modernequipment.core.data.EquipmentData;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

public class AttachmentCompatibilityHelper {

    public static boolean isCompatible(ItemStack attachmentStack, ItemStack equipmentStack,
                                       EquipmentData equipmentData, AttachmentData attachmentData) {
        if (equipmentStack.isEmpty() || attachmentStack.isEmpty()) return false;

        if (!isTypeCompatible(equipmentData, attachmentData)) {
            return false;
        }

        if (!isParentTypeCompatible(equipmentData, attachmentData)) {
            return false;
        }

        return isDetailedCompatible(attachmentStack, equipmentStack, attachmentData);
    }

    private static boolean isTypeCompatible(EquipmentData equipmentData, AttachmentData attachmentData) {
        List<String> allowedTypes = equipmentData.getAllowAttachmentTypes();
        if (allowedTypes == null || allowedTypes.isEmpty()) {
            return true;
        }
        String attachmentType = attachmentData.getType();
        if (attachmentType == null) return false;
        return containsIgnoreCase(allowedTypes, attachmentType);
    }

    private static boolean isParentTypeCompatible(EquipmentData equipmentData, AttachmentData attachmentData) {
        List<String> parentTypes = attachmentData.getCompatibleParentTypes();
        if (parentTypes == null || parentTypes.isEmpty()) {
            return true;
        }
        String equipmentType = equipmentData.getType();
        if (equipmentType != null && containsIgnoreCase(parentTypes, equipmentType)) {
            return true;
        }

        // Backward-compatible armored-rig rule: historical plate packs usually
        // declared only body_armor as their parent.  A chest_rig that explicitly
        // allows armor attachment types is an armored chest rig / plate carrier,
        // so those body-armor protection attachments are valid on it as well.
        if ("chest_rig".equalsIgnoreCase(equipmentType)
                && containsIgnoreCase(parentTypes, "body_armor")
                && isBodyProtectionAttachment(attachmentData.getType())
                && equipmentAllowsType(equipmentData, attachmentData.getType())) {
            return true;
        }
        return false;
    }

    private static boolean equipmentAllowsType(EquipmentData equipmentData, String attachmentType) {
        List<String> allowed = equipmentData.getAllowAttachmentTypes();
        // The body_armor alias is only for chest rigs that explicitly opt into
        // protective attachments.  A plain rig with no allow-list must not
        // accidentally become a plate carrier.
        return allowed != null && !allowed.isEmpty() && containsIgnoreCase(allowed, attachmentType);
    }

    private static boolean isBodyProtectionAttachment(String type) {
        if (type == null) return false;
        return switch (type.toLowerCase(java.util.Locale.ROOT)) {
            case "armor_plate", "front_plate", "back_plate", "side_plate",
                    "groin_plate", "neck_armor" -> true;
            default -> false;
        };
    }

    private static boolean containsIgnoreCase(List<String> values, String wanted) {
        if (values == null || wanted == null) return false;
        for (String value : values) {
            if (value != null && value.equalsIgnoreCase(wanted)) return true;
        }
        return false;
    }

    private static boolean isDetailedCompatible(ItemStack attachmentStack, ItemStack equipmentStack,
                                                AttachmentData attachmentData) {
        Compatible compatible = attachmentData.getCompatible();
        if (compatible == null || compatible.isEmpty()) {
            return true;
        }

        List<String> allowedIds = compatible.getIds();
        if (allowedIds != null && !allowedIds.isEmpty()) {
            ResourceLocation equipmentId = ForgeRegistries.ITEMS.getKey(equipmentStack.getItem());
            if (equipmentId != null && allowedIds.contains(equipmentId.toString())) {
                return true;
            }
        }

        List<String> allowedTags = compatible.getTags();
        if (allowedTags != null && !allowedTags.isEmpty()) {
            for (String tagName : allowedTags) {
                TagKey<Item> tagKey = TagKey.create(BuiltInRegistries.ITEM.key(), new ResourceLocation(tagName));
                if (equipmentStack.is(tagKey)) {
                    return true;
                }
            }
        }

        return false;
    }
}