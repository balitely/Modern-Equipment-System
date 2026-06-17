package com.modernequipment.util;

import com.modernequipment.core.item.EquipmentItem;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

import java.util.Optional;

public class MesInventoryHelper {

    public static ItemStack getChestArmor(Player player) {
        return player.getItemBySlot(EquipmentSlot.CHEST);
    }

    // 获取玩家胸挂槽位的物品（Curios mes_chest_rig）
    public static ItemStack getChestRig(Player player) {
        return CuriosApi.getCuriosInventory(player).map(handler -> {
            var opt = handler.findCurio("mes_chest_rig", 0);
            return opt.map(slotResult -> slotResult.stack()).orElse(ItemStack.EMPTY);
        }).orElse(ItemStack.EMPTY);
    }

    // 获取腰封物品
    public static ItemStack getTacticalBelt(Player player) {
        return CuriosApi.getCuriosInventory(player).map(handler -> {
            var opt = handler.findCurio("mes_tactical_belt", 0);
            return opt.map(slotResult -> slotResult.stack()).orElse(ItemStack.EMPTY);
        }).orElse(ItemStack.EMPTY);
    }

    // 获取背包物品
    public static ItemStack getBackpack(Player player) {
        return CuriosApi.getCuriosInventory(player).map(handler -> {
            var opt = handler.findCurio("mes_backpack", 0);
            return opt.map(slotResult -> slotResult.stack()).orElse(ItemStack.EMPTY);
        }).orElse(ItemStack.EMPTY);
    }

    // 获取安全箱
    public static ItemStack getSafeBox(Player player) {
        return CuriosApi.getCuriosInventory(player).map(handler -> {
            var opt = handler.findCurio("mes_safe_box", 0);
            return opt.map(slotResult -> slotResult.stack()).orElse(ItemStack.EMPTY);
        }).orElse(ItemStack.EMPTY);
    }

    // 获取头盔是否禁用面部槽位
    public static boolean isFaceSlotDisabled(Player player) {
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        if (helmet.getItem() instanceof EquipmentItem eq && eq.getData() != null) {
            return eq.getData().isDisablesFaceSlot();
        }
        return false;
    }

    // 获取头盔是否禁用耳机槽位
    public static boolean isHeadsetSlotDisabled(Player player) {
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        if (helmet.getItem() instanceof EquipmentItem eq && eq.getData() != null) {
            return eq.getData().isDisablesHeadsetSlot();
        }
        return false;
    }

    // 获取物品的 IItemHandler（用于存储面板）
    public static Optional<IItemHandler> getItemHandler(ItemStack stack) {
        if (stack.isEmpty()) return Optional.empty();
        return stack.getCapability(ForgeCapabilities.ITEM_HANDLER).resolve();
    }
}