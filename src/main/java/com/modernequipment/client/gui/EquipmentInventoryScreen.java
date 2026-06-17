package com.modernequipment.client.gui;

import com.modernequipment.MESMod;
import com.modernequipment.core.data.InventoryProperties;
import com.modernequipment.gui.EquipmentContainerMenu;
import com.modernequipment.gui.EquipmentSlot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class EquipmentInventoryScreen extends AbstractContainerScreen<EquipmentContainerMenu> {
    private static final ResourceLocation BACKGROUND = new ResourceLocation(MESMod.MODID, "textures/gui/equipment_background.png");
    private static final ResourceLocation SLOT_1x1 = new ResourceLocation(MESMod.MODID, "textures/gui/slot/slot_1x1.png");

    private final InventoryProperties invProps;
    private final int containerRows;
    private final int containerCols;

    public EquipmentInventoryScreen(EquipmentContainerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.invProps = ((com.modernequipment.core.item.EquipmentItem) menu.getEquipmentStack().getItem()).getData().getInventory();
        this.containerCols = invProps.getEffectiveGridWidth();
        this.containerRows = invProps.getEffectiveGridHeight();
        this.imageWidth = 176;
        this.imageHeight = 18 + containerRows * 18 + 96;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        guiGraphics.blitNineSliced(BACKGROUND, x, y, imageWidth, imageHeight, 4, 4, 4, 4, 0);

        for (Slot slot : menu.slots) {
            if (slot instanceof EquipmentSlot eqSlot) {
                int slotX = x + eqSlot.x - 1;
                int slotY = y + eqSlot.y - 1;
                guiGraphics.blit(SLOT_1x1, slotX, slotY, 0, 0, 18, 18, 18, 18);
            }
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }
}