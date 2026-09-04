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

                JsonObject mat = new JsonObject();
                if (combat.getMaterialFactor() != null && !combat.getMaterialFactor().isEmpty()) {
                    for (Map.Entry<String, Float> e : combat.getMaterialFactor().entrySet()) {
                        if (e.getKey() != null && e.getValue() != null && e.getValue() > 0.0f) {
                            mat.addProperty(e.getKey().toLowerCase(), e.getValue());
                        }
                    }
                }

                // MDC 1.0.32 does not have a native material_factor_sub field.
                // Do not silently discard MES data: if a parent factor is absent,
                // collapse sub-part factors into the corresponding MDC parent part.
                if (combat.getMaterialFactorSub() != null && !combat.getMaterialFactorSub().isEmpty()) {
                    Map<String, float[]> derived = new java.util.HashMap<>();
                    for (Map.Entry<String, Float> e : combat.getMaterialFactorSub().entrySet()) {
                        String parent = parentPartForSubKey(e.getKey());
                        Float value = e.getValue();
                        if (parent == null || value == null || value <= 0.0f) continue;
                        float[] acc = derived.computeIfAbsent(parent, k -> new float[2]);
                        acc[0] += value;
                        acc[1] += 1.0f;
                    }
                    for (Map.Entry<String, float[]> e : derived.entrySet()) {
                        if (!mat.has(e.getKey()) && e.getValue()[1] > 0.0f) {
                            mat.addProperty(e.getKey(), e.getValue()[0] / e.getValue()[1]);
                        }
                    }
                    MESMod.LOGGER.debug("MDC 1.0.32 has no material_factor_sub; collapsed MES sub-part factors for {} into parent material_factor", key);
                }
                if (!mat.entrySet().isEmpty()) {
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


    private static String parentPartForSubKey(String subKey) {
        if (subKey == null) return null;
        String key = subKey.toLowerCase(java.util.Locale.ROOT);
        if (key.startsWith("head_")) return "head";
        if (key.startsWith("chest_")) return "chest";
        if (key.startsWith("stomach_")) return "stomach";
        if (key.startsWith("left_shoulder") || key.startsWith("left_forearm")) return "left_arm";
        if (key.startsWith("right_shoulder") || key.startsWith("right_forearm")) return "right_arm";
        if (key.startsWith("left_thigh") || key.startsWith("left_calf") || key.startsWith("left_foot")) return "left_leg";
        if (key.startsWith("right_thigh") || key.startsWith("right_calf") || key.startsWith("right_foot")) return "right_leg";
        return null;
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