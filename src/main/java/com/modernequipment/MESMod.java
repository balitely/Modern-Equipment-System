package com.modernequipment;

import com.moderndamage.control.api.ProtectionSourceProviderRegistry;
import com.modernequipment.compat.EZWeightCompatibility;
import com.modernequipment.compat.MDCConfigWriter;
import com.modernequipment.compat.MESProtectionSourceProvider;
import com.modernequipment.core.data.AttachmentData;
import com.modernequipment.core.data.EquipmentData;
import com.modernequipment.core.item.AttachmentItem;
import com.modernequipment.core.item.EquipmentArmorItem;
import com.modernequipment.core.item.EquipmentItem;
import com.modernequipment.core.loader.EquipmentDataManager;
import com.modernequipment.core.loader.EquipmentPackLoader;
import com.modernequipment.config.MESConfig;
import com.modernequipment.event.ArmorHitListener;
import com.modernequipment.event.AttachmentTooltipHandler;
import com.modernequipment.event.EquipmentTooltipHandler;
import com.modernequipment.gui.EquipmentContainerMenu;
import com.modernequipment.init.CuriosSlotRegistry;
import com.modernequipment.init.ModCreativeTabs;
import com.modernequipment.init.ModMenus;
import com.modernequipment.network.C2SInstallAttachmentPacket;
import com.modernequipment.network.C2SUninstallAttachmentPacket;
import com.modernequipment.network.S2CUpdateEquipmentPacket;
import com.modernequipment.network.S2CUpdateInventorySlotPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraftforge.network.NetworkEvent;

