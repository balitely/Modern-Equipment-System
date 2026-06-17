package com.modernequipment.core.data;

import java.util.Map;

public class PackMeta {
    private String name;
    private String description;
    private Map<String, String> dependencies;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Map<String, String> getDependencies() { return dependencies; }
    public void setDependencies(Map<String, String> dependencies) { this.dependencies = dependencies; }
}