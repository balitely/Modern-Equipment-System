package com.modernequipment.api.attachment;

public enum AttachmentType {
    HELMET_TOP,       // 头盔顶板
    FACE_SHIELD,      // 面罩
    NECK_ARMOR,       // 护颈
    NVG_MOUNT,        // 夜视仪支架
    NVG,              // 夜视仪
    MANDIBLE,         // 下颌护甲
    COUNTERWEIGHT,    // 配重包
    LIGHT_MOHAWK,     // 信号灯
    ARMOR_PLATE,      // 防弹插板（通用）
    FRONT_PLATE,      // 前插板（可拆分为独立类型）
    BACK_PLATE,       // 后插板
    SIDE_PLATE,       // 侧插板
    GROIN_PLATE,      // 护裆板
    NONE;             // 无类型，用于占位

    public static AttachmentType fromString(String name) {
        try {
            return valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return NONE;
        }
    }
}