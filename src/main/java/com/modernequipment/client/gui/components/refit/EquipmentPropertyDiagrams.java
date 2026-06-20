package com.modernequipment.client.gui.components.refit;

import com.modernequipment.MESMod;
import com.modernequipment.api.attachment.AttachmentType;
import com.modernequipment.api.equipment.IModifiableEquipment;
import com.modernequipment.compat.ModernDamageCompat;
import com.modernequipment.core.data.AttachmentData;
import com.modernequipment.core.data.EquipmentData;
import com.modernequipment.core.item.EquipmentArmorItem;
import com.modernequipment.core.item.EquipmentItem;
import com.modernequipment.core.loader.EquipmentDataManager;
import com.modernequipment.util.MESProtectionCalculator;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

public final class EquipmentPropertyDiagrams {

    private static final String ATTACHMENTS_DURABILITY_KEY = "AttachmentsDurability";

    public static void draw(GuiGraphics graphics, Font font, int x, int y,
                            ItemStack equipmentStack, IModifiableEquipment modifiable, String equipmentType) {
        if (equipmentStack.isEmpty()) return;

        EquipmentData data = getEquipmentData(equipmentStack);
        if (data == null) return;

        float totalMoveSpeed = data.getModifiers() != null ? data.getModifiers().getMovementSpeed() : 0f;
        float totalErgonomics = data.getModifiers() != null ? data.getModifiers().getErgonomics() : 0f;

        Map<AttachmentType, ResourceLocation> attachments = modifiable.getAttachments(equipmentStack);
        for (ResourceLocation attId : attachments.values()) {
            AttachmentData attData = EquipmentDataManager.getAttachment(attId);
            if (attData != null && attData.getModifiers() != null) {
                totalMoveSpeed += attData.getModifiers().getMovementSpeed();
                totalErgonomics += attData.getModifiers().getErgonomics();
            }
        }

        int maxDura = data.getDurability();
        int curDura = equipmentStack.getDamageValue();
        int remainingDura = maxDura - curDura;
        float weight = data.getWeight();

        boolean precise = ModernDamageCompat.isPreciseMode();
        Object armorData = ModernDamageCompat.getArmorData(equipmentStack.getItem());

        int panelHeight = calculatePanelHeight(equipmentType, precise);
        graphics.fill(x, y, x + 288, y + panelHeight, 0xAF222222);

        int barStartX = x + 83;
        int barMaxWidth = 120;
        int barEndX = barStartX + barMaxWidth;
        int barBackgroundColor = 0xFF000000;
        int barBaseColor = 0xFFFFFFFF;
        int barPositivelyColor = 0xFF55FF55;
        int barNegativeColor = 0xFFFF5555;
        int fontColor = 0xCCCCCC;
        int nameTextStartX = x + 5;
        int valueTextStartX = x + 210;

        int yOffset = y + 5;

        if ("helmet".equals(equipmentType)) {
            drawHelmetProperties(graphics, font, precise, armorData, equipmentStack, modifiable,
                    totalMoveSpeed, totalErgonomics, remainingDura, maxDura, weight,
                    barStartX, barEndX, barMaxWidth, barBackgroundColor, barBaseColor,
                    barPositivelyColor, barNegativeColor, fontColor,
                    nameTextStartX, valueTextStartX, yOffset);
        } else if ("body_armor".equals(equipmentType)) {
            drawBodyArmorProperties(graphics, font, precise, armorData, equipmentStack, modifiable,
                    totalMoveSpeed, totalErgonomics, remainingDura, maxDura, weight,
                    barStartX, barEndX, barMaxWidth, barBackgroundColor, barBaseColor,
                    barPositivelyColor, barNegativeColor, fontColor,
                    nameTextStartX, valueTextStartX, yOffset);
        } else {
            drawGenericProperties(graphics, font, remainingDura, maxDura, weight,
                    totalMoveSpeed, totalErgonomics,
                    barStartX, barEndX, barMaxWidth, barBackgroundColor, barBaseColor,
                    barPositivelyColor, barNegativeColor, fontColor,
                    nameTextStartX, valueTextStartX, yOffset);
        }
    }

