package com.modernequipment.core.item;

import com.modernequipment.MESMod;
import com.modernequipment.api.attachment.AttachmentType;
import com.modernequipment.api.attachment.IAttachment;
import com.modernequipment.api.equipment.IModifiableEquipment;
import com.modernequipment.client.animator.EquipmentArmorAnimator;
import com.modernequipment.core.data.*;
import com.modernequipment.core.inventory.CombinedItemHandler;
import com.modernequipment.core.inventory.EquipmentSubInventoryHandler;
import com.modernequipment.core.loader.EquipmentDataManager;
import com.modernequipment.util.AttachmentCompatibilityHelper;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;
import software.bernie.geckolib.util.RenderUtils;

public class EquipmentArmorItem extends ArmorItem implements ICurioItem, IModifiableEquipment, GeoItem {

    private final EquipmentData data;
    private final List<SlotDefinition> slotDefinitions;
    private static final String TAG_ATTACHMENTS = "Attachments";
    private static final String TAG_ATTACHMENTS_DURABILITY = "AttachmentsDurability";

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public EquipmentArmorItem(ArmorItem.Type type, Properties properties, EquipmentData data) {
        super(EquipmentArmorMaterial.MES_EQUIPMENT, type, properties.durability(data.getDurability()));
        this.data = data;
        InventoryProperties inv = data.getInventory();
        if (inv != null && "custom".equals(inv.getType()) && inv.getSlots() != null) {
            this.slotDefinitions = inv.getSlots();
        } else {
            this.slotDefinitions = new ArrayList<>();
        }
    }

    public EquipmentData getData() { return data; }
    public List<SlotDefinition> getSlotDefinitions() { return slotDefinitions; }

    @Nonnull
    public List<EquipmentSubInventoryHandler> getSubHandlers(ItemStack stack) {
        List<EquipmentSubInventoryHandler> handlers = new ArrayList<>();
        CompoundTag tag = stack.getTag();
        ListTag inventoriesTag = tag != null ? tag.getList("SubInventories", Tag.TAG_COMPOUND) : new ListTag();
        for (int i = 0; i < slotDefinitions.size(); i++) {
            SlotDefinition def = slotDefinitions.get(i);
            EquipmentSubInventoryHandler handler = new EquipmentSubInventoryHandler(def);
            if (i < inventoriesTag.size()) {
                handler.deserializeNBT(inventoriesTag.getCompound(i));
            }
            handlers.add(handler);
        }
        return handlers;
    }

