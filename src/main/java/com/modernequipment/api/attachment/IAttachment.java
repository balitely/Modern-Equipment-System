package com.modernequipment.api.attachment;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public interface IAttachment {
    AttachmentType getType(ItemStack stack);
    ResourceLocation getAttachmentId(ItemStack stack);

    static IAttachment getIAttachmentOrNull(ItemStack stack) {
        if (stack.getItem() instanceof IAttachment att) {
            return att;
        }
        return null;
    }
}