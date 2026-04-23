package com.interactout.model;

import java.util.ArrayList;
import java.util.List;

public class AppProfile {
    private String packageName;
    private String appName;
    private boolean enabled;
    private long dailyLimitMs;
    private List<InterventionConfig> interventions = new ArrayList<>();

    public AppProfile() {}

    /**
     * Compute dynamic intensity (paper Section 3.2).
     * Android calls this before each dispatchGesture to scale intervention strength.
     * intensity = usedMs / dailyLimitMs, clamped 0.0–1.0
     */
    public float computeDynamicIntensity(long usedMs) {
        if (dailyLimitMs <= 0) return 1.0f;
        return Math.max(0f, Math.min(1f, (float) usedMs / dailyLimitMs));
    }

    public String getPackageName() { return packageName; }
    public void setPackageName(String packageName) { this.packageName = packageName; }

    public String getAppName() { return appName; }
    public void setAppName(String appName) { this.appName = appName; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public long getDailyLimitMs() { return dailyLimitMs; }
    public void setDailyLimitMs(long dailyLimitMs) { this.dailyLimitMs = dailyLimitMs; }

    public List<InterventionConfig> getInterventions() { return interventions; }
    public void setInterventions(List<InterventionConfig> interventions) {
        this.interventions = interventions;
    }
}
