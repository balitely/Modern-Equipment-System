package com.modernequipment.client.model;

import com.modernequipment.core.item.EquipmentArmorItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class DynamicEquipmentModel extends GeoModel<EquipmentArmorItem> {
    @Override
    public ResourceLocation getModelResource(EquipmentArmorItem animatable) {
        var render = animatable.getData().getRender();
        if (render != null && render.getGeo() != null) {
            return new ResourceLocation(render.getGeo());
        }
        return new ResourceLocation("modernequipment", "geo/armor/default.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(EquipmentArmorItem animatable) {
        var render = animatable.getData().getRender();
        if (render != null && render.getTexture() != null) {
            String texPath = render.getTexture();
            if (!texPath.contains(":")) {
                texPath = "modernequipment:" + texPath;
            }
            return new ResourceLocation(texPath);
        }
        return new ResourceLocation("modernequipment", "textures/armor/default.png");
    }

    @Override
    public ResourceLocation getAnimationResource(EquipmentArmorItem animatable) {
        var render = animatable.getData().getRender();
        if (render != null && render.getAnimation() != null) {
            return new ResourceLocation(render.getAnimation());
        }
        return null;
    }
}