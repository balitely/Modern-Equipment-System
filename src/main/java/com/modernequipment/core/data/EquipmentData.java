package com.modernequipment.core.data;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

public class EquipmentData {
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
    private InventoryProperties inventory;
    @SerializedName("allow_attachment_types")
    private List<String> allowAttachmentTypes;
    @SerializedName("default_attachments")
    private List<String> defaultAttachments;
    @SerializedName("exclusive_attachments")
    private Map<String, AttachmentData> exclusiveAttachments;
    private RenderProperties render;
    private NightVisionProperties nightVision;
    @SerializedName("sub_type")
    private String subType;
    @SerializedName("disables_face_slot")
    private boolean disablesFaceSlot = false;
    @SerializedName("disables_headset_slot")
    private boolean disablesHeadsetSlot = false;
    @SerializedName("disables_chest_rig_slot")
    private boolean disablesChestRigSlot = false;

    // MC原版护甲属性
    @SerializedName("vanilla_armor")
    private int vanillaArmor = 0;
    @SerializedName("vanilla_armor_toughness")
    private float vanillaArmorToughness = 0.0f;

    public String getSubType() { return subType; }
    public void setSubType(String subType) { this.subType = subType; }
    public boolean isDisablesFaceSlot() { return disablesFaceSlot; }
    public void setDisablesFaceSlot(boolean disablesFaceSlot) { this.disablesFaceSlot = disablesFaceSlot; }
    public boolean isDisablesHeadsetSlot() { return disablesHeadsetSlot; }
    public void setDisablesHeadsetSlot(boolean disablesHeadsetSlot) { this.disablesHeadsetSlot = disablesHeadsetSlot; }
    public boolean isDisablesChestRigSlot() { return disablesChestRigSlot; }
    public void setDisablesChestRigSlot(boolean disablesChestRigSlot) { this.disablesChestRigSlot = disablesChestRigSlot; }
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
    public InventoryProperties getInventory() { return inventory; }
    public void setInventory(InventoryProperties inventory) { this.inventory = inventory; }
    public List<String> getAllowAttachmentTypes() { return allowAttachmentTypes; }
    public void setAllowAttachmentTypes(List<String> allowAttachmentTypes) { this.allowAttachmentTypes = allowAttachmentTypes; }
    public List<String> getDefaultAttachments() { return defaultAttachments; }
    public void setDefaultAttachments(List<String> defaultAttachments) { this.defaultAttachments = defaultAttachments; }
    public Map<String, AttachmentData> getExclusiveAttachments() { return exclusiveAttachments; }
    public void setExclusiveAttachments(Map<String, AttachmentData> exclusiveAttachments) { this.exclusiveAttachments = exclusiveAttachments; }
    public RenderProperties getRender() { return render; }
    public void setRender(RenderProperties render) { this.render = render; }
    public NightVisionProperties getNightVision() { return nightVision; }
    public void setNightVision(NightVisionProperties nightVision) { this.nightVision = nightVision; }

    public int getVanillaArmor() { return vanillaArmor; }
    public void setVanillaArmor(int vanillaArmor) { this.vanillaArmor = vanillaArmor; }

    public float getVanillaArmorToughness() { return vanillaArmorToughness; }
    public void setVanillaArmorToughness(float vanillaArmorToughness) { this.vanillaArmorToughness = vanillaArmorToughness; }

    public boolean hasInventory() { return inventory != null && inventory.getTotalSlots() > 0; }
}