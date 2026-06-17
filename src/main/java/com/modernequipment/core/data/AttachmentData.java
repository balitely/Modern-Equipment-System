package com.modernequipment.core.data;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;

/**
 * 对应配件 JSON 文件（如 attachments/armor_plate.json）。
 */
public class AttachmentData {
    private String type;
    private String id;
    @SerializedName("display_name")
    private String displayName;
    @SerializedName("max_stack_size")
    private int maxStackSize = 1;
    private int durability;
    private float weight;
    private ModifierProperties modifiers;
    private CombatProperties combat;
    @SerializedName("compatible_parent_types")
    private List<String> compatibleParentTypes;
    @SerializedName("mount_slots")
    private List<String> mountSlots;
    private boolean unique = false;
    private RenderProperties render;
    private Compatible compatible;

    // 新增：槽位特定战斗属性（键为槽位名，如 "front_plate"）
    @SerializedName("mount_effects")
    private Map<String, CombatProperties> mountEffects;

    public static class Compatible {
        private List<String> tags;
        private List<String> ids;

        public List<String> getTags() { return tags; }
        public void setTags(List<String> tags) { this.tags = tags; }
        public List<String> getIds() { return ids; }
        public void setIds(List<String> ids) { this.ids = ids; }

        public boolean isEmpty() {
            return (tags == null || tags.isEmpty()) && (ids == null || ids.isEmpty());
        }
    }

    // Getters and Setters
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public int getMaxStackSize() { return maxStackSize; }
    public void setMaxStackSize(int maxStackSize) { this.maxStackSize = maxStackSize; }
    public int getDurability() { return durability; }
    public void setDurability(int durability) { this.durability = durability; }
    public float getWeight() { return weight; }
    public void setWeight(float weight) { this.weight = weight; }
    public ModifierProperties getModifiers() { return modifiers; }
    public void setModifiers(ModifierProperties modifiers) { this.modifiers = modifiers; }
    public CombatProperties getCombat() { return combat; }
    public void setCombat(CombatProperties combat) { this.combat = combat; }
    public List<String> getCompatibleParentTypes() { return compatibleParentTypes; }
    public void setCompatibleParentTypes(List<String> compatibleParentTypes) { this.compatibleParentTypes = compatibleParentTypes; }
    public List<String> getMountSlots() { return mountSlots; }
    public void setMountSlots(List<String> mountSlots) { this.mountSlots = mountSlots; }
    public boolean isUnique() { return unique; }
    public void setUnique(boolean unique) { this.unique = unique; }
    public RenderProperties getRender() { return render; }
    public void setRender(RenderProperties render) { this.render = render; }
    public Compatible getCompatible() { return compatible; }
    public void setCompatible(Compatible compatible) { this.compatible = compatible; }

    public Map<String, CombatProperties> getMountEffects() { return mountEffects; }
    public void setMountEffects(Map<String, CombatProperties> mountEffects) { this.mountEffects = mountEffects; }
}