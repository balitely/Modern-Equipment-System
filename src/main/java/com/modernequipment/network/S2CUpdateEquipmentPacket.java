package com.modernequipment.network;

import com.modernequipment.MESMod;
import com.modernequipment.client.gui.EquipmentRefitScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CUpdateEquipmentPacket {
    private final int slot;
    private final ItemStack updatedStack;

    public S2CUpdateEquipmentPacket(int slot, ItemStack updatedStack) {
        this.slot = slot;
        this.updatedStack = updatedStack;
    }

    public static void encode(S2CUpdateEquipmentPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.slot);
        buf.writeItem(msg.updatedStack);
    }

    public static S2CUpdateEquipmentPacket decode(FriendlyByteBuf buf) {
        return new S2CUpdateEquipmentPacket(buf.readInt(), buf.readItem());
    }

    public static void handle(S2CUpdateEquipmentPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (Minecraft.getInstance().player != null) {
                Minecraft.getInstance().player.getInventory().setItem(msg.slot, msg.updatedStack);
                if (Minecraft.getInstance().screen instanceof EquipmentRefitScreen screen) {
                    screen.refresh();
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}