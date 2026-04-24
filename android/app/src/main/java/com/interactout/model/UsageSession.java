package com.interactout.model;
public class UsageSession {
    public String packageName;
    public long durationMs;
    public long timestampMs;
    public UsageSession(String packageName, long durationMs, long timestampMs) {
        this.packageName = packageName;
        this.durationMs = durationMs;
        this.timestampMs = timestampMs;
    }
}
