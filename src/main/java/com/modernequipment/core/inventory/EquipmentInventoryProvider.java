package com.modernequipment.core.inventory;

import com.modernequipment.core.data.EquipmentData;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EquipmentInventoryProvider implements ICapabilityProvider, ICapabilitySerializable<CompoundTag> {
    private final EquipmentInventoryHandler inventory;
    private final LazyOptional<IItemHandler> lazyOptional;

    public EquipmentInventoryProvider(EquipmentData data) {
        this.inventory = new EquipmentInventoryHandler(data);
        this.lazyOptional = LazyOptional.of(() -> inventory);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return ForgeCapabilities.ITEM_HANDLER.orEmpty(cap, lazyOptional);
    }

    @Override
    public CompoundTag serializeNBT() {
        return inventory.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        inventory.deserializeNBT(nbt);
    }
}