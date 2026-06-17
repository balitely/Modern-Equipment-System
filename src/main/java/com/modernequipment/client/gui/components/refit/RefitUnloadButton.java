package com.modernequipment.client.gui.components.refit;

import com.mojang.blaze3d.systems.RenderSystem;
import com.modernequipment.client.gui.EquipmentRefitScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class RefitUnloadButton extends Button implements IComponentTooltip {
    private static final int BUTTON_SIZE = 8;

    public RefitUnloadButton(int pX, int pY, OnPress pOnPress) {
        super(pX, pY, BUTTON_SIZE, BUTTON_SIZE, Component.empty(), pOnPress, DEFAULT_NARRATION);
    }

    @Override
    public void renderWidget(@Nonnull GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();

        int x = getX(), y = getY();
        int u = isHoveredOrFocused() ? 0 : 8;
        graphics.blit(EquipmentRefitScreen.UNLOAD_TEXTURE, x, y, width, height, u, 0, width, height, 16, 8);

        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    @Override
    public void renderTooltip(Consumer<List<Component>> consumer) {
        if (this.isHoveredOrFocused()) {
            consumer.accept(Collections.singletonList(Component.translatable("gui.modernequipment.refit.unload")));
        }
    }
}