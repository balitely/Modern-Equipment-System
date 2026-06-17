package com.modernequipment.core.inventory;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import javax.annotation.Nonnull;
import java.util.List;

public class CombinedItemHandler implements IItemHandler {
    private final List<? extends IItemHandler> handlers;
    private final int totalSlots;

    public CombinedItemHandler(List<? extends IItemHandler> handlers) {
        this.handlers = handlers;
        int total = 0;
        for (IItemHandler h : handlers) {
            total += h.getSlots();
        }
        this.totalSlots = total;
    }

    @Override
    public int getSlots() {
        return totalSlots;
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) {
        int idx = slot;
        for (IItemHandler handler : handlers) {
            if (idx < handler.getSlots()) {
                return handler.getStackInSlot(idx);
            }
            idx -= handler.getSlots();
        }
        return ItemStack.EMPTY;
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        return stack;
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot) {
        return 64;
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return false;
    }
}