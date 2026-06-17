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

public class C2SInstallAttachmentPacket {
    private final int equipmentSlot;
    private final int attachmentSlot;
    private final AttachmentType targetSlot;

    public C2SInstallAttachmentPacket(int equipmentSlot, int attachmentSlot, AttachmentType targetSlot) {
        this.equipmentSlot = equipmentSlot;
        this.attachmentSlot = attachmentSlot;
        this.targetSlot = targetSlot;
    }

    public static void encode(C2SInstallAttachmentPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.equipmentSlot);
        buf.writeInt(msg.attachmentSlot);
        buf.writeEnum(msg.targetSlot);
    }

    public static C2SInstallAttachmentPacket decode(FriendlyByteBuf buf) {
        return new C2SInstallAttachmentPacket(buf.readInt(), buf.readInt(), buf.readEnum(AttachmentType.class));
    }

    public static void handle(C2SInstallAttachmentPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            ItemStack equipment = player.getInventory().getItem(msg.equipmentSlot);
            IModifiableEquipment modifiable = IModifiableEquipment.getModifiableOrNull(equipment);
            if (modifiable == null) return;
            ItemStack attachment = player.getInventory().getItem(msg.attachmentSlot);
            if (attachment.isEmpty()) return;

            if (modifiable.allowAttachment(equipment, attachment)) {
                if (modifiable.installAttachment(equipment, attachment, msg.targetSlot)) {
                    attachment.shrink(1);
                    // 更新装备物品到客户端
                    MESMod.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                            new S2CUpdateEquipmentPacket(msg.equipmentSlot, equipment));
                    // 更新背包配件槽位（已扣除）
                    MESMod.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                            new S2CUpdateInventorySlotPacket(msg.attachmentSlot, ItemStack.EMPTY));
                }
            } else {
                player.displayClientMessage(net.minecraft.network.chat.Component.literal("Attachment not compatible!"), true);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}