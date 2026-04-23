package com.interactout.model;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "usage_sessions", indexes = {
    @Index(name = "idx_pkg_date", columnList = "package_name, session_date")
})
public class UsageSessionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "package_name", nullable = false)
    private String packageName;

    /** Total foreground time for this session in ms */
    private long durationMs;

    /** Unix epoch ms when session ended */
    private long timestampMs;

    /** Derived from timestampMs for easy date queries */
    private LocalDate sessionDate;

    /** Count of times user resisted opening app during this session */
    private int resistedUrges;

    public UsageSessionEntity() {}

    public UsageSessionEntity(String packageName, long durationMs, long timestampMs) {
        this.packageName = packageName;
        this.durationMs = durationMs;
        this.timestampMs = timestampMs;
        this.sessionDate = LocalDate.now();
        this.resistedUrges = 0;
    }

    // ── Getters / Setters ──────────────────────────────────────────────────────

    public Long getId() { return id; }

    public String getPackageName() { return packageName; }
    public void setPackageName(String packageName) { this.packageName = packageName; }

    public long getDurationMs() { return durationMs; }
    public void setDurationMs(long durationMs) { this.durationMs = durationMs; }

    public long getTimestampMs() { return timestampMs; }
    public void setTimestampMs(long timestampMs) { this.timestampMs = timestampMs; }

    public LocalDate getSessionDate() { return sessionDate; }
    public void setSessionDate(LocalDate sessionDate) { this.sessionDate = sessionDate; }

    public int getResistedUrges() { return resistedUrges; }
    public void setResistedUrges(int resistedUrges) { this.resistedUrges = resistedUrges; }
}
