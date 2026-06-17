package com.modernequipment.core.inventory;

import net.minecraft.world.item.ItemStack;

public class InventoryItem {
    private final ItemStack stack;
    private final int x;
    private final int y;
    private final int width;
    private final int height;

    public InventoryItem(ItemStack stack, int x, int y, int width, int height) {
        this.stack = stack;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public ItemStack getStack() { return stack; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
}