    private static void drawHelmetProperties(GuiGraphics graphics, Font font, boolean precise,
                                             Object armorData, ItemStack stack, IModifiableEquipment modifiable,
                                             float moveSpeed, float ergonomics,
                                             int remainingDura, int maxDura, float weight,
                                             int barStartX, int barEndX, int barMaxWidth,
                                             int bgColor, int baseColor, int posColor, int negColor,
                                             int fontColor, int nameX, int valueX, int yOffset) {
        if (precise) {
            graphics.drawString(font, Component.translatable("gui.modernequipment.refit.protection_level"), nameX, yOffset, fontColor, false);
            yOffset += 10;
            String[] subPartKeys = {"head_top", "head_face", "head_neck"};
            for (String subPartKey : subPartKeys) {
                int totalLevel = MESProtectionCalculator.getTotalSubProtectionLevel(stack, modifiable, subPartKey);
                if (totalLevel == 0) continue;
                int totalToughness = MESProtectionCalculator.getTotalSubToughness(stack, modifiable, subPartKey);
                String roman = ModernDamageCompat.toRomanGrade(totalLevel);
                Component subPartName = Component.translatable("tooltip.moderndamage.subpart." + subPartKey);
                Component line = Component.translatable("tooltip.moderndamage.protection_line_roman", subPartName, roman, totalLevel)
                        .append(Component.literal(" [" + totalToughness + "]").withStyle(ChatFormatting.GRAY));
                graphics.drawString(font, line, nameX + 5, yOffset, fontColor, false);
                yOffset += 10;
            }
            float materialFactor = ModernDamageCompat.getMaterialFactor(armorData, "HEAD");
            graphics.drawString(font, Component.translatable("gui.modernequipment.refit.material_factor"), nameX, yOffset, fontColor, false);
            graphics.drawString(font, String.format("%.2f", materialFactor), valueX, yOffset, fontColor, false);
            yOffset += 10;
        } else {
            int totalLevel = MESProtectionCalculator.getTotalProtectionLevel(stack, modifiable, "head");
            int totalToughness = MESProtectionCalculator.getTotalToughness(stack, modifiable, "head");
            float ricochet = MESProtectionCalculator.getTotalRicochetChance(stack, modifiable, "head");
            float materialFactor = ModernDamageCompat.getMaterialFactor(armorData, "HEAD");

            drawStat(graphics, font, "gui.modernequipment.refit.armor_level", totalLevel, nameX, valueX, yOffset, fontColor);
            yOffset += 10;
            drawStat(graphics, font, "gui.modernequipment.refit.toughness", totalToughness, nameX, valueX, yOffset, fontColor);
            yOffset += 10;
            drawStatPercent(graphics, font, "gui.modernequipment.refit.ricochet", ricochet, nameX, valueX, yOffset, fontColor);
            yOffset += 10;
            drawStat(graphics, font, "gui.modernequipment.refit.material_factor", materialFactor, nameX, valueX, yOffset, fontColor);
            yOffset += 10;
        }
        drawDurability(graphics, font, remainingDura, maxDura, barStartX, barEndX, barMaxWidth, bgColor, baseColor, fontColor, nameX, valueX, yOffset);
        yOffset += 10;
        drawWeight(graphics, font, weight, nameX, valueX, yOffset, fontColor);
        yOffset += 10;
        drawMovementSpeed(graphics, font, moveSpeed, barStartX, barEndX, barMaxWidth, bgColor, baseColor, posColor, negColor, fontColor, nameX, valueX, yOffset);
        yOffset += 10;
        drawErgonomics(graphics, font, ergonomics, barStartX, barEndX, barMaxWidth, bgColor, baseColor, posColor, negColor, fontColor, nameX, valueX, yOffset);
    }

