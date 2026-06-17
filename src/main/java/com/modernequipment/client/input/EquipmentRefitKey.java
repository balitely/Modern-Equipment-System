package com.modernequipment.client.input;

import com.modernequipment.MESMod;
import com.modernequipment.api.equipment.IModifiableEquipment;
import com.modernequipment.client.gui.EquipmentRefitScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = MESMod.MODID, value = Dist.CLIENT)
public class EquipmentRefitKey {
    public static final KeyMapping REFIT_KEY = new KeyMapping(
            "key.modernequipment.refit",
            KeyConflictContext.IN_GAME,
            KeyModifier.NONE,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_Z,
            "key.category.modernequipment"
    );

    public static void register() {
        MinecraftForge.EVENT_BUS.register(EquipmentRefitKey.class);
    }

    @SubscribeEvent
    public static void onKeyPress(InputEvent.Key event) {
        if (event.getAction() == GLFW.GLFW_PRESS && REFIT_KEY.matches(event.getKey(), event.getScanCode())) {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player == null || player.isSpectator()) return;
            if (Minecraft.getInstance().screen == null) {
                if (IModifiableEquipment.getModifiableOrNull(player.getMainHandItem()) != null) {
                    Minecraft.getInstance().setScreen(new EquipmentRefitScreen());
                }
            } else if (Minecraft.getInstance().screen instanceof EquipmentRefitScreen) {
                Minecraft.getInstance().screen.onClose();
            }
        }
    }
}