package com.modernequipment.client.gui.components.refit;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
public class FlatColorButton extends Button {
    private static final int DEFAULT_BG = 0xFF222222;
    private static final int HOVERED_BG = 0xFF444444;
    private static final int TEXT_COLOR = 0xFFFFFF;

    public FlatColorButton(int x, int y, int width, int height, Component message, OnPress onPress) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
    }

    @Override
    public void renderWidget(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int bg = this.isHoveredOrFocused() ? HOVERED_BG : DEFAULT_BG;
        graphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, bg);

        int textColor = this.active ? TEXT_COLOR : 0xA0A0A0;
        graphics.drawCenteredString(Minecraft.getInstance().font, this.getMessage(),
                this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, textColor);
    }
}