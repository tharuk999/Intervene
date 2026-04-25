package com.interactout.model;
public class InterventionConfig {
    public InterventionType type;
    public boolean enabled;
    public float intensity;
    public boolean isEnabled() { return enabled; }
    public InterventionType getType() { return type; }
    public float getIntensity() { return intensity; }
}