    private static void drawBodyArmorProperties(GuiGraphics graphics, Font font, boolean precise,
                                                Object armorData, ItemStack stack, IModifiableEquipment modifiable,
                                                float moveSpeed, float ergonomics,
                                                int remainingDura, int maxDura, float weight,
                                                int barStartX, int barEndX, int barMaxWidth,
                                                int bgColor, int baseColor, int posColor, int negColor,
                                                int fontColor, int nameX, int valueX, int yOffset) {
        if (precise) {
            graphics.drawString(font, Component.translatable("gui.modernequipment.refit.protection_level"), nameX, yOffset, fontColor, false);
            yOffset += 10;
            String[] subPartKeys = {
                    "chest_front", "chest_back",
                    "stomach_front", "stomach_back"
            };
            for (String subPartKey : subPartKeys) {
                int totalLevel = MESProtectionCalculator.getTotalSubProtectionLevel(stack, modifiable, subPartKey);
                if (totalLevel == 0) continue;
                int totalToughness = MESProtectionCalculator.getTotalSubToughness(stack, modifiable, subPartKey);
                String roman = ModernDamageCompat.toRomanGrade(totalLevel);
                Component subPartName = Component.translatable("tooltip.moderndamage.subpart." + subPartKey);
                Component line = Component.translatable("tooltip.moderndamage.protection_line_roman", subPartName, roman, totalLevel)
                        .append(Component.literal(" [" + totalToughness + "]").withStyle(ChatFormatting.GRAY));
                graphics.drawString(font, line, nameX + 5, yOffset, fontColor, false);
                yOffset += 10;
            }
            float materialFactorChest = ModernDamageCompat.getMaterialFactor(armorData, "CHEST");
            graphics.drawString(font, Component.translatable("gui.modernequipment.refit.material_factor_chest"), nameX, yOffset, fontColor, false);
            graphics.drawString(font, String.format("%.2f", materialFactorChest), valueX, yOffset, fontColor, false);
            yOffset += 10;
            float materialFactorStomach = ModernDamageCompat.getMaterialFactor(armorData, "STOMACH");
            graphics.drawString(font, Component.translatable("gui.modernequipment.refit.material_factor_stomach"), nameX, yOffset, fontColor, false);
            graphics.drawString(font, String.format("%.2f", materialFactorStomach), valueX, yOffset, fontColor, false);
            yOffset += 10;
        } else {
            int chestLevel = MESProtectionCalculator.getTotalProtectionLevel(stack, modifiable, "chest");
            int chestToughness = MESProtectionCalculator.getTotalToughness(stack, modifiable, "chest");
            float chestRicochet = MESProtectionCalculator.getTotalRicochetChance(stack, modifiable, "chest");
            float chestMaterial = ModernDamageCompat.getMaterialFactor(armorData, "CHEST");

            int stomachLevel = MESProtectionCalculator.getTotalProtectionLevel(stack, modifiable, "stomach");
            int stomachToughness = MESProtectionCalculator.getTotalToughness(stack, modifiable, "stomach");
            float stomachRicochet = MESProtectionCalculator.getTotalRicochetChance(stack, modifiable, "stomach");
            float stomachMaterial = ModernDamageCompat.getMaterialFactor(armorData, "STOMACH");

            graphics.drawString(font, Component.translatable("gui.modernequipment.refit.chest"), nameX, yOffset, fontColor, false);
            yOffset += 10;
            drawStat(graphics, font, "gui.modernequipment.refit.armor_level", chestLevel, nameX + 5, valueX, yOffset, fontColor);
            yOffset += 10;
            drawStat(graphics, font, "gui.modernequipment.refit.toughness", chestToughness, nameX + 5, valueX, yOffset, fontColor);
            yOffset += 10;
            drawStatPercent(graphics, font, "gui.modernequipment.refit.ricochet", chestRicochet, nameX + 5, valueX, yOffset, fontColor);
            yOffset += 10;
            drawStat(graphics, font, "gui.modernequipment.refit.material_factor", chestMaterial, nameX + 5, valueX, yOffset, fontColor);
            yOffset += 10;

            graphics.drawString(font, Component.translatable("gui.modernequipment.refit.stomach"), nameX, yOffset, fontColor, false);
            yOffset += 10;
            drawStat(graphics, font, "gui.modernequipment.refit.armor_level", stomachLevel, nameX + 5, valueX, yOffset, fontColor);
            yOffset += 10;
            drawStat(graphics, font, "gui.modernequipment.refit.toughness", stomachToughness, nameX + 5, valueX, yOffset, fontColor);
            yOffset += 10;
            drawStatPercent(graphics, font, "gui.modernequipment.refit.ricochet", stomachRicochet, nameX + 5, valueX, yOffset, fontColor);
            yOffset += 10;
            drawStat(graphics, font, "gui.modernequipment.refit.material_factor", stomachMaterial, nameX + 5, valueX, yOffset, fontColor);
            yOffset += 10;
        }
        drawDurability(graphics, font, remainingDura, maxDura, barStartX, barEndX, barMaxWidth, bgColor, baseColor, fontColor, nameX, valueX, yOffset);
        yOffset += 10;
        drawWeight(graphics, font, weight, nameX, valueX, yOffset, fontColor);
        yOffset += 10;
        drawMovementSpeed(graphics, font, moveSpeed, barStartX, barEndX, barMaxWidth, bgColor, baseColor, posColor, negColor, fontColor, nameX, valueX, yOffset);
        yOffset += 10;
        drawErgonomics(graphics, font, ergonomics, barStartX, barEndX, barMaxWidth, bgColor, baseColor, posColor, negColor, fontColor, nameX, valueX, yOffset);
    }

