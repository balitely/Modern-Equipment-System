package com.modernequipment.event;

import com.modernequipment.MESMod;
import com.modernequipment.api.attachment.AttachmentType;
import com.modernequipment.api.equipment.IModifiableEquipment;
import com.modernequipment.compat.ModernDamageCompat;
import com.modernequipment.network.S2CUpdateEquipmentPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 装甲受击监听器。
 * 不再直接订阅 ArmorHitEvent（MDC 事件），而是通过 MESMod 动态注册。
 * 此类的 handleArmorHit 方法由反射调用，仅在 MDC 加载时激活。
 */
public class ArmorHitListener {
    private static final String ATTACHMENTS_DURABILITY_KEY = "AttachmentsDurability";

    /**
     * 外部入口——由 MESMod 在 MDC 加载时通过反射注册到事件总线。
     * event 参数是 com.moderndamage.control.api.event.ArmorHitEvent 的实例。
     */
    public static void handleArmorHit(Event event) {
        ModernDamageCompat.ArmorHitEventInfo info = ModernDamageCompat.extractArmorHitInfo(event);
        if (info == null || info.entity == null) return;
        if (info.entity.level().isClientSide) return;

        Player player = info.entity instanceof Player ? (Player) info.entity : null;

        Set<AttachmentType> slotsToDamage = new HashSet<>();

        if (info.subPartKey != null) {
            switch (info.subPartKey) {
                case "head_top":
                    slotsToDamage.add(AttachmentType.HELMET_TOP);
                    break;
                case "head_face":
                    slotsToDamage.add(AttachmentType.FACE_SHIELD);
                    break;
                case "head_neck":
                    slotsToDamage.add(AttachmentType.NECK_ARMOR);
                    break;
                case "chest_front":
                    slotsToDamage.add(AttachmentType.FRONT_PLATE);
                    slotsToDamage.add(AttachmentType.SIDE_PLATE);
                    break;
                case "chest_back":
                    slotsToDamage.add(AttachmentType.BACK_PLATE);
                    slotsToDamage.add(AttachmentType.SIDE_PLATE);
                    break;
                case "stomach_front":
                    slotsToDamage.add(AttachmentType.FRONT_PLATE);
                    slotsToDamage.add(AttachmentType.GROIN_PLATE);
                    break;
                case "stomach_back":
                    slotsToDamage.add(AttachmentType.BACK_PLATE);
                    slotsToDamage.add(AttachmentType.GROIN_PLATE);
                    break;
                default:
                    break;
            }
        } else if (info.hitPartName != null) {
            switch (info.hitPartName) {
                case "HEAD":
                    slotsToDamage.add(AttachmentType.HELMET_TOP);
                    slotsToDamage.add(AttachmentType.FACE_SHIELD);
                    slotsToDamage.add(AttachmentType.NECK_ARMOR);
                    break;
                case "CHEST":
                    slotsToDamage.add(AttachmentType.FRONT_PLATE);
                    slotsToDamage.add(AttachmentType.BACK_PLATE);
                    slotsToDamage.add(AttachmentType.SIDE_PLATE);
                    break;
                case "STOMACH":
                    slotsToDamage.add(AttachmentType.FRONT_PLATE);
                    slotsToDamage.add(AttachmentType.BACK_PLATE);
                    slotsToDamage.add(AttachmentType.GROIN_PLATE);
                    break;
                default:
                    break;
            }
        }

        if (slotsToDamage.isEmpty()) return;

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() != EquipmentSlot.Type.ARMOR) continue;
            ItemStack equipment = info.entity.getItemBySlot(slot);
            if (equipment.isEmpty()) continue;
            damageAttachmentsInEquipment(info.entity, equipment, slotsToDamage, info.finalDamage);
        }

        if (player != null) {
            CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
                handler.getCurios().forEach((id, stacksHandler) -> {
                    var stacks = stacksHandler.getStacks();
                    for (int i = 0; i < stacks.getSlots(); i++) {
                        ItemStack equipment = stacks.getStackInSlot(i);
                        if (equipment.isEmpty()) continue;
                        damageAttachmentsInEquipment(player, equipment, slotsToDamage, info.finalDamage);
                    }
                });
            });
        }
    }

    private static void damageAttachmentsInEquipment(LivingEntity holder, ItemStack equipment, Set<AttachmentType> slotsToDamage, float damage) {
        IModifiableEquipment modifiable = IModifiableEquipment.getModifiableOrNull(equipment);
        if (modifiable == null) return;

        Map<AttachmentType, ResourceLocation> attachments = modifiable.getAttachments(equipment);
        if (attachments.isEmpty()) return;

        CompoundTag tag = equipment.getOrCreateTag();
        CompoundTag durTag = tag.getCompound(ATTACHMENTS_DURABILITY_KEY);
        boolean changed = false;

        for (AttachmentType slot : slotsToDamage) {
            ResourceLocation attId = attachments.get(slot);
            if (attId == null) continue;
            String durabilityKey = slot.name() + "_" + attId.toString();
            int old = durTag.getInt(durabilityKey);
            if (old <= 0) continue;
            int loss = Math.max(1, (int) (damage * 0.2f));
            int newDura = Math.max(0, old - loss);
            durTag.putInt(durabilityKey, newDura);
            changed = true;
        }

        if (changed) {
            tag.put(ATTACHMENTS_DURABILITY_KEY, durTag);
            if (holder instanceof ServerPlayer sp) {
                int slotIndex = -1;
                for (int i = 0; i < sp.getInventory().getContainerSize(); i++) {
                    if (sp.getInventory().getItem(i) == equipment) {
                        slotIndex = i;
                        break;
                    }
                }
                if (slotIndex != -1) {
                    MESMod.CHANNEL.send(PacketDistributor.PLAYER.with(() -> sp),
                            new S2CUpdateEquipmentPacket(slotIndex, equipment));
                } else {
                    MESMod.LOGGER.warn("ArmorHitListener: Could not find equipment slot index for sync");
                }
            }
        }
    }
}