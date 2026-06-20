package com.modernequipment.util;

import com.modernequipment.MESMod;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Geo模型补丁工具类
 * 自动检测并给缺少 armorLeftArm / armorRightArm 骨骼的 Geo 模型添加空骨骼,
 * 解决与其他模组（如 Armored Arms）的兼容性问题。
 */
public class GeoModelPatcher {

    private static final String[] ARM_BONE_NAMES = {"armorLeftArm", "armorRightArm"};
    private static final float[] ARM_PIVOT_X = {-5f, 5f};
    private static final float ARM_PIVOT_Y = 22f;
    private static final float ARM_PIVOT_Z = 0f;
    private static final String BODY_BONE_NAME = "bipedBody";

    // 缓存 children 字段反射提升性能
    private static Field CHILDREN_FIELD;

    static {
        try {
            CHILDREN_FIELD = GeoBone.class.getDeclaredField("children");
            CHILDREN_FIELD.setAccessible(true);
        } catch (NoSuchFieldException e) {
            MESMod.LOGGER.error("GeoModelPatcher - Failed to get children field from GeoBone", e);
        }
    }

    /**
     * 对 BakedGeoModel 进行补丁，添加缺失的手臂骨骼
     * @param location 模型资源位置
     * @param model BakedGeoModel 实例
     */
    public static void patchModel(ResourceLocation location, BakedGeoModel model) {
        if (model == null || location == null) return;

        // 检查：如果已经有手臂骨骼则跳过（每次调用都会检查，正确处理资源重载）
        if (hasArmBones(model)) return;

        // 寻找 bipedBody 骨骼作为父骨骼
        GeoBone bodyBone = model.getBone(BODY_BONE_NAME).orElse(null);
        if (bodyBone == null) {
            // 没有 body 骨骼，说明不是身体盔甲模型，不处理
            return;
        }

        // 检查并尝试添加手臂骨骼
        try {
            if (CHILDREN_FIELD == null) return;

            @SuppressWarnings("unchecked")
            List<GeoBone> children = (List<GeoBone>) CHILDREN_FIELD.get(bodyBone);
            if (children == null) return;

            for (int i = 0; i < ARM_BONE_NAMES.length; i++) {
                String boneName = ARM_BONE_NAMES[i];
                // 确认子骨骼中不存在
                if (findChildByName(children, boneName)) continue;

                GeoBone armBone = new GeoBone(bodyBone, boneName, false, 0.0, true, false);
                armBone.setPivotX(ARM_PIVOT_X[i]);
                armBone.setPivotY(ARM_PIVOT_Y);
                armBone.setPivotZ(ARM_PIVOT_Z);
                armBone.setHidden(true);

                try {
                    children.add(armBone);
                    MESMod.LOGGER.debug("GeoModelPatcher - Patched {}: added {}", location, boneName);
                } catch (UnsupportedOperationException e) {
                    // 如果 children 是不可变列表，跳过
                    MESMod.LOGGER.warn("GeoModelPatcher - children list is immutable for {}, cannot add {}", location, boneName);
                    break;
                }
            }
        } catch (Exception e) {
            MESMod.LOGGER.warn("GeoModelPatcher - Failed to patch model {}: {}", location, e.getMessage());
        }
    }

    /**
     * 检查模型是否已有手臂骨骼
     */
    private static boolean hasArmBones(BakedGeoModel model) {
        for (String boneName : ARM_BONE_NAMES) {
            if (model.getBone(boneName).isPresent()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 在子骨骼列表中查找指定名称的骨骼
     */
    private static boolean findChildByName(List<GeoBone> children, String name) {
        if (children == null) return false;
        for (GeoBone child : children) {
            if (child.getName().equals(name)) return true;
        }
        return false;
    }
}