package com.modernequipment.core.inventory;

import com.modernequipment.MESMod;
import com.modernequipment.core.data.EquipmentData;
import com.modernequipment.core.data.InventoryProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;

public class EquipmentInventoryHandler extends ItemStackHandler {
    private final EquipmentData data;
    private final int rows;
    private final int cols;

    public EquipmentInventoryHandler(EquipmentData data) {
        super(data.hasInventory() ? data.getInventory().getTotalSlots() : 0);
        this.data = data;
        InventoryProperties invProps = data.hasInventory() ? data.getInventory() : null;
        if (invProps != null) {
            this.rows = invProps.getEffectiveGridHeight();
            this.cols = invProps.getEffectiveGridWidth();
        } else {
            this.rows = 0;
            this.cols = 0;
        }
    }

    public EquipmentData getData() { return data; }
    public int getRows() { return rows; }
    public int getCols() { return cols; }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return super.isItemValid(slot, stack);
    }

    @Override
    public int getSlotLimit(int slot) {
        return super.getSlotLimit(slot);
    }
}