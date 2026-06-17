package com.modernequipment.event;

import com.modernequipment.MESMod;
import com.modernequipment.core.item.EquipmentItem;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.event.CurioEquipEvent;
import top.theillusivec4.curios.api.event.CurioUnequipEvent;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

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
            handleChestSlotChange(player, newStack);
        }
    }

    private static void handleHelmetSlotChange(Player player, ItemStack newHelmet) {
        if (newHelmet.getItem() instanceof EquipmentItem eqItem && eqItem.getData() != null) {
            if (eqItem.getData().isDisablesFaceSlot()) {
                clearAndDropCurioSlot(player, "mes_face");
            }
            if (eqItem.getData().isDisablesHeadsetSlot()) {
                clearAndDropCurioSlot(player, "mes_tactical_headset");
            }
        }
    }

    private static void handleChestSlotChange(Player player, ItemStack newChest) {
        if (newChest.getItem() instanceof EquipmentItem eqItem && eqItem.getData() != null) {
            if (eqItem.getData().isDisablesChestRigSlot()) {
                clearAndDropCurioSlot(player, "mes_chest_rig");
            }
        }
    }

    private static void clearAndDropCurioSlot(Player player, String slotIdentifier) {
        CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
            ICurioStacksHandler stacksHandler = handler.getCurios().get(slotIdentifier);
            if (stacksHandler == null) return;

            int slots = stacksHandler.getSlots();
            for (int i = 0; i < slots; i++) {
                ItemStack stack = stacksHandler.getStacks().getStackInSlot(i);
                if (!stack.isEmpty()) {
                    stacksHandler.getStacks().setStackInSlot(i, ItemStack.EMPTY);
                    if (!player.level().isClientSide) {
                        player.spawnAtLocation(stack);
                    }
                }
            }
        });
    }

    @SubscribeEvent
    public static void onCurioEquip(CurioEquipEvent event) {
        Player player = null;
        if (event.getSlotContext().entity() instanceof Player p) player = p;
        if (player == null) return;

        String slotId = event.getSlotContext().identifier();
        ItemStack stack = event.getStack();

        // 检查头盔是否禁用面部/耳机
        if ("mes_face".equals(slotId) || "mes_tactical_headset".equals(slotId)) {
            ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
            if (helmet.getItem() instanceof EquipmentItem eqItem && eqItem.getData() != null) {
                boolean disabled = "mes_face".equals(slotId) ?
                        eqItem.getData().isDisablesFaceSlot() :
                        eqItem.getData().isDisablesHeadsetSlot();
                if (disabled) {
                    event.setResult(Event.Result.DENY);
                    return;
                }
            }
        }
        // 检查防弹衣是否禁用胸挂
        else if ("mes_chest_rig".equals(slotId)) {
            ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
            if (chest.getItem() instanceof EquipmentItem eqItem && eqItem.getData() != null) {
                if (eqItem.getData().isDisablesChestRigSlot()) {
                    event.setResult(Event.Result.DENY);
                    return;
                }
            }
        }


    }
}