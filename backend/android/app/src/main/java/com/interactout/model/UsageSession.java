package com.interactout.model;

public class UsageSession {
    private String packageName;
    private long durationMs;
    private long timestampMs;

    public UsageSession() {}

    public UsageSession(String packageName, long durationMs, long timestampMs) {
        this.packageName = packageName;
        this.durationMs = durationMs;
        this.timestampMs = timestampMs;
    }

    public String getPackageName() { return packageName; }
    public void setPackageName(String packageName) { this.packageName = packageName; }

    public long getDurationMs() { return durationMs; }
    public void setDurationMs(long durationMs) { this.durationMs = durationMs; }

    public long getTimestampMs() { return timestampMs; }
    public void setTimestampMs(long timestampMs) { this.timestampMs = timestampMs; }
}
