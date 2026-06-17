package com.modernequipment.core.data;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class InventoryProperties {
    private String type = "grid";
    private int rows = 0;
    private int columns = 0;
    private int gridWidth = 0;
    private int gridHeight = 0;
    private List<SlotDefinition> slots;
    @SerializedName("weight_affects_player")
    private boolean weightAffectsPlayer = true;

    public List<SlotDefinition> getOriginalSlotList() {
        if ("custom".equals(type) && slots != null) {
            return slots;
        }
        List<SlotDefinition> list = new ArrayList<>();
        int id = 0;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                list.add(new SlotDefinition(id++, col, row, 1, 1));
            }
        }
        return list;
    }

    public int getTotalSlots() {
        if ("custom".equals(type) && slots != null) {
            int total = 0;
            for (SlotDefinition slot : slots) {
                total += slot.getWidth() * slot.getHeight();
            }
            return total;
        }
        return rows * columns;
    }

    public List<SlotDefinition> getSlotList() {
        if ("custom".equals(type) && slots != null) {
            List<SlotDefinition> expanded = new ArrayList<>();
            int globalId = 0;
            for (SlotDefinition original : slots) {
                int startX = original.getX();
                int startY = original.getY();
                int w = original.getWidth();
                int h = original.getHeight();
                for (int dy = 0; dy < h; dy++) {
                    for (int dx = 0; dx < w; dx++) {
                        SlotDefinition sub = new SlotDefinition(
                                globalId++,
                                startX + dx,
                                startY + dy,
                                1, 1   // 强制为 1x1
                        );
                        expanded.add(sub);
                    }
                }
            }
            validateSlotIds(expanded);
            return expanded;
        }
        List<SlotDefinition> list = new ArrayList<>();
        int id = 0;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                list.add(new SlotDefinition(id++, col, row, 1, 1));
            }
        }
        return list;
    }

    public int getEffectiveGridWidth() {
        if ("custom".equals(type) && slots != null) {
            if (gridWidth > 0) return gridWidth;
            int maxX = 0;
            for (SlotDefinition slot : slots) {
                int right = slot.getX() + slot.getWidth();
                if (right > maxX) maxX = right;
            }
            return maxX;
        }
        return columns;
    }

    public int getEffectiveGridHeight() {
        if ("custom".equals(type) && slots != null) {
            if (gridHeight > 0) return gridHeight;
            int maxY = 0;
            for (SlotDefinition slot : slots) {
                int bottom = slot.getY() + slot.getHeight();
                if (bottom > maxY) maxY = bottom;
            }
            return maxY;
        }
        return rows;
    }

    private void validateSlotIds(List<SlotDefinition> slots) {
        if (slots == null || slots.isEmpty()) return;
        Set<Integer> ids = new HashSet<>();
        for (SlotDefinition slot : slots) {
            if (slot.getId() < 0) {
            }
            if (!ids.add(slot.getId())) {
            }
        }
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public int getRows() { return rows; }
    public void setRows(int rows) { this.rows = rows; }
    public int getColumns() { return columns; }
    public void setColumns(int columns) { this.columns = columns; }
    public int getGridWidth() { return gridWidth; }
    public void setGridWidth(int gridWidth) { this.gridWidth = gridWidth; }
    public int getGridHeight() { return gridHeight; }
    public void setGridHeight(int gridHeight) { this.gridHeight = gridHeight; }
    public List<SlotDefinition> getSlots() { return slots; }
    public void setSlots(List<SlotDefinition> slots) { this.slots = slots; }
    public boolean isWeightAffectsPlayer() { return weightAffectsPlayer; }
    public void setWeightAffectsPlayer(boolean weightAffectsPlayer) { this.weightAffectsPlayer = weightAffectsPlayer; }
}