package com.interactout.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "app_profiles")
public class AppProfileEntity {

    @Id
    @Column(name = "package_name")
    private String packageName;

    private String appName;
    private boolean enabled;
    private long dailyLimitMs;

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<InterventionEntity> interventions = new ArrayList<>();

    // ── Business logic ─────────────────────────────────────────────────────────

    /**
     * Replace interventions list, setting back-reference.
     * Ensures all 8 InterventionTypes are always present.
     */
    public void syncInterventions(List<InterventionEntity> incoming) {
        this.interventions.clear();

        // Track which types are covered
        List<InterventionType> covered = new ArrayList<>();
        for (InterventionEntity e : incoming) {
            e.setProfile(this);
            this.interventions.add(e);
            covered.add(e.getType());
        }

        // Fill any missing types with disabled defaults
        for (InterventionType t : InterventionType.values()) {
            if (!covered.contains(t)) {
                InterventionEntity def = new InterventionEntity();
                def.setType(t);
                def.setEnabled(false);
                def.setIntensity(0.3f);
                def.setProfile(this);
                this.interventions.add(def);
            }
        }
    }

    /**
     * Compute dynamic intensity (paper Section 3.2):
     * intensity = usedMs / dailyLimitMs, clamped 0–1.
     * Passed to GestureInterceptorService to scale all interventions.
     */
    public float computeDynamicIntensity(long usedMs) {
        if (dailyLimitMs <= 0) return 1.0f;
        return Math.max(0f, Math.min(1f, (float) usedMs / dailyLimitMs));
    }

    // ── Getters / Setters ──────────────────────────────────────────────────────

    public String getPackageName() { return packageName; }
    public void setPackageName(String packageName) { this.packageName = packageName; }

    public String getAppName() { return appName; }
    public void setAppName(String appName) { this.appName = appName; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public long getDailyLimitMs() { return dailyLimitMs; }
    public void setDailyLimitMs(long dailyLimitMs) { this.dailyLimitMs = dailyLimitMs; }

    public List<InterventionEntity> getInterventions() { return interventions; }
    public void setInterventions(List<InterventionEntity> interventions) {
        this.interventions = interventions;
    }
}
