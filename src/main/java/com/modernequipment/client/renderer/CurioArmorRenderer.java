package com.modernequipment.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.modernequipment.MESMod;
import com.modernequipment.core.item.EquipmentItem;
import com.modernequipment.util.ResourceValidator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

@OnlyIn(Dist.CLIENT)
public class CurioArmorRenderer implements ICurioRenderer {

    @Override
    public <T extends LivingEntity, M extends EntityModel<T>> void render(
            ItemStack stack,
            SlotContext slotContext,
            PoseStack poseStack,
            RenderLayerParent<T, M> renderLayerParent,
            MultiBufferSource bufferSource,
            int light,
            float limbSwing,
            float limbSwingAmount,
            float partialTick,
            float ageInTicks,
            float netHeadYaw,
            float headPitch) {

        // ==================== 日志1：渲染入口 ====================
        MESMod.LOGGER.info("=== CurioArmorRenderer.render called ===");
        MESMod.LOGGER.info("Item: {}", stack.getItem());
        MESMod.LOGGER.info("Slot: {}", slotContext.identifier());
        MESMod.LOGGER.info("Entity: {}", slotContext.entity());

        if (!(stack.getItem() instanceof EquipmentItem equipmentItem)) {
            MESMod.LOGGER.warn("Item is not an EquipmentItem, skipping render");
            return;
        }
        if (!(slotContext.entity() instanceof Player player)) {
            MESMod.LOGGER.warn("Entity is not a Player, skipping render");
            return;
        }

        // ==================== 日志2：获取模型路径 ====================
        String modelPath = equipmentItem.getData().getRender().getModel();
        MESMod.LOGGER.info("Model path from render.getModel(): {}", modelPath);

        if (modelPath == null || modelPath.isEmpty()) {
            MESMod.LOGGER.warn("Model path is null or empty, cannot render");
            return;
        }

        // ==================== 检查贴图是否存在 ====================
        String texturePath = equipmentItem.getData().getRender().getTexture();
        if (texturePath != null && !texturePath.isEmpty()) {
            if (!ResourceValidator.textureExists(texturePath)) {
                MESMod.LOGGER.warn("Texture not found: {}, skipping render", texturePath);
                return;
            }
        }

        poseStack.pushPose();

        // ==================== 调试位置变换（临时启用可快速定位模型） ====================
        // 如果正式渲染看不到模型，可以取消下面两行注释，将模型放在玩家前方2格，方便观察
        // poseStack.translate(0, 1.5, 2);
        // poseStack.scale(2, 2, 2);
        // 正式使用时启用以下行
        applyTransform(slotContext.identifier(), poseStack, player, netHeadYaw, headPitch);

        // ==================== 日志3：加载模型 ====================
        ResourceLocation modelRl = new ResourceLocation(modelPath);
        ModelResourceLocation modelLocation = new ModelResourceLocation(modelRl, "inventory");
        MESMod.LOGGER.info("Attempting to load model: {}", modelLocation);

        BakedModel wearModel = Minecraft.getInstance().getModelManager().getModel(modelLocation);
        if (wearModel == null) {
            MESMod.LOGGER.error("BakedModel is null for {}", modelLocation);
            poseStack.popPose();
            return;
        }

        BakedModel missingModel = Minecraft.getInstance().getModelManager().getMissingModel();
        if (wearModel == missingModel) {
            MESMod.LOGGER.error("Model {} is missing (loaded as missing model). Check file path and JSON syntax.", modelLocation);
            poseStack.popPose();
            return;
        }

        MESMod.LOGGER.info("Model loaded successfully: {}", modelLocation);

        // ==================== 日志4：开始渲染 ====================
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        MESMod.LOGGER.info("Rendering model with ItemRenderer, light={}", light);
        itemRenderer.render(stack, ItemDisplayContext.FIXED, false, poseStack, bufferSource, light, OverlayTexture.NO_OVERLAY, wearModel);

        MESMod.LOGGER.info("Render call completed");

        poseStack.popPose();
    }

    private void applyTransform(String slotId, PoseStack poseStack, Player player,
                                float netHeadYaw, float headPitch) {
        if (slotId.equals("mes_face") || slotId.equals("mes_tactical_headset")) {
            // 头部装备：跟随头部旋转
            poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(netHeadYaw));
            poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(headPitch));
            // 调整到头部位置（Y 值约为眼睛高度 - 0.35，Z 向前突出）
            poseStack.translate(0, player.getEyeHeight() - 0.35, 0.15);
            poseStack.scale(0.8f, 0.8f, 0.8f);
        } else {
            // 身体装备（胸挂、背包、腰封、护臂、安全箱）：放在胸部位置
            poseStack.translate(0, 0.85, 0.2);
            poseStack.scale(0.85f, 0.85f, 0.85f);
        }
    }
}