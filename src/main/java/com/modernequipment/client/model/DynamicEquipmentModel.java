package com.modernequipment.client.model;

import com.modernequipment.MESMod;
import com.modernequipment.core.item.EquipmentArmorItem;
import com.modernequipment.util.GeoModelPatcher;
import com.modernequipment.util.ResourceValidator;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.model.GeoModel;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DynamicEquipmentModel extends GeoModel<EquipmentArmorItem> {

    private static final Set<ResourceLocation> BODY_ARMOR_MODELS = ConcurrentHashMap.newKeySet();

    @Override
    public ResourceLocation getModelResource(EquipmentArmorItem animatable) {
        ResourceLocation location;
        var render = animatable.getData().getRender();
        if (render != null && render.getGeo() != null) {
            String geoPath = render.getGeo();
            if (!geoPath.contains(":")) {
                geoPath = "modernequipment:" + geoPath;
            }

            // 检查 Geo 模型资源是否存在
            if (!ResourceValidator.geoExists(geoPath)) {
                MESMod.LOGGER.debug("DynamicEquipmentModel.getModelResource - Geo not found, using default: {}", geoPath);
                location = ResourceValidator.getDefaultGeo();
            } else {
                location = new ResourceLocation(geoPath);
            }
        } else {
            location = new ResourceLocation("modernequipment", "geo/armor/default.geo.json");
        }

        // 标记 body_armor 类型的模型，后续在 getBakedModel 中打补丁
        if ("body_armor".equals(animatable.getData().getType())) {
            BODY_ARMOR_MODELS.add(location);
            MESMod.LOGGER.debug("DynamicEquipmentModel - Mark {} for arm bone patch (body_armor)", location);
        }

        return location;
    }

    @Override
    public ResourceLocation getTextureResource(EquipmentArmorItem animatable) {
        var render = animatable.getData().getRender();
        if (render != null && render.getTexture() != null) {
            String texPath = render.getTexture();
            if (!texPath.contains(":")) {
                texPath = "modernequipment:" + texPath;
            }

            // 检查贴图资源是否存在
            if (!ResourceValidator.textureExists(texPath)) {
                MESMod.LOGGER.debug("DynamicEquipmentModel.getTextureResource - Texture not found, using default: {}", texPath);
                return ResourceValidator.getDefaultTexture();
            }

            return new ResourceLocation(texPath);
        }
        return new ResourceLocation("modernequipment", "textures/armor/default.png");
    }

    @Override
    public ResourceLocation getAnimationResource(EquipmentArmorItem animatable) {
        var render = animatable.getData().getRender();
        if (render != null && render.getAnimation() != null) {
            String animPath = render.getAnimation();
            if (!animPath.contains(":")) {
                animPath = "modernequipment:" + animPath;
            }

            // 检查动画资源是否存在，如果不存在则返回 null（不使用动画）
            if (!ResourceValidator.animationExists(animPath)) {
                MESMod.LOGGER.debug("DynamicEquipmentModel.getAnimationResource - Animation not found, skipping: {}", animPath);
                return null;
            }

            return new ResourceLocation(animPath);
        }
        return null;
    }

    @Override
    public BakedGeoModel getBakedModel(ResourceLocation location) {
        BakedGeoModel model = super.getBakedModel(location);
        // 仅对 body_armor 类型的模型添加手臂骨骼补丁
        if (BODY_ARMOR_MODELS.contains(location)) {
            GeoModelPatcher.patchModel(location, model);
        }
        return model;
    }
}