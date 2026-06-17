package com.modernequipment.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.modernequipment.MESMod;
import com.modernequipment.client.model.DynamicCurioModel;
import com.modernequipment.core.item.EquipmentItem;
import com.modernequipment.util.MESDebugLogger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.cache.texture.AnimatableTexture;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

/**
 * Curios饰品Geo模型渲染器
 * 直接烘焙和渲染Geo模型
 */
@OnlyIn(Dist.CLIENT)
public class GeoCurioRenderer implements ICurioRenderer, GeoRenderer<EquipmentItem> {

    protected final DynamicCurioModel model;
    protected HumanoidModel<LivingEntity> headModel;

    protected EquipmentItem animatable;
    protected ItemStack currentStack;
    protected LivingEntity currentEntity;

    public GeoCurioRenderer() {
        this.model = new DynamicCurioModel();
        this.headModel = null;
    }

    /**
     * 延迟初始化headModel，避免构造函数中抛出异常
     */
    private HumanoidModel<LivingEntity> getHeadModel() {
        if (headModel == null) {
            try {
                this.headModel = new HumanoidModel<>(
                    Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.PLAYER)
                );
            } catch (Exception e) {
                MESDebugLogger.error(MESMod.LOGGER, "GeoCurioRenderer - Failed to create headModel", e);
                return null;
            }
        }
        return headModel;
    }

    @Override
    public GeoModel<EquipmentItem> getGeoModel() {
        return this.model;
    }

    @Override
    public EquipmentItem getAnimatable() {
        return this.animatable;
    }

    @Override
    public long getInstanceId(EquipmentItem animatable) {
        return animatable.hashCode();
    }

    @Override
    public void fireCompileRenderLayersEvent() {}

    @Override
    public boolean firePreRenderEvent(PoseStack poseStack, BakedGeoModel model, MultiBufferSource bufferSource, float partialTick, int packedLight) {
        return true;
    }

    @Override
    public void firePostRenderEvent(PoseStack poseStack, BakedGeoModel model, MultiBufferSource bufferSource, float partialTick, int packedLight) {}

    @Override
    public void updateAnimatedTextureFrame(EquipmentItem animatable) {
        if (currentEntity != null)
            AnimatableTexture.setAndUpdate(getTextureLocation(animatable));
    }

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

        if (!(stack.getItem() instanceof EquipmentItem equipmentItem)) {
            return;
        }

        // 没有 render 数据则不渲染
        if (equipmentItem.getData().getRender() == null) {
            MESDebugLogger.info(MESMod.LOGGER, "GeoCurioRenderer - no render data, skipping: {}", stack.getItem());
            return;
        }

        this.animatable = equipmentItem;
        this.currentStack = stack;
        this.currentEntity = slotContext.entity();

        MESDebugLogger.info(MESMod.LOGGER, "GeoCurioRenderer.render - item: {}, slot: {}", stack.getItem(), slotContext.identifier());

        // 同步姿态
        try {
            HumanoidModel<LivingEntity> hModel = getHeadModel();
            if (hModel != null) {
                ICurioRenderer.followBodyRotations(slotContext.entity(), hModel);
            }
        } catch (Exception e) {
            MESDebugLogger.error(MESMod.LOGGER, "GeoCurioRenderer - followBodyRotations failed", e);
        }

        // 获取模型路径
        ResourceLocation modelPath = model.getModelResource(equipmentItem);
        ResourceLocation texPath = model.getTextureResource(equipmentItem);
        MESDebugLogger.info(MESMod.LOGGER, "GeoCurioRenderer - model: {}, texture: {}", modelPath, texPath);

        // 烘焙模型
        BakedGeoModel bakedModel;
        try {
            bakedModel = model.getBakedModel(modelPath);
            MESDebugLogger.info(MESMod.LOGGER, "GeoCurioRenderer - bakedModel: {}, topLevelBones: {}",
                bakedModel, bakedModel != null ? bakedModel.topLevelBones().size() : 0);
        } catch (Exception e) {
            MESDebugLogger.error(MESMod.LOGGER, "GeoCurioRenderer - Failed to bake model: {}", modelPath, e);
            return;
        }

        if (bakedModel == null || bakedModel.topLevelBones().isEmpty()) {
            MESDebugLogger.error(MESMod.LOGGER, "GeoCurioRenderer - bakedModel is null or empty");
            return;
        }

        poseStack.pushPose();

        // 根据槽位定位到正确的身体部位，使模型跟随身体旋转
        String slotId = slotContext.identifier();
        HumanoidModel<LivingEntity> hModel = getHeadModel();
        if (hModel != null) {
            switch (slotId) {
                case "mes_tactical_headset":
                case "mes_face":
                    hModel.head.translateAndRotate(poseStack);
                    break;
                case "mes_chest_rig":
                case "mes_backpack":
                case "mes_tactical_belt":
                case "mes_safe_box":
                    hModel.body.translateAndRotate(poseStack);
                    break;
                case "mes_arm_armor":
                    hModel.rightArm.translateAndRotate(poseStack);
                    break;
                default:
                    hModel.body.translateAndRotate(poseStack);
                    break;
            }
        }
        // Geo模型标准变换
        poseStack.translate(0, 1.5f, 0);
        poseStack.scale(-1.0f, -1.0f, 1.0f);

        // 渲染所有骨骼
        RenderType renderType = RenderType.armorCutoutNoCull(texPath);
        VertexConsumer buffer = ItemRenderer.getArmorFoilBuffer(bufferSource, renderType, false, stack.hasFoil());

        for (GeoBone bone : bakedModel.topLevelBones()) {
            MESDebugLogger.info(MESMod.LOGGER, "GeoCurioRenderer - rendering bone: {}", bone.getName());
            renderRecursively(poseStack, equipmentItem, bone, renderType, bufferSource, buffer, false, partialTick, light, OverlayTexture.NO_OVERLAY, 1.0f, 1.0f, 1.0f, 1.0f);
        }

        poseStack.popPose();
    }
}