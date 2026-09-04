package com.modernequipment.event;

import com.modernequipment.MESMod;
import com.modernequipment.core.data.EquipmentData;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import com.modernequipment.util.MesInventoryHelper;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

/**
 * Keeps MES vanilla armor slots and MES Curios slots mutually compatible.
 *
 * Semantics of disables_chest_rig_slot:
 * - The flag may be declared by either body armor or a chest-rig.
 * - If either equipped side has the flag set to true, body armor and chest-rig
 *   are mutually exclusive.
 * - They may coexist only when both sides have the flag set to false.
 *
 * Helmet flags keep their original semantics:
 * - disables_face_slot: a helmet disables mes_face.
 * - disables_headset_slot: a helmet disables mes_tactical_headset.
 *
 * EquipmentArmorItem must be handled in addition to EquipmentItem because MES
 * helmets/body armor are registered as EquipmentArmorItem.
 */
@Mod.EventBusSubscriber(modid = MESMod.MODID)
public class CuriosEventHandler {

    @SubscribeEvent
    public static void onLivingEquipmentChange(LivingEquipmentChangeEvent event) {
        LivingEntity entity = event.getEntity();
        if (!(entity instanceof Player player)) return;

        EquipmentSlot slot = event.getSlot();
        ItemStack newStack = event.getTo();

        if (slot == EquipmentSlot.HEAD) {
            handleHelmetSlotChange(player, newStack);
        } else if (slot == EquipmentSlot.CHEST) {
            handleChestArmorSlotChange(player, event.getFrom(), newStack);
        }
    }

    private static void handleHelmetSlotChange(Player player, ItemStack newHelmet) {
        EquipmentData data = MesInventoryHelper.getEquipmentData(newHelmet);
        if (data == null) return;

        if (data.isDisablesFaceSlot()) {
            clearAndDropCurioSlot(player, "mes_face");
        }
        if (data.isDisablesHeadsetSlot()) {
            clearAndDropCurioSlot(player, "mes_tactical_headset");
        }
    }

    /**
     * Body armor and a chest-rig may coexist only when neither side declares
     * disables_chest_rig_slot=true. LivingEquipmentChangeEvent is not
     * cancellable, so an invalid body-armor equip is reverted here.
     */
    private static void handleChestArmorSlotChange(Player player, ItemStack oldChest, ItemStack newChest) {
        if (newChest == null || newChest.isEmpty()) return;
        if (player.level().isClientSide) return;

        if (!MesInventoryHelper.isChestArmorSlotBlocked(player, newChest)) return;

        ItemStack rejected = newChest.copy();
        ItemStack restore = oldChest == null ? ItemStack.EMPTY : oldChest.copy();

        // Revert the armor change. Do NOT remove the already-equipped chest rig;
        // whichever side declares the exclusion, the newly equipped armor loses.
        player.setItemSlot(EquipmentSlot.CHEST, restore);

        // Return the rejected chest armor to the player's inventory. If there is
        // no room, drop only the remaining stack at the player's feet.
        if (!rejected.isEmpty()) {
            player.getInventory().add(rejected);
            if (!rejected.isEmpty()) {
                player.spawnAtLocation(rejected);
            }
        }
    }

    private static void clearAndDropCurioSlot(Player player, String slotIdentifier) {
        // Do inventory mutation on the logical server only; Curios will sync it.
        // Mutating the client copy as well can cause brief ghost items/desync.
        if (player.level().isClientSide) return;

        CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
            ICurioStacksHandler stacksHandler = handler.getCurios().get(slotIdentifier);
            if (stacksHandler == null) return;

            int slots = stacksHandler.getSlots();
            for (int i = 0; i < slots; i++) {
                ItemStack stack = stacksHandler.getStacks().getStackInSlot(i);
                if (!stack.isEmpty()) {
                    ItemStack toDrop = stack.copy();
                    stacksHandler.getStacks().setStackInSlot(i, ItemStack.EMPTY);
                    player.spawnAtLocation(toDrop);
                }
            }
        });
    }

}
