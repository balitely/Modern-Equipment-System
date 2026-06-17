package com.modernequipment.core.loader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.modernequipment.MESMod;
import com.modernequipment.core.data.AttachmentData;
import com.modernequipment.core.data.EquipmentData;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipFile;

public class EquipmentPackLoader {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static boolean loaded = false;

    public static final String PACKS_DIR_NAME = "mes_packs";

    public static void loadPacks() {
        if (loaded) return;
        Path packsDir = FMLPaths.GAMEDIR.get().resolve(PACKS_DIR_NAME);
        if (!Files.isDirectory(packsDir)) {
            try {
                Files.createDirectories(packsDir);
                MESMod.LOGGER.info("Created MES packs directory at {}", packsDir);
            } catch (IOException e) {
                MESMod.LOGGER.error("Failed to create MES packs directory", e);
                return;
            }
        }
        MESMod.LOGGER.info("Scanning for equipment packs in {}", packsDir);
        List<Path> packs = findPackPaths(packsDir);
        MESMod.LOGGER.info("Found {} potential pack(s)", packs.size());
        EquipmentDataManager.clear();
        for (Path pack : packs) {
            loadEquipmentFromPack(pack);
        }
        loaded = true;
    }

    private static List<Path> findPackPaths(Path packsDir) {
        List<Path> packs = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(packsDir)) {
            for (Path entry : stream) {
                if (Files.isDirectory(entry)) {
                    packs.add(entry);
                } else if (entry.toString().endsWith(".zip")) {
                    packs.add(entry);
                }
            }
        } catch (IOException e) {
            MESMod.LOGGER.error("Failed to scan packs directory", e);
        }
        return packs;
    }

    private static void loadEquipmentFromPack(Path packPath) {
        if (Files.isDirectory(packPath)) {
            loadFromDirectory(packPath);
        } else if (packPath.toString().endsWith(".zip")) {
            loadFromZip(packPath);
        }
    }

    private static void loadFromDirectory(Path dir) {
        Path equipmentDataDir = dir.resolve("data/modernequipment/equipment");
        if (Files.isDirectory(equipmentDataDir)) {
            loadJsonFiles(equipmentDataDir, true);
        }
        Path attachmentsDataDir = dir.resolve("data/modernequipment/attachments");
        if (Files.isDirectory(attachmentsDataDir)) {
            loadJsonFiles(attachmentsDataDir, false);
        }
    }

    private static void loadFromZip(Path zipPath) {
        try (ZipFile zipFile = new ZipFile(zipPath.toFile())) {
            zipFile.stream().forEach(entry -> {
                String name = entry.getName();
                if (name.startsWith("data/modernequipment/equipment/") && name.endsWith(".json")) {
                    try (InputStream stream = zipFile.getInputStream(entry)) {
                        EquipmentData data = GSON.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), EquipmentData.class);
                        if (data != null && data.getId() != null) {
                            ResourceLocation id = new ResourceLocation(MESMod.MODID, data.getId());
                            EquipmentDataManager.addEquipment(id, data);
                            MESMod.LOGGER.debug("Loaded equipment {} from zip {}", id, zipPath.getFileName());
                        }
                    } catch (IOException | JsonSyntaxException | JsonIOException e) {
                        MESMod.LOGGER.error("Failed to load equipment json {} from zip {}", entry.getName(), zipPath.getFileName(), e);
                    }
                } else if (name.startsWith("data/modernequipment/attachments/") && name.endsWith(".json")) {
                    try (InputStream stream = zipFile.getInputStream(entry)) {
                        AttachmentData data = GSON.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), AttachmentData.class);
                        if (data != null && data.getId() != null) {
                            ResourceLocation id = new ResourceLocation(MESMod.MODID, data.getId());
                            EquipmentDataManager.addAttachment(id, data);
                            MESMod.LOGGER.debug("Loaded attachment {} from zip {}", id, zipPath.getFileName());
                        }
                    } catch (IOException | JsonSyntaxException | JsonIOException e) {
                        MESMod.LOGGER.error("Failed to load attachment json {} from zip {}", entry.getName(), zipPath.getFileName(), e);
                    }
                }
            });
        } catch (IOException e) {
            MESMod.LOGGER.error("Failed to open zip file {}", zipPath, e);
        }
    }

    private static void loadJsonFiles(Path dir, boolean isEquipment) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, path -> path.toString().endsWith(".json"))) {
            for (Path jsonPath : stream) {
                try (InputStream streamIn = Files.newInputStream(jsonPath)) {
                    if (isEquipment) {
                        EquipmentData data = GSON.fromJson(new InputStreamReader(streamIn, StandardCharsets.UTF_8), EquipmentData.class);
                        if (data != null && data.getId() != null) {
                            ResourceLocation id = new ResourceLocation(MESMod.MODID, data.getId());
                            EquipmentDataManager.addEquipment(id, data);
                            MESMod.LOGGER.debug("Loaded equipment {} from {}", id, jsonPath);
                        }
                    } else {
                        AttachmentData data = GSON.fromJson(new InputStreamReader(streamIn, StandardCharsets.UTF_8), AttachmentData.class);
                        if (data != null && data.getCombat() != null) {
                            MESMod.LOGGER.debug("Loaded attachment {} combat: armorLevels={}, armorLevelsSub={}, toughnessSub={}, ricochetSub={}",
                                    data.getId(),
                                    data.getCombat().getArmorLevels(),
                                    data.getCombat().getArmorLevelsSub(),
                                    data.getCombat().getToughnessSub(),
                                    data.getCombat().getRicochetSub());
                        }
                        if (data != null && data.getId() != null) {
                            ResourceLocation id = new ResourceLocation(MESMod.MODID, data.getId());
                            EquipmentDataManager.addAttachment(id, data);
                            MESMod.LOGGER.debug("Loaded attachment {} from {}", id, jsonPath);
                        }
                    }
                } catch (IOException | JsonSyntaxException | JsonIOException e) {
                    MESMod.LOGGER.error("Failed to load json file {}", jsonPath, e);
                }
            }
        } catch (IOException e) {
            MESMod.LOGGER.error("Failed to scan directory {}", dir, e);
        }
    }
}