package com.modernequipment.compat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.modernequipment.MESMod;
import com.modernequipment.compat.ModernDamageCompat;
import com.modernequipment.core.data.CombatProperties;
import com.modernequipment.core.data.EquipmentData;
import com.modernequipment.core.loader.EquipmentDataManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class MDCConfigWriter {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FMLPaths.CONFIGDIR.get().resolve("moderndamage/armor_properties.json");
    private static boolean written = false;

    public static void updateAndReload() {
        if (written) return;
        if (!ModernDamageCompat.isLoaded()) {
            MESMod.LOGGER.debug("MDC not loaded, skipping MDC config write");
            return;
        }
        try {
            JsonObject root = readOrCreateConfig();
            boolean changed = false;

            for (EquipmentData data : EquipmentDataManager.getAllEquipment()) {
                CombatProperties combat = data.getCombat();
                if (combat == null) {
                    MESMod.LOGGER.debug("No combat data for {}", data.getId());
                    continue;
                }

                ResourceLocation id = new ResourceLocation(MESMod.MODID, data.getId());
                Item item = ForgeRegistries.ITEMS.getValue(id);
                if (item == null) {
                    MESMod.LOGGER.warn("Item not found for MDC config: {}", id);
                    continue;
                }

                String key = id.toString();
                JsonObject itemObj = root.has(key) ? root.getAsJsonObject(key) : new JsonObject();

                if (combat.getArmorLevels() != null && !combat.getArmorLevels().isEmpty()) {
                    JsonObject coverage = new JsonObject();
                    for (Map.Entry<String, Integer> e : combat.getArmorLevels().entrySet()) {
                        coverage.addProperty(e.getKey().toLowerCase(), e.getValue());
                    }
                    itemObj.add("coverage", coverage);
                    MESMod.LOGGER.info("Added coverage for {}: {}", key, coverage);
                }

                if (combat.getToughness() != null && !combat.getToughness().isEmpty()) {
                    JsonObject toughness = new JsonObject();
                    for (Map.Entry<String, Integer> e : combat.getToughness().entrySet()) {
                        toughness.addProperty(e.getKey().toLowerCase(), e.getValue());
                    }
                    itemObj.add("toughness", toughness);
                }

                if (combat.getMaterialFactor() != null && !combat.getMaterialFactor().isEmpty()) {
                    JsonObject mat = new JsonObject();
                    for (Map.Entry<String, Float> e : combat.getMaterialFactor().entrySet()) {
                        mat.addProperty(e.getKey().toLowerCase(), e.getValue());
                    }
                    itemObj.add("material_factor", mat);
                }

                if (combat.getRicochetChance() != null && !combat.getRicochetChance().isEmpty()) {
                    JsonObject rc = new JsonObject();
                    for (Map.Entry<String, Float> e : combat.getRicochetChance().entrySet()) {
                        rc.addProperty(e.getKey().toLowerCase(), e.getValue());
                    }
                    itemObj.add("ricochet_chance", rc);
                }

                if (combat.getArmorLevelsSub() != null && !combat.getArmorLevelsSub().isEmpty()) {
                    JsonObject subCoverage = new JsonObject();
                    for (Map.Entry<String, Integer> e : combat.getArmorLevelsSub().entrySet()) {
                        subCoverage.addProperty(e.getKey().toLowerCase(), e.getValue());
                    }
                    itemObj.add("coverage_sub", subCoverage);
                    MESMod.LOGGER.info("Added coverage_sub for {}: {}", key, subCoverage);
                }

                if (combat.getToughnessSub() != null && !combat.getToughnessSub().isEmpty()) {
                    JsonObject subToughness = new JsonObject();
                    for (Map.Entry<String, Integer> e : combat.getToughnessSub().entrySet()) {
                        subToughness.addProperty(e.getKey().toLowerCase(), e.getValue());
                    }
                    itemObj.add("toughness_sub", subToughness);
                    MESMod.LOGGER.info("Added toughness_sub for {}: {}", key, subToughness);
                }

                if (combat.getRicochetSub() != null && !combat.getRicochetSub().isEmpty()) {
                    JsonObject subRicochet = new JsonObject();
                    for (Map.Entry<String, Float> e : combat.getRicochetSub().entrySet()) {
                        subRicochet.addProperty(e.getKey().toLowerCase(), e.getValue());
                    }
                    itemObj.add("ricochet_sub", subRicochet);
                    MESMod.LOGGER.info("Added ricochet_sub for {}: {}", key, subRicochet);
                }

                root.add(key, itemObj);
                changed = true;
            }

            if (changed) {
                writeConfig(root);
                try {
                    ModernDamageCompat.reloadArmorData();
                    MESMod.LOGGER.info("MDC config reloaded successfully");
                } catch (Exception e) {
                    MESMod.LOGGER.error("Failed to reload MDC config. MES armor data may need a game restart to take effect.", e);
                }
            } else {
                MESMod.LOGGER.debug("No MES combat data found, MDC config unchanged");
            }
            written = true;
        } catch (Exception e) {
            MESMod.LOGGER.error("Unexpected error while writing MDC config", e);
        }
    }

    private static JsonObject readOrCreateConfig() {
        if (Files.exists(CONFIG_PATH)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
                return GSON.fromJson(reader, JsonObject.class);
            } catch (IOException e) {
                MESMod.LOGGER.error("Failed to read MDC config, will create new one", e);
            }
        }
        return new JsonObject();
    }

    private static void writeConfig(JsonObject root) {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(root, writer);
            }
        } catch (IOException e) {
            MESMod.LOGGER.error("Failed to write MDC config", e);
        }
    }
}