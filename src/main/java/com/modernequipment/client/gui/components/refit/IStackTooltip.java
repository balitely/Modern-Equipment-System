package com.modernequipment.client.gui.components.refit;

import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

public interface IStackTooltip {
    void renderTooltip(Consumer<ItemStack> consumer);
}