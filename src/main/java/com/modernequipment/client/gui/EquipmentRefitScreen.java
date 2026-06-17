package com.modernequipment.client.gui;

import com.modernequipment.MESMod;
import com.modernequipment.api.attachment.AttachmentType;
import com.modernequipment.api.equipment.IModifiableEquipment;
import com.modernequipment.client.gui.components.refit.*;
import com.modernequipment.core.data.AttachmentData;
import com.modernequipment.core.item.EquipmentArmorItem;
import com.modernequipment.core.item.EquipmentItem;
import com.modernequipment.core.loader.EquipmentDataManager;
import com.modernequipment.network.C2SInstallAttachmentPacket;
import com.modernequipment.network.C2SUninstallAttachmentPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class EquipmentRefitScreen extends Screen {
    public static final ResourceLocation SLOT_TEXTURE = new ResourceLocation(MESMod.MODID, "textures/gui/refit_slot.png");
    public static final ResourceLocation TURN_PAGE_TEXTURE = new ResourceLocation(MESMod.MODID, "textures/gui/refit_turn_page.png");
    public static final ResourceLocation UNLOAD_TEXTURE = new ResourceLocation(MESMod.MODID, "textures/gui/refit_unload.png");
    public static final ResourceLocation ICONS_TEXTURE = new ResourceLocation(MESMod.MODID, "textures/gui/refit_slot_icons.png");

    public static final int SLOT_SIZE = 18;
    public static final int ICON_UV_SIZE = 32;
    private static final int INVENTORY_ATTACHMENT_SLOT_COUNT = 8;

    private final Inventory playerInventory;
    private final int equipmentSlotIndex;
    private final String equipmentType;

    private AttachmentType selectedSlot = AttachmentType.NONE;
    private int currentPage = 0;
    private List<AttachmentType> allowedSlots = new ArrayList<>();
    private boolean hidePropertyDiagrams = false;

    public EquipmentRefitScreen() {
        super(Component.literal("Equipment Refit"));
        this.playerInventory = Minecraft.getInstance().player.getInventory();
        this.equipmentSlotIndex = Minecraft.getInstance().player.getInventory().selected;

        ItemStack mainHand = Minecraft.getInstance().player.getMainHandItem();
        String type = "unknown";
        if (mainHand.getItem() instanceof EquipmentItem eqItem) {
            type = eqItem.getData().getType();
        } else if (mainHand.getItem() instanceof EquipmentArmorItem armorItem) {
            type = armorItem.getData().getType();
        }
        this.equipmentType = type;
    }

    @Override
    protected void init() {
        super.init();
        ItemStack equipmentStack = Minecraft.getInstance().player.getMainHandItem();
        IModifiableEquipment modifiable = IModifiableEquipment.getModifiableOrNull(equipmentStack);
        if (modifiable != null) {
            allowedSlots = List.of(modifiable.getAllowedAttachmentTypes(equipmentStack));
        } else {
            allowedSlots = new ArrayList<>();
        }

        addToggleStatsButton();
        addAttachmentTypeButtons();
        addInventoryAttachmentButtons();
    }

    private void addToggleStatsButton() {
        Component buttonText = hidePropertyDiagrams ?
                Component.translatable("gui.modernequipment.refit.show") :
                Component.translatable("gui.modernequipment.refit.hide");
        this.addRenderableWidget(new FlatColorButton(11, 11, 288, 16, buttonText, b -> {
            hidePropertyDiagrams = !hidePropertyDiagrams;
            clearWidgets();
            init();
        }));
    }

    private void addAttachmentTypeButtons() {
        ItemStack equipmentStack = Minecraft.getInstance().player.getMainHandItem();
        IModifiableEquipment modifiable = IModifiableEquipment.getModifiableOrNull(equipmentStack);
        if (modifiable == null) return;

        List<AttachmentType> helmetSlots = Arrays.asList(
                AttachmentType.HELMET_TOP,
                AttachmentType.NVG_MOUNT,
                AttachmentType.FACE_SHIELD
        );
        List<AttachmentType> bodyArmorSlots = Arrays.asList(
                AttachmentType.FRONT_PLATE,
                AttachmentType.BACK_PLATE,
                AttachmentType.SIDE_PLATE,
                AttachmentType.GROIN_PLATE,
                AttachmentType.NECK_ARMOR
        );

        List<AttachmentType> slotsToShow;
        if ("helmet".equals(equipmentType)) {
            slotsToShow = helmetSlots;
        } else if ("body_armor".equals(equipmentType)) {
            slotsToShow = bodyArmorSlots;
        } else {
            return;
        }

        int maxSlots = slotsToShow.size();
        int startX = this.width - 30 - (maxSlots - 1) * SLOT_SIZE;
        int startY = 10;
        for (int i = 0; i < maxSlots; i++) {
            AttachmentType type = slotsToShow.get(i);
            int x = startX + i * SLOT_SIZE;

            boolean allow;
            if (type == AttachmentType.FRONT_PLATE || type == AttachmentType.BACK_PLATE) {
                allow = allowedSlots.contains(AttachmentType.ARMOR_PLATE);
            } else {
                allow = allowedSlots.contains(type);
            }

            EquipmentAttachmentSlot button = new EquipmentAttachmentSlot(x, startY, type, equipmentSlotIndex,
                    playerInventory, equipmentType, allow, this::onSlotSelected);
            addRenderableWidget(button);
            if (selectedSlot == type) {
                button.setSelected(true);
                if (allow && modifiable.getAttachments(equipmentStack).containsKey(type)) {
                    RefitUnloadButton unloadButton = new RefitUnloadButton(x + 5, startY + SLOT_SIZE + 2, b -> {
                        MESMod.CHANNEL.sendToServer(new C2SUninstallAttachmentPacket(equipmentSlotIndex, type));
                    });
                    addRenderableWidget(unloadButton);
                }
            }
        }
    }

    private void onSlotSelected(Button button) {
        EquipmentAttachmentSlot slotButton = (EquipmentAttachmentSlot) button;
        AttachmentType type = slotButton.getType();
        if (selectedSlot == type) {
            selectedSlot = AttachmentType.NONE;
            currentPage = 0;
        } else {
            selectedSlot = type;
            currentPage = 0;
        }
        clearWidgets();
        init();
    }

    private void addInventoryAttachmentButtons() {
        if (selectedSlot == AttachmentType.NONE) return;

        ItemStack equipmentStack = Minecraft.getInstance().player.getMainHandItem();
        IModifiableEquipment modifiable = IModifiableEquipment.getModifiableOrNull(equipmentStack);
        if (modifiable == null) return;

        int startX = this.width - 30;
        int startY = 50;
        int pageStart = currentPage * INVENTORY_ATTACHMENT_SLOT_COUNT;
        int count = 0;
        int currentY = startY;

        List<Integer> compatibleSlots = new ArrayList<>();
        for (int i = 0; i < playerInventory.getContainerSize(); i++) {
            ItemStack stack = playerInventory.getItem(i);
            if (modifiable.allowAttachment(equipmentStack, stack)) {
                ResourceLocation attId = ForgeRegistries.ITEMS.getKey(stack.getItem());
                if (attId != null) {
                    AttachmentData attData = EquipmentDataManager.getAttachment(attId);
                    if (attData != null) {
                        String attType = attData.getType();
                        boolean typeMatches = selectedSlot.name().equalsIgnoreCase(attType);
                        if (!typeMatches && (selectedSlot == AttachmentType.FRONT_PLATE || selectedSlot == AttachmentType.BACK_PLATE)
                                && "armor_plate".equalsIgnoreCase(attType)) {
                            typeMatches = true;
                        }
                        if (typeMatches) {
                            compatibleSlots.add(i);
                        }
                    }
                }
            }
        }

        for (int idx = 0; idx < compatibleSlots.size(); idx++) {
            if (idx < pageStart) continue;
            if (count >= INVENTORY_ATTACHMENT_SLOT_COUNT) break;
            int slotIndex = compatibleSlots.get(idx);
            InventoryAttachmentSlot button = new InventoryAttachmentSlot(startX, currentY, slotIndex, playerInventory, b -> {
                MESMod.CHANNEL.sendToServer(new C2SInstallAttachmentPacket(equipmentSlotIndex, slotIndex, selectedSlot));
            });
            addRenderableWidget(button);
            currentY += SLOT_SIZE;
            count++;
        }

        int totalPage = (compatibleSlots.size() - 1) / INVENTORY_ATTACHMENT_SLOT_COUNT;
        if (currentPage > 0) {
            RefitTurnPageButton turnPageUp = new RefitTurnPageButton(startX, startY - 10, true, b -> {
                currentPage--;
                clearWidgets();
                init();
            });
            addRenderableWidget(turnPageUp);
        }
        if (currentPage < totalPage) {
            RefitTurnPageButton turnPageDown = new RefitTurnPageButton(startX, startY + SLOT_SIZE * INVENTORY_ATTACHMENT_SLOT_COUNT + 2, false, b -> {
                currentPage++;
                clearWidgets();
                init();
            });
            addRenderableWidget(turnPageDown);
        }
    }

    @Override
    public void render(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);

        if (!hidePropertyDiagrams) {
            ItemStack equipmentStack = Minecraft.getInstance().player.getMainHandItem();
            IModifiableEquipment modifiable = IModifiableEquipment.getModifiableOrNull(equipmentStack);
            if (modifiable != null) {
                EquipmentPropertyDiagrams.draw(graphics, font, 11, 11 + 16 + 2, equipmentStack, modifiable, equipmentType);
            }
        }

        for (var widget : this.renderables) {
            if (widget instanceof AbstractWidget w) {
                if (w instanceof IStackTooltip stackTooltip && w.isHoveredOrFocused()) {
                    stackTooltip.renderTooltip(stack -> graphics.renderTooltip(font, stack, mouseX, mouseY));
                }
                if (w instanceof IComponentTooltip compTooltip && w.isHoveredOrFocused()) {
                    compTooltip.renderTooltip(components -> graphics.renderComponentTooltip(font, components, mouseX, mouseY));
                }
            }
        }
    }

    public void refresh() {
        ItemStack equipmentStack = Minecraft.getInstance().player.getMainHandItem();
        IModifiableEquipment modifiable = IModifiableEquipment.getModifiableOrNull(equipmentStack);
        if (modifiable != null) {
            allowedSlots = List.of(modifiable.getAllowedAttachmentTypes(equipmentStack));
        } else {
            allowedSlots = new ArrayList<>();
        }
        clearWidgets();
        addToggleStatsButton();
        addAttachmentTypeButtons();
        addInventoryAttachmentButtons();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}