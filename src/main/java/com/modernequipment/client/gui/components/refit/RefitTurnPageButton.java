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

public class RefitTurnPageButton extends Button implements IComponentTooltip {
    private final boolean isUpPage;

    public RefitTurnPageButton(int pX, int pY, boolean isUpPage, OnPress pOnPress) {
        super(pX, pY, 18, 10, Component.empty(), pOnPress, DEFAULT_NARRATION);
        this.isUpPage = isUpPage;
    }

    @Override
    public void renderWidget(@Nonnull GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();

        int x = getX(), y = getY();
        int v = isUpPage ? 0 : 80;
        if (isHoveredOrFocused()) {
            graphics.blit(EquipmentRefitScreen.TURN_PAGE_TEXTURE, x, y, width, height, 0, v, 180, 80, 180, 160);
        } else {
            graphics.blit(EquipmentRefitScreen.TURN_PAGE_TEXTURE, x + 1, y + 1, width - 2, height - 2, 1, v + 1, 178, 78, 180, 160);
        }

        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    @Override
    public void renderTooltip(Consumer<List<Component>> consumer) {
        if (this.isHoveredOrFocused()) {
            String key = isUpPage ? "gui.modernequipment.refit.page_up" : "gui.modernequipment.refit.page_down";
            consumer.accept(Collections.singletonList(Component.translatable(key)));
        }
    }
}