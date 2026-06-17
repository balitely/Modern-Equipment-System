package com.modernequipment.gui;

import com.modernequipment.core.data.InventoryProperties;
import com.modernequipment.core.data.SlotDefinition;
import com.modernequipment.core.inventory.EquipmentSubInventoryHandler;
import com.modernequipment.core.item.EquipmentItem;
import com.modernequipment.init.ModMenus;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EquipmentContainerMenu extends AbstractContainerMenu {
    private final ItemStack equipmentStack;
    private final List<EquipmentSubInventoryHandler> subHandlers;
    private final List<SlotDefinition> originalSlots;
    private final int totalSlots; // 所有子槽位总数

    public EquipmentContainerMenu(int containerId, Inventory playerInv, ItemStack equipmentStack) {
        super(ModMenus.EQUIPMENT_CONTAINER.get(), containerId);
        this.equipmentStack = equipmentStack;
        EquipmentItem item = (EquipmentItem) equipmentStack.getItem();
        this.originalSlots = item.getSlotDefinitions();
        this.subHandlers = item.getSubHandlers(equipmentStack);

        // 计算总槽位数并添加槽位
        int globalSlotIndex = 0;
        for (int i = 0; i < subHandlers.size(); i++) {
            EquipmentSubInventoryHandler handler = subHandlers.get(i);
            SlotDefinition def = originalSlots.get(i);
            int baseX = 8 + def.getX() * 18;
            int baseY = 18 + def.getY() * 18;
            int subCount = handler.getSubSlotCount();
            for (int sub = 0; sub < subCount; sub++) {
                int[] offset = handler.getSubSlotOffset(sub);
                int slotX = baseX + offset[0];
                int slotY = baseY + offset[1];
                // 每个子槽位使用 handler 和子索引，并传入 this
                addSlot(new EquipmentSlot(handler, sub, slotX, slotY, 1, 1, this));
                globalSlotIndex++;
            }
        }
        totalSlots = globalSlotIndex;

        // 添加玩家背包和快捷栏（同之前）
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                addSlot(new Slot(playerInv, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }
        for (int i = 0; i < 9; ++i) {
            addSlot(new Slot(playerInv, i, 8 + i * 18, 142));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return !equipmentStack.isEmpty() && player.getMainHandItem() == equipmentStack;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();

        // 如果点击的是装备容器内的槽位（索引 < totalSlots），尝试移到玩家背包
        if (index < totalSlots) {
            if (!moveItemStackTo(stack, totalSlots, slots.size(), false)) {
                return ItemStack.EMPTY;
            }
        } else {
            // 否则从玩家背包移到装备容器，但需要找到第一个可用的容器槽位
            if (!moveItemStackTo(stack, 0, totalSlots, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return copy;
    }

    /**
     * 实时保存子容器数据到物品的 NBT
     */
    public void saveInventory() {
        if (!equipmentStack.isEmpty()) {
            EquipmentItem.saveSubHandlers(equipmentStack, subHandlers);
        }
    }

    /**
     * 菜单关闭时最后保存一次（保险）
     */
    @Override
    public void removed(Player player) {
        super.removed(player);
        saveInventory();
    }

    public List<EquipmentSubInventoryHandler> getSubHandlers() {
        return subHandlers;
    }

    public ItemStack getEquipmentStack() {
        return equipmentStack;
    }
}