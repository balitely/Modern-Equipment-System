package com.modernequipment.event;

import com.moderndamage.control.api.ModDamagePart;
import com.moderndamage.control.api.ModDamageSubPart;
import com.moderndamage.control.api.event.ArmorHitEvent;
import com.modernequipment.MESMod;
import com.modernequipment.api.attachment.AttachmentType;
import com.modernequipment.api.equipment.IModifiableEquipment;
import com.modernequipment.network.S2CUpdateEquipmentPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Mod.EventBusSubscriber(modid = MESMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ArmorHitListener {
    private static final String ATTACHMENTS_DURABILITY_KEY = "AttachmentsDurability";

    @SubscribeEvent
    public static void onArmorHit(ArmorHitEvent event) {
        LivingEntity living = event.getEntity();
        if (living.level().isClientSide) return;

        Player player = living instanceof Player ? (Player) living : null;

        Set<AttachmentType> slotsToDamage = new HashSet<>();
        ModDamageSubPart subPart = event.getSubPart();
        ModDamagePart hitPart = event.getHitPart();

        if (subPart != null) {
            switch (subPart) {
                case HEAD_TOP:
                    slotsToDamage.add(AttachmentType.HELMET_TOP);
                    break;
                case HEAD_FACE:
                    slotsToDamage.add(AttachmentType.FACE_SHIELD);
                    break;
                case HEAD_NECK:
                    slotsToDamage.add(AttachmentType.NECK_ARMOR);
                    break;
                case CHEST_FRONT:
                    slotsToDamage.add(AttachmentType.FRONT_PLATE);
                    slotsToDamage.add(AttachmentType.SIDE_PLATE);
                    break;
                case CHEST_BACK:
                    slotsToDamage.add(AttachmentType.BACK_PLATE);
                    slotsToDamage.add(AttachmentType.SIDE_PLATE);
                    break;
                case STOMACH_FRONT:
                    slotsToDamage.add(AttachmentType.FRONT_PLATE);
                    slotsToDamage.add(AttachmentType.GROIN_PLATE);
                    break;
                case STOMACH_BACK:
                    slotsToDamage.add(AttachmentType.BACK_PLATE);
                    slotsToDamage.add(AttachmentType.GROIN_PLATE);
                    break;
                default:
                    break;
            }
        } else if (hitPart != null) {
            switch (hitPart) {
                case HEAD:
                    slotsToDamage.add(AttachmentType.HELMET_TOP);
                    slotsToDamage.add(AttachmentType.FACE_SHIELD);
                    slotsToDamage.add(AttachmentType.NECK_ARMOR);
                    break;
                case CHEST:
                    slotsToDamage.add(AttachmentType.FRONT_PLATE);
                    slotsToDamage.add(AttachmentType.BACK_PLATE);
                    slotsToDamage.add(AttachmentType.SIDE_PLATE);
                    break;
                case STOMACH:
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
            ItemStack equipment = living.getItemBySlot(slot);
            if (equipment.isEmpty()) continue;
            damageAttachmentsInEquipment(living, equipment, slotsToDamage, event.getFinalDamage());
        }

        if (player != null) {
            CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
                handler.getCurios().forEach((id, stacksHandler) -> {
                    var stacks = stacksHandler.getStacks();
                    for (int i = 0; i < stacks.getSlots(); i++) {
                        ItemStack equipment = stacks.getStackInSlot(i);
                        if (equipment.isEmpty()) continue;
                        damageAttachmentsInEquipment(player, equipment, slotsToDamage, event.getFinalDamage());
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