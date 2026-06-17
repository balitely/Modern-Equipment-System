package com.modernequipment.client;

import com.modernequipment.MESMod;
import com.modernequipment.client.gui.EquipmentInventoryScreen;
import com.modernequipment.client.input.EquipmentRefitKey;
import com.modernequipment.client.renderer.GeoCurioRenderer;
import com.modernequipment.core.item.EquipmentItem;
import com.modernequipment.init.ModMenus;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

@Mod.EventBusSubscriber(modid = MESMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientEvents {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        MenuScreens.register(ModMenus.EQUIPMENT_CONTAINER.get(), EquipmentInventoryScreen::new);
        MESMod.LOGGER.info("ClientEvents.onClientSetup - start");

        event.enqueueWork(() -> {
            MESMod.LOGGER.info("ClientEvents.onClientSetup enqueued work - registering Curios renderers");

            // 列出所有注册的物品用于调试
            int totalItems = 0;
            int eqItems = 0;
            for (Item item : ForgeRegistries.ITEMS.getValues()) {
                totalItems++;
                if (item instanceof EquipmentItem) {
                    eqItems++;
                    MESMod.LOGGER.info("Found EquipmentItem: {} - {}", 
                        ForgeRegistries.ITEMS.getKey(item), item.getClass().getSimpleName());
                }
            }
            MESMod.LOGGER.info("Total items: {}, EquipmentItems: {}", totalItems, eqItems);

            ForgeRegistries.ITEMS.getValues().stream()
                    .filter(item -> item instanceof EquipmentItem)
                    .forEach(item -> {
                        MESMod.LOGGER.info("Registering Curios renderer for item: {}", item);
                        CuriosRendererRegistry.register(item, GeoCurioRenderer::new);
                    });
            MESMod.LOGGER.info("ClientEvents.onClientSetup enqueued work - registration complete");
        });
    }

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(EquipmentRefitKey.REFIT_KEY);
    }
}