package com.modernequipment.client.animator;

import com.modernequipment.client.model.DynamicEquipmentModel;
import com.modernequipment.core.item.EquipmentArmorItem;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class EquipmentArmorAnimator extends GeoArmorRenderer<EquipmentArmorItem> {
    public EquipmentArmorAnimator() {
        super(new DynamicEquipmentModel());
    }
}