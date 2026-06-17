package com.modernequipment.client.gui.components.refit;

import com.mojang.blaze3d.systems.RenderSystem;
import com.modernequipment.MESMod;
import com.modernequipment.api.attachment.AttachmentType;
import com.modernequipment.api.equipment.IModifiableEquipment;
import com.modernequipment.client.gui.EquipmentRefitScreen;
import com.modernequipment.core.data.AttachmentData;
import com.modernequipment.core.loader.EquipmentDataManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.Locale;
import java.util.function.Consumer;

public class EquipmentAttachmentSlot extends Button implements IStackTooltip {
    private final AttachmentType type;
    private final Inventory inventory;
    private final int equipmentSlotIndex;
    private final String equipmentType;
    private final String nameKey;
    private boolean selected = false;
    private ItemStack attachmentItem = ItemStack.EMPTY;
    private final boolean allowed;

    public EquipmentAttachmentSlot(int pX, int pY, AttachmentType type, int equipmentSlotIndex,
                                   Inventory inventory, String equipmentType, boolean allowed, OnPress onPress) {
        super(pX, pY, EquipmentRefitScreen.SLOT_SIZE, EquipmentRefitScreen.SLOT_SIZE,
                Component.empty(), onPress, DEFAULT_NARRATION);
        this.type = type;
        this.inventory = inventory;
        this.equipmentSlotIndex = equipmentSlotIndex;
        this.equipmentType = equipmentType;
        this.nameKey = "attachment." + MESMod.MODID + "." + type.name().toLowerCase(Locale.ROOT);
        this.allowed = allowed;
        this.active = allowed;
    }

    @Override
    public void renderTooltip(Consumer<ItemStack> consumer) {
        if (this.isHoveredOrFocused() && !attachmentItem.isEmpty()) {
            consumer.accept(attachmentItem);
        }
    }

    @Override
    public void renderWidget(@Nonnull GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        if (this.isHoveredOrFocused()) {
            Font font = Minecraft.getInstance().font;
            int yOffset = this.getY() + 20;
            if (this.selected && !attachmentItem.isEmpty()) {
                yOffset = this.getY() + 30;
            }
            graphics.drawCenteredString(font, Component.translatable(nameKey),
                    this.getX() + this.getWidth() / 2, yOffset, ChatFormatting.WHITE.getColor());
        }

        ItemStack equipment = inventory.getItem(equipmentSlotIndex);
        IModifiableEquipment modifiable = IModifiableEquipment.getModifiableOrNull(equipment);
        if (modifiable == null) return;

        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();

        int x = this.getX();
        int y = this.getY();
        if (allowed && (isHoveredOrFocused() || selected)) {
            graphics.blit(EquipmentRefitScreen.SLOT_TEXTURE, x, y, 0, 0, width, height,
                    EquipmentRefitScreen.SLOT_SIZE, EquipmentRefitScreen.SLOT_SIZE);
        } else {
            graphics.blit(EquipmentRefitScreen.SLOT_TEXTURE, x + 1, y + 1, 1, 1, width - 2, height - 2,
                    EquipmentRefitScreen.SLOT_SIZE, EquipmentRefitScreen.SLOT_SIZE);
        }

        this.attachmentItem = getAttachmentItem(equipment, modifiable);
        if (!attachmentItem.isEmpty()) {
            graphics.renderItem(attachmentItem, x + 1, y + 1);
            graphics.renderItemDecorations(Minecraft.getInstance().font, attachmentItem, x + 1, y + 1);
        } else {
            int[] uv = getSlotTextureUV(type, equipmentType, allowed);
            if (uv != null) {
                graphics.blit(EquipmentRefitScreen.ICONS_TEXTURE, x + 2, y + 2, width - 4, height - 4,
                        uv[0], uv[1], EquipmentRefitScreen.ICON_UV_SIZE, EquipmentRefitScreen.ICON_UV_SIZE,
                        EquipmentRefitScreen.ICON_UV_SIZE * 7, EquipmentRefitScreen.ICON_UV_SIZE * 3);
            }
        }

        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    private ItemStack getAttachmentItem(ItemStack equipment, IModifiableEquipment modifiable) {
        ResourceLocation id = modifiable.getAttachments(equipment).get(type);
        if (id != null) {
            var item = ForgeRegistries.ITEMS.getValue(id);
            if (item != null) {
                CompoundTag tag = equipment.getTag();
                if (tag != null) {
                    CompoundTag durTag = tag.getCompound("AttachmentsDurability");
                    String durabilityKey = type.name() + "_" + id.toString();
                    int currentDura = durTag.getInt(durabilityKey);
                    int maxDura = item.getMaxDamage();
                    if (maxDura > 0) {
                        ItemStack stack = new ItemStack(item);
                        if (currentDura <= 1) {
                            stack.setDamageValue(maxDura);
                        } else if (currentDura < maxDura) {
                            stack.setDamageValue(maxDura - currentDura);
                        }
                        return stack;
                    }
                }
                return new ItemStack(item);
            }
        }
        return ItemStack.EMPTY;
    }

    private int[] getSlotTextureUV(AttachmentType type, String equipType, boolean allow) {
        if (!allow) {
            return new int[]{0, EquipmentRefitScreen.ICON_UV_SIZE * 2};
        }
        int u = -1;
        if ("helmet".equals(equipType)) {
            switch (type) {
                case HELMET_TOP: u = 0; break;
                case NVG_MOUNT:
                case NVG: u = 1; break;
                case FACE_SHIELD: u = 2; break;
                default: return null;
            }
        } else if ("body_armor".equals(equipType)) {
            switch (type) {
                case FRONT_PLATE: u = 0; break;
                case BACK_PLATE: u = 1; break;
                case SIDE_PLATE: u = 2; break;
                case GROIN_PLATE: u = 3; break;
                case NECK_ARMOR: u = 4; break;
                default: return null;
            }
        } else {
            return null;
        }
        int v = "helmet".equals(equipType) ? 0 : EquipmentRefitScreen.ICON_UV_SIZE;
        return new int[]{u * EquipmentRefitScreen.ICON_UV_SIZE, v};
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public AttachmentType getType() {
        return type;
    }

    public ItemStack getAttachmentItem() {
        return attachmentItem;
    }

    public boolean isAllowed() {
        return allowed;
    }
}