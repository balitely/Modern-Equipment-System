package com.modernequipment.gui;

import com.modernequipment.core.data.SlotDefinition;
import com.modernequipment.core.inventory.EquipmentSubInventoryHandler;
import com.sighs.petiteinventory.init.Area;
import com.sighs.petiteinventory.utils.ItemUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

public class EquipmentSlot extends SlotItemHandler {
    private final int slotWidth;
    private final int slotHeight;
    private final EquipmentContainerMenu menu; // 新增：对父菜单的引用

    public EquipmentSlot(IItemHandler itemHandler, int index, int x, int y, int width, int height, EquipmentContainerMenu menu) {
        super(itemHandler, index, x, y);
        this.slotWidth = width;
        this.slotHeight = height;
        this.menu = menu;
    }

    public int getSlotWidth() {
        return slotWidth;
    }

    public int getSlotHeight() {
        return slotHeight;
    }

    @Override
    public void set(@Nonnull ItemStack stack) {
        super.set(stack);
        // 物品放入槽位时立即保存
        menu.saveInventory();
    }

    @Override
    public void onTake(Player player, ItemStack stack) {
        super.onTake(player, stack);
        // 物品取出槽位时立即保存
        menu.saveInventory();
    }

    @Override
    public boolean mayPlace(@Nonnull ItemStack stack) {
        // 父类检查（基本有效性）
        if (!super.mayPlace(stack)) return false;

        // 获取当前子容器处理器
        if (!(getItemHandler() instanceof EquipmentSubInventoryHandler subHandler)) {
            return true;
        }

        // 获取当前子槽位在子容器内的索引
        int subIndex = getSlotIndex();
        SlotDefinition def = subHandler.getDefinition();
        int gridWidth = def.getWidth();
        int gridHeight = def.getHeight(); // 实际高度，但线性索引按宽度优先

        // 计算当前子槽位的网格坐标 (x, y)
        int currentX = subIndex % gridWidth;
        int currentY = subIndex / gridWidth;

        // 遍历子容器中所有已存在的物品
        for (int i = 0; i < subHandler.getSlots(); i++) {
            ItemStack existing = subHandler.getStackInSlot(i);
            if (existing.isEmpty()) continue;

            // 通过 PetiteInventory 获取物品尺寸（支持旋转）
            Area area = ItemUtils.getArea(existing);
            int itemWidth = area.width();
            int itemHeight = area.height();

            // 计算该物品占用的左上角坐标（假设物品存储在索引 i 对应的格子上）
            int itemX = i % gridWidth;
            int itemY = i / gridWidth;

            // 检查当前格子是否被该物品的矩形区域覆盖
            if (currentX >= itemX && currentX < itemX + itemWidth &&
                    currentY >= itemY && currentY < itemY + itemHeight) {
                // 已被占用，禁止放入新物品
                return false;
            }
        }

        return true;
    }
}