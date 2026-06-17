package com.modernequipment.compat;

import com.modernequipment.MESMod;
import com.modernequipment.core.data.AttachmentData;
import com.modernequipment.core.data.EquipmentData;
import com.modernequipment.core.loader.EquipmentDataManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.reflect.Method;

public class EZWeightCompatibility {

    private static boolean initialized = false;

    public static void syncWeights() {
        if (initialized) return;
        if (!ModList.get().isLoaded("ezweight")) {
            MESMod.LOGGER.debug("EZWeight not loaded, skipping weight sync");
            return;
        }

        try {
            Class<?> registryClass = Class.forName("com.armilp.ezweight.data.ItemWeightRegistry");
            Method setWeightMethod = registryClass.getMethod("setWeight", ResourceLocation.class, double.class);
            Method saveToFileMethod = registryClass.getMethod("saveToFile", java.io.File.class);
            Method getConfigFileMethod = registryClass.getMethod("getConfigFile");

            boolean changed = false;

            for (EquipmentData data : EquipmentDataManager.getAllEquipment()) {
                ResourceLocation id = new ResourceLocation(MESMod.MODID, data.getId());
                if (ForgeRegistries.ITEMS.containsKey(id)) {
                    double weight = data.getWeight();
                    if (weight > 0) {
                        setWeightMethod.invoke(null, id, weight);
                        changed = true;
                    }
                }
            }

            for (AttachmentData data : EquipmentDataManager.getAllAttachments()) {
                ResourceLocation id = new ResourceLocation(MESMod.MODID, data.getId());
                if (ForgeRegistries.ITEMS.containsKey(id)) {
                    double weight = data.getWeight();
                    if (weight > 0) {
                        setWeightMethod.invoke(null, id, weight);
                        changed = true;
                    }
                }
            }

            if (changed) {
                Object configFile = getConfigFileMethod.invoke(null);
                saveToFileMethod.invoke(null, configFile);
                MESMod.LOGGER.info("MES equipment weights have been saved to EZWeight config");
            }

            initialized = true;
        } catch (Exception e) {
            MESMod.LOGGER.error("Failed to sync weights with EZWeight", e);
        }
    }
}