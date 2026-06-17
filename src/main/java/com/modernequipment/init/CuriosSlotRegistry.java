package com.modernequipment.init;

import com.modernequipment.MESMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.InterModComms;
import top.theillusivec4.curios.api.SlotTypeMessage;

public class CuriosSlotRegistry {

    public static void registerSlots() {
        registerSlot("mes_face", 1, 10, new ResourceLocation(MESMod.MODID, "slot/face"));
        registerSlot("mes_chest_rig", 1, 11, new ResourceLocation(MESMod.MODID, "slot/chest_rig"));
        registerSlot("mes_backpack", 1, 12, new ResourceLocation(MESMod.MODID, "slot/backpack"));
        registerSlot("mes_tactical_belt", 1, 13, new ResourceLocation(MESMod.MODID, "slot/tactical_belt"));
        registerSlot("mes_tactical_headset", 1, 14, new ResourceLocation(MESMod.MODID, "slot/tactical_headset"));
        registerSlot("mes_arm_armor", 1, 15, new ResourceLocation(MESMod.MODID, "slot/arm_armor"));
        registerSlot("mes_safe_box", 1, 16, new ResourceLocation(MESMod.MODID, "slot/safe_box"));
    }

    @SuppressWarnings("deprecation")
    private static void registerSlot(String identifier, int size, int priority, ResourceLocation icon) {
        SlotTypeMessage.Builder builder = new SlotTypeMessage.Builder(identifier)
                .size(size)
                .priority(priority);
        if (icon != null) {
            builder.icon(icon);
        }
        InterModComms.sendTo("curios", SlotTypeMessage.REGISTER_TYPE, builder::build);
    }
}