    private static void drawGenericProperties(GuiGraphics graphics, Font font,
                                              int remainingDura, int maxDura, float weight,
                                              float moveSpeed, float ergonomics,
                                              int barStartX, int barEndX, int barMaxWidth,
                                              int bgColor, int baseColor, int posColor, int negColor,
                                              int fontColor, int nameX, int valueX, int yOffset) {
        drawDurability(graphics, font, remainingDura, maxDura, barStartX, barEndX, barMaxWidth, bgColor, baseColor, fontColor, nameX, valueX, yOffset);
        yOffset += 10;
        drawWeight(graphics, font, weight, nameX, valueX, yOffset, fontColor);
        yOffset += 10;
        drawMovementSpeed(graphics, font, moveSpeed, barStartX, barEndX, barMaxWidth, bgColor, baseColor, posColor, negColor, fontColor, nameX, valueX, yOffset);
        yOffset += 10;
        drawErgonomics(graphics, font, ergonomics, barStartX, barEndX, barMaxWidth, bgColor, baseColor, posColor, negColor, fontColor, nameX, valueX, yOffset);
    }

    private static void drawStat(GuiGraphics graphics, Font font, String key, int value, int nameX, int valueX, int y, int color) {
        graphics.drawString(font, Component.translatable(key), nameX, y, color, false);
        graphics.drawString(font, String.valueOf(value), valueX, y, color, false);
    }

    private static void drawStat(GuiGraphics graphics, Font font, String key, float value, int nameX, int valueX, int y, int color) {
        graphics.drawString(font, Component.translatable(key), nameX, y, color, false);
        graphics.drawString(font, String.format("%.2f", value), valueX, y, color, false);
    }

    private static void drawStatPercent(GuiGraphics graphics, Font font, String key, float value, int nameX, int valueX, int y, int color) {
        graphics.drawString(font, Component.translatable(key), nameX, y, color, false);
        graphics.drawString(font, String.format("%.1f%%", value * 100), valueX, y, color, false);
    }

    private static void drawDurability(GuiGraphics graphics, Font font, int cur, int max, int barStartX, int barEndX, int barMaxWidth, int bgColor, int baseColor, int fontColor, int nameX, int valueX, int y) {
        graphics.drawString(font, Component.translatable("gui.modernequipment.refit.durability"), nameX, y, fontColor, false);
        graphics.fill(barStartX, y + 2, barEndX, y + 6, bgColor);
        int length = max > 0 ? (int) (barStartX + barMaxWidth * ((float) cur / max)) : barStartX;
        graphics.fill(barStartX, y + 2, length, y + 6, baseColor);
        graphics.drawString(font, cur + "/" + max, valueX, y, fontColor, false);
    }

    private static void drawWeight(GuiGraphics graphics, Font font, float weight, int nameX, int valueX, int y, int color) {
        graphics.drawString(font, Component.translatable("gui.modernequipment.refit.weight"), nameX, y, color, false);
        graphics.drawString(font, String.format("%.1f kg", weight), valueX, y, color, false);
    }

    private static void drawMovementSpeed(GuiGraphics graphics, Font font, float speed, int barStartX, int barEndX, int barMaxWidth, int bgColor, int baseColor, int posColor, int negColor, int fontColor, int nameX, int valueX, int y) {
        graphics.drawString(font, Component.translatable("gui.modernequipment.refit.movement_speed"), nameX, y, fontColor, false);
        if (speed > 0) {
            graphics.drawString(font, String.format("+%.0f%%", speed * 100), valueX, y, fontColor, false);
        } else if (speed < 0) {
            graphics.drawString(font, String.format("%.0f%%", speed * 100), valueX, y, fontColor, false);
        } else {
            graphics.drawString(font, "0%", valueX, y, fontColor, false);
        }
    }

    private static void drawErgonomics(GuiGraphics graphics, Font font, float ergo, int barStartX, int barEndX, int barMaxWidth, int bgColor, int baseColor, int posColor, int negColor, int fontColor, int nameX, int valueX, int y) {
        graphics.drawString(font, Component.translatable("gui.modernequipment.refit.ergonomics"), nameX, y, fontColor, false);
        if (ergo > 0) {
            graphics.drawString(font, String.format("+%.0f", ergo), valueX, y, fontColor, false);
        } else if (ergo < 0) {
            graphics.drawString(font, String.format("%.0f", ergo), valueX, y, fontColor, false);
        } else {
            graphics.drawString(font, "0", valueX, y, fontColor, false);
        }
    }

    private static int calculatePanelHeight(String type, boolean precise) {
        if ("helmet".equals(type)) return precise ? 130 : 120;
        else if ("body_armor".equals(type)) return precise ? 140 : 200;
        return 110;
    }

    private static EquipmentData getEquipmentData(ItemStack stack) {
        if (stack.getItem() instanceof EquipmentItem eq) return eq.getData();
        if (stack.getItem() instanceof EquipmentArmorItem armor) return armor.getData();
        return null;
    }
}