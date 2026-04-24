package com.interactout.model;
import java.util.List;
public class AppProfile {
    public String packageName;
    public String appName;
    public boolean enabled;
    public long dailyLimitMs;
    public List<InterventionConfig> interventions;
    public boolean isEnabled() { return enabled; }
    public String getPackageName() { return packageName; }
    public String getAppName() { return appName; }
    public long getDailyLimitMs() { return dailyLimitMs; }
    public List<InterventionConfig> getInterventions() { return interventions; }
    public float computeDynamicIntensity(long usedMs) {
        if (dailyLimitMs <= 0) return 1.0f;
        return Math.max(0f, Math.min(1f, (float) usedMs / dailyLimitMs));
    }
}
