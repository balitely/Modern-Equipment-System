package com.modernequipment.core.inventory;

import com.modernequipment.core.data.SlotDefinition;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;

public class EquipmentSubInventoryHandler extends ItemStackHandler {
    private final SlotDefinition definition;
    private final int subSlotCount;

    public EquipmentSubInventoryHandler(SlotDefinition definition) {
        super(definition.getWidth() * definition.getHeight());
        this.definition = definition;
        this.subSlotCount = definition.getWidth() * definition.getHeight();
    }

    public SlotDefinition getDefinition() {
        return definition;
    }

    public int getSubSlotCount() {
        return subSlotCount;
    }

    public int[] getSubSlotOffset(int subIndex) {
        int w = definition.getWidth();
        int h = definition.getHeight();
        int dx = subIndex % w;
        int dy = subIndex / w;
        return new int[]{dx * 18, dy * 18};
    }

    @Override
    public CompoundTag serializeNBT() {
        return super.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        super.deserializeNBT(nbt);
    }
}