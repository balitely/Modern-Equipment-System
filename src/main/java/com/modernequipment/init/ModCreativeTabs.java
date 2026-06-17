package com.modernequipment.init;

import com.modernequipment.MESMod;
import com.modernequipment.core.data.AttachmentData;
import com.modernequipment.core.data.EquipmentData;
import com.modernequipment.core.loader.EquipmentDataManager;
import com.modernequipment.core.loader.EquipmentPackLoader;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MESMod.MODID);

    // 头盔
    public static final RegistryObject<CreativeModeTab> HELMET_TAB = TABS.register("helmets",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.modernequipment.helmets"))
                    .icon(() -> new ItemStack(MESMod.ICON_HELMET.get()))
                    .displayItems((params, output) -> {
                        output.accept(new ItemStack(MESMod.ICON_HELMET.get())); // 占位
                        fillEquipmentByType("helmet", output);
                    })
                    .build());

    // 头盔配件
    public static final RegistryObject<CreativeModeTab> HELMET_ATTACHMENT_TAB = TABS.register("helmet_attachments",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.modernequipment.helmet_attachments"))
                    .icon(() -> new ItemStack(MESMod.ICON_HELMET_ATTACHMENT.get()))
                    .displayItems((params, output) -> {
                        output.accept(new ItemStack(MESMod.ICON_HELMET_ATTACHMENT.get()));
                        fillAttachmentByType("helmet_attachment", output);
                    })
                    .build());

    // 面部装备
    public static final RegistryObject<CreativeModeTab> FACE_EQUIPMENT_TAB = TABS.register("face_equipment",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.modernequipment.face_equipment"))
                    .icon(() -> new ItemStack(MESMod.ICON_FACE_EQUIPMENT.get()))
                    .displayItems((params, output) -> {
                        output.accept(new ItemStack(MESMod.ICON_FACE_EQUIPMENT.get()));
                        fillEquipmentByType("face_equipment", output);
                    })
                    .build());

    // 防弹衣
    public static final RegistryObject<CreativeModeTab> BODY_ARMOR_TAB = TABS.register("body_armor",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.modernequipment.body_armor"))
                    .icon(() -> new ItemStack(MESMod.ICON_BODY_ARMOR.get()))
                    .displayItems((params, output) -> {
                        output.accept(new ItemStack(MESMod.ICON_BODY_ARMOR.get()));
                        fillEquipmentByType("body_armor", output);
                    })
                    .build());

    // 胸挂
    public static final RegistryObject<CreativeModeTab> CHEST_RIG_TAB = TABS.register("chest_rigs",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.modernequipment.chest_rigs"))
                    .icon(() -> new ItemStack(MESMod.ICON_CHEST_RIG.get()))
                    .displayItems((params, output) -> {
                        output.accept(new ItemStack(MESMod.ICON_CHEST_RIG.get()));
                        fillEquipmentByType("chest_rig", output);
                    })
                    .build());

    // 插板
    public static final RegistryObject<CreativeModeTab> ARMOR_PLATE_TAB = TABS.register("armor_plates",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.modernequipment.armor_plates"))
                    .icon(() -> new ItemStack(MESMod.ICON_ARMOR_PLATE.get()))
                    .displayItems((params, output) -> {
                        output.accept(new ItemStack(MESMod.ICON_ARMOR_PLATE.get()));
                        fillAttachmentByType("armor_plate", output);
                    })
                    .build());

    // 四肢装备
    public static final RegistryObject<CreativeModeTab> LIMB_ARMOR_TAB = TABS.register("limb_armor",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.modernequipment.limb_armor"))
                    .icon(() -> new ItemStack(MESMod.ICON_LIMB_ARMOR.get()))
                    .displayItems((params, output) -> {
                        output.accept(new ItemStack(MESMod.ICON_LIMB_ARMOR.get()));
                        fillEquipmentByType("limb_armor", output);
                    })
                    .build());

    // 背包
    public static final RegistryObject<CreativeModeTab> BACKPACK_TAB = TABS.register("backpacks",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.modernequipment.backpacks"))
                    .icon(() -> new ItemStack(MESMod.ICON_BACKPACK.get()))
                    .displayItems((params, output) -> {
                        output.accept(new ItemStack(MESMod.ICON_BACKPACK.get()));
                        fillEquipmentByType("backpack", output);
                    })
                    .build());

    // 战术腰封
    public static final RegistryObject<CreativeModeTab> TACTICAL_BELT_TAB = TABS.register("tactical_belts",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.modernequipment.tactical_belts"))
                    .icon(() -> new ItemStack(MESMod.ICON_TACTICAL_BELT.get()))
                    .displayItems((params, output) -> {
                        output.accept(new ItemStack(MESMod.ICON_TACTICAL_BELT.get()));
                        fillEquipmentByType("tactical_belt", output);
                    })
                    .build());

    // 战术耳机
    public static final RegistryObject<CreativeModeTab> TACTICAL_HEADSET_TAB = TABS.register("tactical_headsets",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.modernequipment.tactical_headsets"))
                    .icon(() -> new ItemStack(MESMod.ICON_TACTICAL_HEADSET.get()))
                    .displayItems((params, output) -> {
                        output.accept(new ItemStack(MESMod.ICON_TACTICAL_HEADSET.get()));
                        fillEquipmentByType("tactical_headset", output);
                    })
                    .build());
    // 安全箱
    public static final RegistryObject<CreativeModeTab> SAFE_BOX_TAB = TABS.register("safe_boxes",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.modernequipment.safe_boxes"))
                    .icon(() -> new ItemStack(MESMod.ICON_SAFE_BOX.get()))
                    .displayItems((params, output) -> {
                        output.accept(new ItemStack(MESMod.ICON_SAFE_BOX.get()));
                        fillEquipmentByType("safe_box", output);
                    })
                    .build());

    private static void fillEquipmentByType(String type, CreativeModeTab.Output output) {
        if (EquipmentDataManager.getAllEquipment().isEmpty() && EquipmentDataManager.getAllAttachments().isEmpty()) {
            EquipmentPackLoader.loadPacks();
        }
        for (EquipmentData data : EquipmentDataManager.getAllEquipment()) {
            if (type.equals(data.getType())) {
                Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(MESMod.MODID, data.getId()));
                if (item != null) output.accept(item.getDefaultInstance());
            }
        }
    }

    private static void fillAttachmentByType(String type, CreativeModeTab.Output output) {
        if (EquipmentDataManager.getAllEquipment().isEmpty() && EquipmentDataManager.getAllAttachments().isEmpty()) {
            EquipmentPackLoader.loadPacks();
        }
        for (AttachmentData data : EquipmentDataManager.getAllAttachments()) {
            if (type.equals(data.getType())) {
                Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(MESMod.MODID, data.getId()));
                if (item != null) output.accept(item.getDefaultInstance());
            }
        }
    }
}