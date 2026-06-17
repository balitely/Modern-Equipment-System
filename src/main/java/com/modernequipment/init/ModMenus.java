package com.modernequipment.init;

import com.modernequipment.MESMod;
import com.modernequipment.gui.EquipmentContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, MESMod.MODID);

    public static final RegistryObject<MenuType<EquipmentContainerMenu>> EQUIPMENT_CONTAINER = MENUS.register("equipment_container",
            () -> IForgeMenuType.create((windowId, inv, data) -> {
                net.minecraft.world.item.ItemStack stack = data.readItem();
                return new EquipmentContainerMenu(windowId, inv, stack);
            }));
}