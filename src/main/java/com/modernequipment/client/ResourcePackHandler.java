package com.modernequipment.client;

import com.modernequipment.MESMod;
import com.modernequipment.core.loader.EquipmentPackLoader;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Mod.EventBusSubscriber(modid = MESMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ResourcePackHandler {

    @SubscribeEvent
    public static void onAddPackFinders(AddPackFindersEvent event) {
        Path packsDir = FMLPaths.GAMEDIR.get().resolve(EquipmentPackLoader.PACKS_DIR_NAME);
        if (!Files.isDirectory(packsDir)) return;

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(packsDir)) {
            for (Path packPath : stream) {
                String baseName = packPath.getFileName().toString().replaceAll("[^a-zA-Z0-9._-]", "_");

                if (event.getPackType() == PackType.CLIENT_RESOURCES) {
                    registerPack(event, packPath, "mes_pack_res_" + baseName, "MES Equipment Pack (Resources)");
                }
                else if (event.getPackType() == PackType.SERVER_DATA) {
                    registerPack(event, packPath, "mes_pack_data_" + baseName, "MES Equipment Pack (Data)");
                }
            }
        } catch (IOException e) {
            MESMod.LOGGER.error("Failed to register MES packs", e);
        }
    }

    private static void registerPack(AddPackFindersEvent event, Path packPath, String packId, String displayName) {
        PackResources resources;
        if (Files.isDirectory(packPath)) {
            resources = new PathPackResources(packId, packPath, true);
        } else if (packPath.toString().endsWith(".zip")) {
            resources = new FilePackResources(packId, packPath.toFile(), true);
        } else {
            return;
        }

        Pack pack = Pack.readMetaAndCreate(
                packId,
                Component.literal(displayName + ": " + packPath.getFileName().toString()),
                true, // 默认启用
                (id) -> resources,
                event.getPackType(),
                Pack.Position.BOTTOM,
                PackSource.DEFAULT
        );

        if (pack != null) {
            event.addRepositorySource(consumer -> consumer.accept(pack));
            MESMod.LOGGER.info("Registered MES pack as {}: {}", event.getPackType(), packPath.getFileName());
        }
    }
}