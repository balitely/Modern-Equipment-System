package com.modernequipment.core.item;

import com.modernequipment.MESMod;
import com.modernequipment.api.attachment.AttachmentType;
import com.modernequipment.api.attachment.IAttachment;
import com.modernequipment.core.data.AttachmentData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import javax.annotation.Nonnull;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.util.UUID;

public class AttachmentItem extends Item implements ICurioItem, IAttachment {
    private final AttachmentData data;

    public AttachmentItem(Properties properties, AttachmentData data) {
        super(properties.durability(data.getDurability()));
        this.data = data;
    }

    public AttachmentData getData() {
        return data;
    }

    @Override
    public AttachmentType getType(ItemStack stack) {
        return AttachmentType.fromString(data.getType());
    }

    @Override
    public ResourceLocation getAttachmentId(ItemStack stack) {
        return new ResourceLocation(MESMod.MODID, data.getId());
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(String identifier, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> modifiers = HashMultimap.create();
        if (data == null || data.getModifiers() == null) return modifiers;

        float moveSpeed = data.getModifiers().getMovementSpeed();
        float ergonomics = data.getModifiers().getErgonomics();

        if (moveSpeed != 0) {
            modifiers.put(Attributes.MOVEMENT_SPEED, new AttributeModifier(
                    UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890"),
                    "MES Attachment Movement Speed",
                    moveSpeed,
                    AttributeModifier.Operation.MULTIPLY_TOTAL
            ));
        }

        if (ergonomics != 0) {
            Attribute ergonomicsAttr = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation("moderndamage", "ergonomics"));
            if (ergonomicsAttr != null) {
                modifiers.put(ergonomicsAttr, new AttributeModifier(
                        UUID.fromString("b2c3d4e5-f6a7-8901-bcde-f23456789012"),
                        "MES Attachment Ergonomics",
                        ergonomics,
                        AttributeModifier.Operation.ADDITION
                ));
            }
        }
        return modifiers;
    }
}