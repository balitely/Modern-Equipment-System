package com.modernequipment.network;

import com.modernequipment.MESMod;
import com.modernequipment.api.attachment.AttachmentType;
import com.modernequipment.api.equipment.IModifiableEquipment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public class C2SUninstallAttachmentPacket {
    private final int equipmentSlot;
    private final AttachmentType targetSlot;

    public C2SUninstallAttachmentPacket(int equipmentSlot, AttachmentType targetSlot) {
        this.equipmentSlot = equipmentSlot;
        this.targetSlot = targetSlot;
    }

    public static void encode(C2SUninstallAttachmentPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.equipmentSlot);
        buf.writeEnum(msg.targetSlot);
    }

    public static C2SUninstallAttachmentPacket decode(FriendlyByteBuf buf) {
        return new C2SUninstallAttachmentPacket(buf.readInt(), buf.readEnum(AttachmentType.class));
    }

    public static void handle(C2SUninstallAttachmentPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            ItemStack equipment = player.getInventory().getItem(msg.equipmentSlot);
            IModifiableEquipment modifiable = IModifiableEquipment.getModifiableOrNull(equipment);
            if (modifiable == null) return;
            ItemStack removed = modifiable.uninstallAttachment(equipment, msg.targetSlot);
            if (!removed.isEmpty()) {
                int freeSlot = player.getInventory().getFreeSlot();
                if (freeSlot != -1) {
                    player.getInventory().setItem(freeSlot, removed.copy());
                    MESMod.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                            new S2CUpdateInventorySlotPacket(freeSlot, removed));
                } else {
                    player.drop(removed, false);
                }
                MESMod.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                        new S2CUpdateEquipmentPacket(msg.equipmentSlot, equipment));
            }
        });
        ctx.get().setPacketHandled(true);
    }
}