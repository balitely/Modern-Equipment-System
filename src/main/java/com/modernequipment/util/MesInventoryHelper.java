package com.modernequipment.util;

import com.modernequipment.core.data.EquipmentData;
import com.modernequipment.core.item.EquipmentArmorItem;
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


    /**
     * Returns true when the vanilla CHEST armor slot must reject the supplied
     * body armor because an equipped MES chest-rig occupies that slot.
     *
     * The exclusion is symmetric with the Curios-side rule: once a chest-rig
     * is present, the two items may coexist only when neither side declares
     * disables_chest_rig_slot=true.  Keeping this check here lets both the
     * vanilla armor slot and the server-side fallback use the same semantics.
     */
    public static boolean isChestArmorSlotBlocked(Player player, ItemStack candidateChestArmor) {
        if (player == null) return false;

        ItemStack chestRig = getChestRig(player);
        if (chestRig.isEmpty()) return false;

        EquipmentData rigData = getEquipmentData(chestRig);
        EquipmentData armorData = getEquipmentData(candidateChestArmor);

        boolean rigBlocksChest = rigData != null && rigData.isDisablesChestRigSlot();
        boolean armorBlocksRig = armorData != null && armorData.isDisablesChestRigSlot();
        return rigBlocksChest || armorBlocksRig;
    }

    public static EquipmentData getEquipmentData(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        if (stack.getItem() instanceof EquipmentArmorItem armorItem) {
            return armorItem.getData();
        }
        if (stack.getItem() instanceof EquipmentItem equipmentItem) {
            return equipmentItem.getData();
        }
        return null;
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
        EquipmentData data = getEquipmentData(helmet);
        return data != null && data.isDisablesFaceSlot();
    }

    // 获取头盔是否禁用耳机槽位
    public static boolean isHeadsetSlotDisabled(Player player) {
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        EquipmentData data = getEquipmentData(helmet);
        return data != null && data.isDisablesHeadsetSlot();
    }

    // 获取物品的 IItemHandler（用于存储面板）
    public static Optional<IItemHandler> getItemHandler(ItemStack stack) {
        if (stack.isEmpty()) return Optional.empty();
        return stack.getCapability(ForgeCapabilities.ITEM_HANDLER).resolve();
    }
}