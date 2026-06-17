package com.modernequipment.event;

import com.modernequipment.MESMod;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.event.DropRulesEvent;
import top.theillusivec4.curios.api.type.capability.ICurio.DropRule;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

/**
 * 处理安全箱槽位（mes_safe_box）的死亡不掉落逻辑。
 * 通过 DropRulesEvent 为槽位内的所有物品添加 ALWAYS_KEEP 覆盖规则。
 */
@Mod.EventBusSubscriber(modid = MESMod.MODID)
public class SafeBoxDropHandler {

    private static final String SAFE_BOX_SLOT_ID = "mes_safe_box";

    @SubscribeEvent
    public static void onDropRules(DropRulesEvent event) {
        LivingEntity livingEntity = event.getEntity();

        CuriosApi.getCuriosInventory(livingEntity).ifPresent(handler -> {
            // 获取安全箱槽位的处理器
            handler.getStacksHandler(SAFE_BOX_SLOT_ID).ifPresent(stacksHandler -> {
                // 为槽位内的每一个物品（包括非空槽位）添加 ALWAYS_KEEP 覆盖规则
                // 注意：需要为每个槽位索引单独添加规则，因为 DropRulesEvent 的覆盖规则是针对物品的谓词
                // 这里使用一个简单的谓词：只要物品存在于安全箱槽位中（不区分索引），就应用 ALWAYS_KEEP
                // TODO: 若需要更精确地仅保护槽位中的物品（而非其他来源的同物品），可以通过槽位上下文判断，
                //       但 DropRulesEvent 的 addOverride 只接收 ItemStack 谓词，无法直接区分槽位。
                //       安全做法：为槽位中实际存在的每个物品单独添加覆盖规则（基于物品本身的匹配）。
                //       下面实现为：遍历槽位中所有非空物品，为每个物品添加一个精确匹配的覆盖规则。
                var stacks = stacksHandler.getStacks();
                for (int i = 0; i < stacks.getSlots(); i++) {
                    ItemStack stack = stacks.getStackInSlot(i);
                    if (!stack.isEmpty()) {
                        // 为当前物品添加覆盖规则，确保其死亡时保留
                        event.addOverride(
                                s -> ItemStack.isSameItemSameTags(s, stack),
                                DropRule.ALWAYS_KEEP
                        );
                    }
                }
            });
        });
    }
}