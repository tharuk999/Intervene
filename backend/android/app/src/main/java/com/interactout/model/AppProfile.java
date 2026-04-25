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
     * MODIFIED: Always return max intensity (1.0) instead of dynamic scaling.
     * Original paper logic: intensity = usedMs / dailyLimitMs
     * New behavior: interventions always at full strength
     */
    public float computeDynamicIntensity(long usedMs) {
        return 1.0f;
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