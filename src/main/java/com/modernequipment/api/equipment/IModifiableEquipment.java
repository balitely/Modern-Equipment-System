package com.modernequipment.api.equipment;

import com.modernequipment.api.attachment.AttachmentType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import java.util.Map;

public interface IModifiableEquipment {

    Map<AttachmentType, ResourceLocation> getAttachments(ItemStack stack);

    boolean allowAttachment(ItemStack equipment, ItemStack attachment);

    boolean installAttachment(ItemStack equipment, ItemStack attachment, AttachmentType slot);

    ItemStack uninstallAttachment(ItemStack equipment, AttachmentType slot);

    AttachmentType[] getAllowedAttachmentTypes(ItemStack equipment);

    static IModifiableEquipment getModifiableOrNull(ItemStack stack) {
        if (stack.getItem() instanceof IModifiableEquipment me) {
            return me;
        }
        return null;
    }
}