    public static void saveSubHandlers(ItemStack stack, List<EquipmentSubInventoryHandler> handlers) {
        CompoundTag tag = stack.getOrCreateTag();
        ListTag list = new ListTag();
        for (EquipmentSubInventoryHandler handler : handlers) {
            list.add(handler.serializeNBT());
        }
        tag.put("SubInventories", list);
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        List<IItemHandler> handlers = new ArrayList<>();
        if (data.hasInventory() && !slotDefinitions.isEmpty()) {
            handlers.addAll(getSubHandlers(stack));
        }
        handlers.add(new EquipmentAttachmentHandler(stack, this));
        if (handlers.isEmpty()) {
            return super.initCapabilities(stack, nbt);
        }
        return new ICapabilityProvider() {
            private final LazyOptional<IItemHandler> lazyOptional = LazyOptional.of(() -> new CombinedItemHandler(handlers));
            @Override
            public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
                if (cap == ForgeCapabilities.ITEM_HANDLER) {
                    return lazyOptional.cast();
                }
                return LazyOptional.empty();
            }
        };
    }

    @Override
    public CompoundTag getShareTag(ItemStack stack) {
        CompoundTag tag = super.getShareTag(stack);
        if (tag == null) tag = new CompoundTag();
        List<EquipmentSubInventoryHandler> handlers = getSubHandlers(stack);
        ListTag list = new ListTag();
        for (EquipmentSubInventoryHandler handler : handlers) {
            list.add(handler.serializeNBT());
        }
        tag.put("SubInventories", list);
        return tag;
    }

    @Override
    public void readShareTag(ItemStack stack, @Nullable CompoundTag nbt) {
        super.readShareTag(stack, nbt);
        if (nbt != null && nbt.contains("SubInventories")) {
            ListTag list = nbt.getList("SubInventories", Tag.TAG_COMPOUND);
            List<EquipmentSubInventoryHandler> handlers = getSubHandlers(stack);
            for (int i = 0; i < list.size() && i < handlers.size(); i++) {
                handlers.get(i).deserializeNBT(list.getCompound(i));
            }
            saveSubHandlers(stack, handlers);
        }
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> original = super.getAttributeModifiers(slot, stack);
        Multimap<Attribute, AttributeModifier> modifiers = HashMultimap.create();
        modifiers.putAll(original);
        if (slot == getType().getSlot()) {
            addMESModifiers(modifiers, stack);
        }
        return modifiers;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(String identifier, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> modifiers = HashMultimap.create();
        addMESModifiers(modifiers, stack);
        return modifiers;
    }

    private void addModifier(Multimap<Attribute, AttributeModifier> modifiers, Attribute attribute, float amount, AttributeModifier.Operation operation, String name) {
        if (attribute == null || amount == 0) return;
        UUID uuid = UUID.nameUUIDFromBytes(name.getBytes());
        modifiers.put(attribute, new AttributeModifier(uuid, name, amount, operation));
    }

    private void addMESModifiers(Multimap<Attribute, AttributeModifier> modifiers, ItemStack stack) {
        if (data == null) return;
        if (data.getModifiers() != null) {
            float moveSpeed = data.getModifiers().getMovementSpeed();
            float ergonomics = data.getModifiers().getErgonomics();
            addModifier(modifiers, Attributes.MOVEMENT_SPEED, moveSpeed, AttributeModifier.Operation.MULTIPLY_TOTAL, "mes_self_speed");
            Attribute ergoAttr = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation("moderndamage", "ergonomics"));
            if (ergoAttr != null) {
                addModifier(modifiers, ergoAttr, ergonomics, AttributeModifier.Operation.ADDITION, "mes_self_ergo");
            }
        }
        IModifiableEquipment modifiable = IModifiableEquipment.getModifiableOrNull(stack);
        if (modifiable != null) {
            Map<AttachmentType, ResourceLocation> attachments = modifiable.getAttachments(stack);
            for (Map.Entry<AttachmentType, ResourceLocation> entry : attachments.entrySet()) {
                AttachmentType slot = entry.getKey();
                ResourceLocation attId = entry.getValue();
                AttachmentData attData = EquipmentDataManager.getAttachment(attId);
                if (attData == null) continue;
                ModifierProperties modProps = null;
                if (attData.getMountEffects() != null && attData.getMountEffects().containsKey(slot.name().toLowerCase())) {
                    CombatProperties combat = attData.getMountEffects().get(slot.name().toLowerCase());
                    if (combat != null) modProps = combat.getModifiers();
                }
                if (modProps == null) modProps = attData.getModifiers();
                if (modProps == null) continue;
                float moveSpeed = modProps.getMovementSpeed();
                float ergonomics = modProps.getErgonomics();
                String attIdStr = attId.toString();
                addModifier(modifiers, Attributes.MOVEMENT_SPEED, moveSpeed, AttributeModifier.Operation.MULTIPLY_TOTAL, "mes_att_speed_" + attIdStr + "_" + slot.name());
                Attribute ergoAttr = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation("moderndamage", "ergonomics"));
                if (ergoAttr != null) {
                    addModifier(modifiers, ergoAttr, ergonomics, AttributeModifier.Operation.ADDITION, "mes_att_ergo_" + attIdStr + "_" + slot.name());
                }
            }
        }
    }

    @Override
    public Map<AttachmentType, ResourceLocation> getAttachments(ItemStack stack) {
        Map<AttachmentType, ResourceLocation> map = new HashMap<>();
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(TAG_ATTACHMENTS)) {
            CompoundTag attTag = tag.getCompound(TAG_ATTACHMENTS);
            for (String key : attTag.getAllKeys()) {
                AttachmentType type = AttachmentType.fromString(key);
                ResourceLocation id = new ResourceLocation(attTag.getString(key));
                map.put(type, id);
            }
        }
        return map;
    }

    @Override
    public boolean allowAttachment(ItemStack equipment, ItemStack attachment) {
        ResourceLocation attId = ForgeRegistries.ITEMS.getKey(attachment.getItem());
        if (attId == null) return false;
        AttachmentData attData = EquipmentDataManager.getAttachment(attId);
        if (attData == null) return false;
        EquipmentData eqData = this.getData();
        return AttachmentCompatibilityHelper.isCompatible(attachment, equipment, eqData, attData);
    }

    @Override
    public boolean installAttachment(ItemStack equipment, ItemStack attachment, AttachmentType slot) {
        if (!allowAttachment(equipment, attachment)) return false;
        if (getAttachments(equipment).containsKey(slot)) return false;
        IAttachment att = IAttachment.getIAttachmentOrNull(attachment);
        if (att == null) return false;
        ResourceLocation attId = ForgeRegistries.ITEMS.getKey(attachment.getItem());
        AttachmentData attData = EquipmentDataManager.getAttachment(attId);
        if (attData != null && attData.getMountSlots() != null) {
            if (!attData.getMountSlots().contains(slot.name().toLowerCase())) return false;
        }
        ResourceLocation id = att.getAttachmentId(attachment);
        CompoundTag tag = equipment.getOrCreateTag();
        CompoundTag attTag = tag.getCompound(TAG_ATTACHMENTS);
        attTag.putString(slot.name(), id.toString());
        tag.put(TAG_ATTACHMENTS, attTag);
        if (attData != null && attData.getDurability() > 0) {
            int maxDura = attData.getDurability();
            int currentDura = maxDura - attachment.getDamageValue();
            if (currentDura < 0) currentDura = 0;
            CompoundTag durabilityTag = tag.getCompound(TAG_ATTACHMENTS_DURABILITY);
            String durabilityKey = slot.name() + "_" + id.toString();
            durabilityTag.putInt(durabilityKey, currentDura);
            tag.put(TAG_ATTACHMENTS_DURABILITY, durabilityTag);
        }
        return true;
    }

    @Override
    public ItemStack uninstallAttachment(ItemStack equipment, AttachmentType slot) {
        CompoundTag tag = equipment.getTag();
        if (tag == null) return ItemStack.EMPTY;
        CompoundTag attTag = tag.getCompound(TAG_ATTACHMENTS);
        String idStr = attTag.getString(slot.name());
        if (idStr.isEmpty()) return ItemStack.EMPTY;
        attTag.remove(slot.name());
        tag.put(TAG_ATTACHMENTS, attTag);
        int currentDura = -1;
        CompoundTag durabilityTag = tag.getCompound(TAG_ATTACHMENTS_DURABILITY);
        String durabilityKey = slot.name() + "_" + idStr;
        if (durabilityTag.contains(durabilityKey)) {
            currentDura = durabilityTag.getInt(durabilityKey);
            durabilityTag.remove(durabilityKey);
            tag.put(TAG_ATTACHMENTS_DURABILITY, durabilityTag);
        }
        ResourceLocation id = new ResourceLocation(idStr);
        Item item = ForgeRegistries.ITEMS.getValue(id);
        if (item == null) return ItemStack.EMPTY;
        ItemStack result = new ItemStack(item);
        if (result.getMaxDamage() > 0 && currentDura >= 0) {
            int damage = result.getMaxDamage() - currentDura;
            if (damage > 0) result.setDamageValue(damage);
        }
        return result;
    }

    @Override
    public AttachmentType[] getAllowedAttachmentTypes(ItemStack equipment) {
        if (data.getAllowAttachmentTypes() == null) return new AttachmentType[0];
        return data.getAllowAttachmentTypes().stream()
                .map(AttachmentType::fromString)
                .filter(t -> t != AttachmentType.NONE)
                .toArray(AttachmentType[]::new);
    }

    private class EquipmentAttachmentHandler implements net.minecraftforge.items.IItemHandler {
        private final ItemStack equipment;
        private final List<Map.Entry<AttachmentType, ResourceLocation>> entryList;

        public EquipmentAttachmentHandler(ItemStack equipment, IModifiableEquipment modifiable) {
            this.equipment = equipment;
            this.entryList = new ArrayList<>(modifiable.getAttachments(equipment).entrySet());
        }

        private int getAttachmentCurrentDura(ItemStack equipment, AttachmentType slot, ResourceLocation attId, int maxDura) {
            CompoundTag tag = equipment.getTag();
            if (tag == null) return maxDura;
            CompoundTag durTag = tag.getCompound(TAG_ATTACHMENTS_DURABILITY);
            String key = slot.name() + "_" + attId.toString();
            if (durTag.contains(key)) return durTag.getInt(key);
            return maxDura;
        }

        @Override
        public int getSlots() { return entryList.size(); }

        @Override
        public ItemStack getStackInSlot(int slot) {
            if (slot < 0 || slot >= entryList.size()) return ItemStack.EMPTY;
            Map.Entry<AttachmentType, ResourceLocation> entry = entryList.get(slot);
            ResourceLocation attId = entry.getValue();
            Item item = ForgeRegistries.ITEMS.getValue(attId);
            if (item == null) return ItemStack.EMPTY;
            int maxDura = item.getMaxDamage();
            if (maxDura <= 0) return new ItemStack(item);
            int currentDura = getAttachmentCurrentDura(equipment, entry.getKey(), attId, maxDura);
            if (currentDura <= 0) return ItemStack.EMPTY;
            ItemStack stack = new ItemStack(item);
            int damage = maxDura - currentDura;
            if (damage > 0) stack.setDamageValue(damage);
            return stack;
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) { return stack; }
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) { return ItemStack.EMPTY; }
        @Override
        public int getSlotLimit(int slot) { return 64; }
        @Override
        public boolean isItemValid(int slot, ItemStack stack) { return false; }
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private EquipmentArmorAnimator renderer;

            @Override
            public @NotNull HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
                if (this.renderer == null) {
                    this.renderer = new EquipmentArmorAnimator();
                }
                this.renderer.prepForRender(livingEntity, itemStack, equipmentSlot, original);
                return this.renderer;
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public double getTick(Object o) {
        return RenderUtils.getCurrentTick();
    }
}