@Mod(MESMod.MODID)
@Mod.EventBusSubscriber(modid = MESMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class MESMod {
    public static final String MODID = "modernequipment";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    private static final DeferredRegister<Item> ICON_ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);

    public static final RegistryObject<Item> ICON_HELMET = ICON_ITEMS.register("icon_helmet", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> ICON_HELMET_ATTACHMENT = ICON_ITEMS.register("icon_helmet_attachment", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> ICON_FACE_EQUIPMENT = ICON_ITEMS.register("icon_face_equipment", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> ICON_BODY_ARMOR = ICON_ITEMS.register("icon_body_armor", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> ICON_CHEST_RIG = ICON_ITEMS.register("icon_chest_rig", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> ICON_ARMOR_PLATE = ICON_ITEMS.register("icon_armor_plate", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> ICON_LIMB_ARMOR = ICON_ITEMS.register("icon_limb_armor", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> ICON_BACKPACK = ICON_ITEMS.register("icon_backpack", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> ICON_TACTICAL_BELT = ICON_ITEMS.register("icon_tactical_belt", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> ICON_TACTICAL_HEADSET = ICON_ITEMS.register("icon_tactical_headset", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> ICON_SAFE_BOX = ICON_ITEMS.register("icon_safe_box", () -> new Item(new Item.Properties()));

    // 网络通道（主通道，用于配件改装）
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(MODID, "refit"),
            () -> "1.0",
            s -> true,
            s -> true
    );

    private static int packetId = 0;

    public MESMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // 注册客户端配置
        net.minecraftforge.fml.ModLoadingContext.get().registerConfig(
                net.minecraftforge.fml.config.ModConfig.Type.CLIENT, MESConfig.CLIENT_CONFIG);

        ICON_ITEMS.register(modEventBus);
        ModCreativeTabs.TABS.register(modEventBus);
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerItems);
        LOGGER.info("MESMod initialized – icon items and creative tabs registered");
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(this::onRightClick);
        ModMenus.MENUS.register(modEventBus);
        registerPackets();
        MinecraftForge.EVENT_BUS.register(new ArmorHitListener());
        MinecraftForge.EVENT_BUS.register(new AttachmentTooltipHandler());
        MinecraftForge.EVENT_BUS.register(new EquipmentTooltipHandler());
    }

    private void registerPackets() {
        registerPacket(C2SInstallAttachmentPacket.class,
                C2SInstallAttachmentPacket::encode,
                C2SInstallAttachmentPacket::decode,
                C2SInstallAttachmentPacket::handle);
        registerPacket(C2SUninstallAttachmentPacket.class,
                C2SUninstallAttachmentPacket::encode,
                C2SUninstallAttachmentPacket::decode,
                C2SUninstallAttachmentPacket::handle);
        registerPacket(S2CUpdateEquipmentPacket.class,
                S2CUpdateEquipmentPacket::encode,
                S2CUpdateEquipmentPacket::decode,
                S2CUpdateEquipmentPacket::handle);
        registerPacket(S2CUpdateInventorySlotPacket.class,
                S2CUpdateInventorySlotPacket::encode,
                S2CUpdateInventorySlotPacket::decode,
                S2CUpdateInventorySlotPacket::handle);
    }

    public static <MSG> void registerPacket(Class<MSG> clazz,
                                            BiConsumer<MSG, FriendlyByteBuf> encoder,
                                            Function<FriendlyByteBuf, MSG> decoder,
                                            BiConsumer<MSG, Supplier<NetworkEvent.Context>> handler) {
        CHANNEL.registerMessage(packetId++, clazz, encoder, decoder, handler);
    }

    private void registerProtectionProvider() {
        try {
            ProtectionSourceProviderRegistry.register(new MESProtectionSourceProvider());
            LOGGER.info("Successfully registered MES ProtectionSourceProvider to MDC");
        } catch (Throwable e) {
            LOGGER.error("Failed to register MES ProtectionSourceProvider. Is MDC loaded?", e);
        }
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            EquipmentPackLoader.loadPacks();
            LOGGER.info("Loaded {} equipment, {} attachments",
                    EquipmentDataManager.getAllEquipment().size(),
                    EquipmentDataManager.getAllAttachments().size());
            EZWeightCompatibility.syncWeights();
            MDCConfigWriter.updateAndReload();

            LOGGER.info("Attempting to register MES ProtectionSourceProvider...");
            registerProtectionProvider();
            LOGGER.info("MES ProtectionSourceProvider registration process completed.");
        });
    }

    private void registerItems(final RegisterEvent event) {
        if (event.getRegistryKey().equals(ForgeRegistries.Keys.ITEMS)) {
            if (EquipmentDataManager.getAllEquipment().isEmpty() && EquipmentDataManager.getAllAttachments().isEmpty()) {
                EquipmentPackLoader.loadPacks();
            }
            event.register(ForgeRegistries.Keys.ITEMS, helper -> {
                for (EquipmentData data : EquipmentDataManager.getAllEquipment()) {
                    ResourceLocation id = new ResourceLocation(MODID, data.getId());
                    Item item;
                    String type = data.getType();
                    String subType = data.getSubType();

                    // 判断是否应注册为原版盔甲
                    if ("helmet".equals(type)) {
                        item = new EquipmentArmorItem(ArmorItem.Type.HELMET,
                                new Item.Properties().stacksTo(data.getMaxStackSize()), data);
                    } else if ("body_armor".equals(type)) {
                        item = new EquipmentArmorItem(ArmorItem.Type.CHESTPLATE,
                                new Item.Properties().stacksTo(data.getMaxStackSize()), data);
                    } else if ("limb_armor".equals(type)) {
                        if ("legs".equals(subType)) {
                            item = new EquipmentArmorItem(ArmorItem.Type.LEGGINGS,
                                    new Item.Properties().stacksTo(data.getMaxStackSize()), data);
                        } else if ("feet".equals(subType)) {
                            item = new EquipmentArmorItem(ArmorItem.Type.BOOTS,
                                    new Item.Properties().stacksTo(data.getMaxStackSize()), data);
                        } else {
                            item = new EquipmentItem(new Item.Properties().stacksTo(data.getMaxStackSize()), data);
                        }
                    } else {
                        item = new EquipmentItem(new Item.Properties().stacksTo(data.getMaxStackSize()), data);
                    }

                    helper.register(id, item);
                    LOGGER.info("Registered equipment: {} as {}", id, item.getClass().getSimpleName());
                }
                // 附件注册保持不变
                for (AttachmentData data : EquipmentDataManager.getAllAttachments()) {
                    ResourceLocation id = new ResourceLocation(MODID, data.getId());
                    Item item = new AttachmentItem(new Item.Properties().stacksTo(data.getMaxStackSize()), data);
                    helper.register(id, item);
                    LOGGER.info("Registered attachment: {}", id);
                }
            });
        }
    }

    @SubscribeEvent
    public static void onInterModEnqueue(InterModEnqueueEvent event) {
        CuriosSlotRegistry.registerSlots();
        LOGGER.info("Registered MES Curios slots via IMC");
    }

    @SubscribeEvent
    public void onRightClick(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        ItemStack stack = event.getItemStack();
        if (stack.getItem() instanceof EquipmentItem eq && eq.getData().hasInventory()) {
            if (!player.level().isClientSide) {
                NetworkHooks.openScreen((ServerPlayer) player, new SimpleMenuProvider((id, inv, p) ->
                        new EquipmentContainerMenu(id, inv, stack), stack.getHoverName()), buf -> buf.writeItem(stack));
            }
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
        }
    }
}