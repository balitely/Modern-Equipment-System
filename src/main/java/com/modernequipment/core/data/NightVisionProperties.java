package com.modernequipment.core.data;

public class NightVisionProperties {
    private float intensity = 1.0f;
    private boolean greenFilter = true;
    private boolean toggleable = true;

    public float getIntensity() { return intensity; }
    public void setIntensity(float intensity) { this.intensity = intensity; }
    public boolean isGreenFilter() { return greenFilter; }
    public void setGreenFilter(boolean greenFilter) { this.greenFilter = greenFilter; }
    public boolean isToggleable() { return toggleable; }
    public void setToggleable(boolean toggleable) { this.toggleable = toggleable; }
}