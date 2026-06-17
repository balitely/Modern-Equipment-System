package com.modernequipment.core.loader;

import com.modernequipment.core.data.AttachmentData;
import com.modernequipment.core.data.EquipmentData;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class EquipmentDataManager {
    private static final Map<ResourceLocation, EquipmentData> EQUIPMENT_DATA = new HashMap<>();
    private static final Map<ResourceLocation, AttachmentData> ATTACHMENT_DATA = new HashMap<>();

    public static void addEquipment(ResourceLocation id, EquipmentData data) {
        EQUIPMENT_DATA.put(id, data);
    }

    public static EquipmentData getEquipment(ResourceLocation id) {
        return EQUIPMENT_DATA.get(id);
    }

    public static Collection<EquipmentData> getAllEquipment() {
        return EQUIPMENT_DATA.values();
    }

    public static void addAttachment(ResourceLocation id, AttachmentData data) {
        ATTACHMENT_DATA.put(id, data);
    }

    public static AttachmentData getAttachment(ResourceLocation id) {
        return ATTACHMENT_DATA.get(id);
    }

    public static Collection<AttachmentData> getAllAttachments() {
        return ATTACHMENT_DATA.values();
    }

    public static void clear() {
        EQUIPMENT_DATA.clear();
        ATTACHMENT_DATA.clear();
    }
}