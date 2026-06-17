package com.modernequipment.event;

import com.moderndamage.control.api.ModDamagePart;
import com.moderndamage.control.api.ModDamageSubPart;
import com.moderndamage.control.config.ModClothConfig;
import com.moderndamage.control.util.RomanNumberHelper;
import com.modernequipment.MESMod;
import com.modernequipment.api.equipment.IModifiableEquipment;
import com.modernequipment.core.data.EquipmentData;
import com.modernequipment.core.item.EquipmentArmorItem;
import com.modernequipment.core.item.EquipmentItem;
import com.modernequipment.util.MESProtectionCalculator;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;

public class EquipmentTooltipHandler {

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof EquipmentItem) && !(stack.getItem() instanceof EquipmentArmorItem)) {
            return;
        }

        // 移除 MDC 添加的行
        event.getToolTip().removeIf(line -> {
            String s = line.getString();
            return s.contains("ModernDamageControl Protection") ||
                    s.contains("§7[现代伤害控制] 防护水平") ||
                    s.contains("§8- §7");
        });

        EquipmentData data = null;
        IModifiableEquipment modifiable = IModifiableEquipment.getModifiableOrNull(stack);
        if (stack.getItem() instanceof EquipmentItem eq) {
            data = eq.getData();
        } else if (stack.getItem() instanceof EquipmentArmorItem armor) {
            data = armor.getData();
        }
        if (data == null) return;

        ModClothConfig config = ModClothConfig.get();
        boolean precise = config.enablePreciseHitbox && config.damageModel == ModClothConfig.DamageModel.HARDCORE;

        // 使用统一的计算器获取总防护
        Map<String, Integer> totalProt = new HashMap<>();
        Map<String, Integer> totalTough = new HashMap<>();

        if (precise) {
            // 子部位模式
            for (ModDamageSubPart subPart : ModDamageSubPart.values()) {
                String key = subPart.getSubKey();
                int level = MESProtectionCalculator.getTotalSubProtectionLevel(stack, modifiable, subPart);
                if (level > 0) {
                    totalProt.put(key, level);
                    totalTough.put(key, MESProtectionCalculator.getTotalSubToughness(stack, modifiable, subPart));
                }
            }
        } else {
            // 主部位模式
            for (ModDamagePart part : ModDamagePart.values()) {
                String key = part.name().toLowerCase();
                int level = MESProtectionCalculator.getTotalProtectionLevel(stack, modifiable, part);
                if (level > 0) {
                    totalProt.put(key, level);
                    totalTough.put(key, MESProtectionCalculator.getTotalToughness(stack, modifiable, part));
                }
            }
        }

        if (!totalProt.isEmpty()) {
            event.getToolTip().add(Component.translatable("tooltip.moderndamage.armor_protection").withStyle(ChatFormatting.GRAY));
            for (Map.Entry<String, Integer> entry : totalProt.entrySet()) {
                String key = entry.getKey();
                int level = entry.getValue();
                int toughness = totalTough.getOrDefault(key, 0);
                String roman = RomanNumberHelper.toRomanGrade(level);
                String translationKey = precise ? "tooltip.moderndamage.subpart." + key : "tooltip.moderndamage.part." + key;
                Component partName = Component.translatable(translationKey);
                Component line = Component.translatable("tooltip.moderndamage.protection_line_roman", partName, roman, level)
                        .append(Component.literal(" [" + toughness + "]").withStyle(ChatFormatting.GRAY));
                event.getToolTip().add(line);
            }
        }
    }
}