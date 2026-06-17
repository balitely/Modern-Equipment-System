package com.modernequipment.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CUpdateInventorySlotPacket {
    private final int slot;
    private final ItemStack stack;

    public S2CUpdateInventorySlotPacket(int slot, ItemStack stack) {
        this.slot = slot;
        this.stack = stack;
    }

    public static void encode(S2CUpdateInventorySlotPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.slot);
        buf.writeItem(msg.stack);
    }

    public static S2CUpdateInventorySlotPacket decode(FriendlyByteBuf buf) {
        return new S2CUpdateInventorySlotPacket(buf.readInt(), buf.readItem());
    }

    public static void handle(S2CUpdateInventorySlotPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (Minecraft.getInstance().player != null) {
                Minecraft.getInstance().player.getInventory().setItem(msg.slot, msg.stack);
                // 刷新改装界面（如果打开）
                if (Minecraft.getInstance().screen instanceof com.modernequipment.client.gui.EquipmentRefitScreen screen) {
                    screen.refresh();
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}