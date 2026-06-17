package com.modernequipment.event;

import com.moderndamage.control.config.ModClothConfig;
import com.modernequipment.MESMod;
import com.modernequipment.core.data.AttachmentData;
import com.modernequipment.core.data.CombatProperties;
import com.modernequipment.core.data.ModifierProperties;
import com.modernequipment.core.item.AttachmentItem;
import com.modernequipment.util.AttachmentDynamicStatsHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = MESMod.MODID)
public class AttachmentTooltipHandler {

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof AttachmentItem attItem)) return;

        AttachmentData data = attItem.getData();
        if (data == null) return;

        int maxDura = data.getDurability();
        int currentDura = maxDura - stack.getDamageValue();
        if (currentDura < 0) currentDura = 0;

        ModClothConfig config = ModClothConfig.get();
        boolean precise = config.enablePreciseHitbox && config.damageModel == ModClothConfig.DamageModel.HARDCORE;

        List<String> mountSlots = data.getMountSlots();
        if (mountSlots != null && !mountSlots.isEmpty()) {
            for (String slotName : mountSlots) {
                // 槽位标题
                String slotKey = "attachment." + MESMod.MODID + "." + slotName;
                event.getToolTip().add(Component.translatable(slotKey).withStyle(ChatFormatting.GOLD));

                // 显示 modifiers（移动速度、人机功效）
                ModifierProperties modProps = AttachmentDynamicStatsHelper.getModifiersForSlot(data, slotName);
                if (modProps != null && (modProps.getMovementSpeed() != 0 || modProps.getErgonomics() != 0)) {
                    String speedStr = String.format("%+.0f%%", modProps.getMovementSpeed() * 100);
                    String ergoStr = String.format("%+.0f", modProps.getErgonomics());
                    event.getToolTip().add(Component.literal("  ")
                            .append(Component.translatable("gui.modernequipment.refit.movement_speed").withStyle(ChatFormatting.GRAY))
                            .append(Component.literal(": " + speedStr).withStyle(ChatFormatting.WHITE))
                            .append(Component.literal(", "))
                            .append(Component.translatable("gui.modernequipment.refit.ergonomics").withStyle(ChatFormatting.GRAY))
                            .append(Component.literal(": " + ergoStr).withStyle(ChatFormatting.WHITE)));
                }

                // 防护属性
                Map<String, Integer> protMap = AttachmentDynamicStatsHelper.getProtectionForSlot(data, slotName, currentDura, maxDura, precise);
                Map<String, Integer> toughMap = AttachmentDynamicStatsHelper.getToughnessForSlot(data, slotName, currentDura, maxDura, precise);
                if (!protMap.isEmpty()) {
                    for (Map.Entry<String, Integer> entry : protMap.entrySet()) {
                        String partKey = entry.getKey();
                        int level = entry.getValue();
                        int toughness = toughMap.getOrDefault(partKey, 0);
                        String roman = toRomanGrade(level);
                        String partTranslationKey = precise ? "tooltip.moderndamage.subpart." + partKey : "tooltip.moderndamage.part." + partKey.toLowerCase();
                        event.getToolTip().add(Component.literal("  ").append(Component.translatable("tooltip.moderndamage.protection_line_roman",
                                        Component.translatable(partTranslationKey), roman, level)
                                .append(Component.literal(" [" + toughness + "]").withStyle(ChatFormatting.GRAY))));
                    }
                }
            }
        } else {
            // 无 mount_slots 的旧逻辑（保留兼容）
            var combat = AttachmentDynamicStatsHelper.getRepresentativeCombat(data);
            if (combat != null) {
                event.getToolTip().add(Component.translatable("tooltip.moderndamage.armor_protection").withStyle(ChatFormatting.GRAY));
                Map<String, Integer> protMap = precise ? combat.getArmorLevelsSub() : combat.getArmorLevels();
                Map<String, Integer> toughMap = precise ? combat.getToughnessSub() : combat.getToughness();
                if (protMap != null) {
                    for (Map.Entry<String, Integer> entry : protMap.entrySet()) {
                        String partKey = entry.getKey();
                        int level = (int) Math.round(entry.getValue() * (maxDura > 0 ? (float) currentDura / maxDura : 1.0f));
                        if (level <= 0) continue;
                        int toughness = toughMap != null ? toughMap.getOrDefault(partKey, 0) : 0;
                        toughness = (int) Math.round(toughness * (maxDura > 0 ? (float) currentDura / maxDura : 1.0f));
                        String roman = toRomanGrade(level);
                        String partTranslationKey = precise ? "tooltip.moderndamage.subpart." + partKey : "tooltip.moderndamage.part." + partKey.toLowerCase();
                        event.getToolTip().add(Component.translatable("tooltip.moderndamage.protection_line_roman",
                                        Component.translatable(partTranslationKey), roman, level)
                                .append(Component.literal(" [" + toughness + "]").withStyle(ChatFormatting.GRAY)));
                    }
                }
            }
        }

        // 耐久信息（显示损坏状态）
        if (maxDura > 0) {
            event.getToolTip().add(Component.empty());
            if (currentDura <= 1) {
                event.getToolTip().add(Component.translatable("gui.modernequipment.refit.durability")
                        .append(Component.literal(": " + currentDura + "/" + maxDura + " (BROKEN)").withStyle(ChatFormatting.RED)));
            } else {
                event.getToolTip().add(Component.translatable("gui.modernequipment.refit.durability")
                        .append(Component.literal(": " + currentDura + "/" + maxDura).withStyle(ChatFormatting.GRAY)));
            }
        }

        // 材质系数（取第一个槽位的材质系数）
        CombatProperties firstCombat = null;
        if (mountSlots != null && !mountSlots.isEmpty()) {
            firstCombat = AttachmentDynamicStatsHelper.getCombatForSlot(data, mountSlots.get(0));
        }
        if (firstCombat == null) firstCombat = data.getCombat();
        if (firstCombat != null && firstCombat.getMaterialFactor() != null && !firstCombat.getMaterialFactor().isEmpty()) {
            float materialFactor = firstCombat.getMaterialFactor().values().stream().findFirst().orElse(1.0f);
            event.getToolTip().add(Component.translatable("gui.modernequipment.refit.material_factor")
                    .append(Component.literal(": " + String.format("%.2f", materialFactor)).withStyle(ChatFormatting.GRAY)));
        }
    }

    private static String toRomanGrade(int level) {
        if (level <= 0) return "None";
        String[] grades = {"I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};
        int index = Math.min(level / 10, 9);
        return grades[index];
    }
}