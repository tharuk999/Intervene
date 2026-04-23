package com.interactout.model;

public class InterventionConfig {
    private InterventionType type;
    private boolean enabled;
    private float intensity;   // 0.0–1.0

    public InterventionConfig() {}

    public InterventionConfig(InterventionType type, boolean enabled, float intensity) {
        this.type = type;
        this.enabled = enabled;
        this.intensity = intensity;
    }

    public InterventionType getType() { return type; }
    public void setType(InterventionType type) { this.type = type; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public float getIntensity() { return intensity; }
    public void setIntensity(float intensity) { this.intensity = Math.max(0f, Math.min(1f, intensity)); }
}
