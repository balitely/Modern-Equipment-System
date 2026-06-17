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
        return allowedTypes.contains(attachmentType);
    }

    private static boolean isParentTypeCompatible(EquipmentData equipmentData, AttachmentData attachmentData) {
        List<String> parentTypes = attachmentData.getCompatibleParentTypes();
        if (parentTypes == null || parentTypes.isEmpty()) {
            return true;
        }
        String equipmentType = equipmentData.getType();
        return equipmentType != null && parentTypes.contains(equipmentType);
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