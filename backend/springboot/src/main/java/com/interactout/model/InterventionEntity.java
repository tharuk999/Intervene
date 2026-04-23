package com.interactout.model;

import jakarta.persistence.*;

@Entity
@Table(name = "interventions")
public class InterventionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_name", nullable = false)
    private AppProfileEntity profile;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InterventionType type;

    private boolean enabled;

    /** 0.0–1.0 normalized intensity; maps to paper's per-intervention scale */
    private float intensity;

    // ── Getters / Setters ─────────────────────────────────────────────────────

    public Long getId() { return id; }

    public AppProfileEntity getProfile() { return profile; }
    public void setProfile(AppProfileEntity profile) { this.profile = profile; }

    public InterventionType getType() { return type; }
    public void setType(InterventionType type) { this.type = type; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public float getIntensity() { return intensity; }
    public void setIntensity(float intensity) { this.intensity = Math.max(0f, Math.min(1f, intensity)); }
}
