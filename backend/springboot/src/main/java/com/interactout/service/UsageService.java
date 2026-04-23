package com.interactout.service;

import com.interactout.model.AppProfileEntity;
import com.interactout.model.UsageSessionEntity;
import com.interactout.repository.AppProfileRepository;
import com.interactout.repository.UsageSessionRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Transactional
public class UsageService {

    private final UsageSessionRepository sessionRepo;
    private final AppProfileRepository profileRepo;

    /**
     * In-memory bypass grants: packageName → expiry epoch ms.
     * Paper Figure 4: "1 more minute" / "15 more minutes" notification buttons.
     * For prod, persist to Redis or DB.
     */
    private final ConcurrentHashMap<String, Long> bypassGrants = new ConcurrentHashMap<>();

    public UsageService(UsageSessionRepository sessionRepo, AppProfileRepository profileRepo) {
        this.sessionRepo = sessionRepo;
        this.profileRepo = profileRepo;
    }

    // ── Session recording ──────────────────────────────────────────────────────

    /** Called by Android service when foreground session ends */
    public UsageSessionEntity recordSession(String packageName, long durationMs, long timestampMs) {
        UsageSessionEntity session = new UsageSessionEntity(packageName, durationMs, timestampMs);
        return sessionRepo.save(session);
    }

    /** Increment resisted urges for most recent session of package today */
    public void recordResistedUrge(String packageName) {
        LocalDate today = LocalDate.now();
        List<UsageSessionEntity> sessions =
            sessionRepo.findByPackageNameAndSessionDate(packageName, today);
        if (!sessions.isEmpty()) {
            UsageSessionEntity latest = sessions.get(sessions.size() - 1);
            latest.setResistedUrges(latest.getResistedUrges() + 1);
            sessionRepo.save(latest);
        }
    }

    // ── Usage vs limit ─────────────────────────────────────────────────────────

    public record UsageStatus(long usedMs, long limitMs, boolean bypassActive) {}

    /** Android polls this before each app open to decide intervention intensity */
    public UsageStatus getUsageToday(String packageName) {
        LocalDate today = LocalDate.now();
        long usedMs = sessionRepo.sumDurationByPackageAndDate(packageName, today);
        long limitMs = profileRepo.findById(packageName)
            .map(AppProfileEntity::getDailyLimitMs)
            .orElse(0L);
        boolean bypass = hasBypass(packageName);
        return new UsageStatus(usedMs, limitMs, bypass);
    }

    // ── Bypass (paper Figure 4) ────────────────────────────────────────────────

    public void grantBypass(String packageName, long durationMs) {
        bypassGrants.put(packageName, System.currentTimeMillis() + durationMs);
    }

    public boolean hasBypass(String packageName) {
        Long expiry = bypassGrants.get(packageName);
        return expiry != null && expiry > System.currentTimeMillis();
    }

    public void revokeBypass(String packageName) {
        bypassGrants.remove(packageName);
    }

    // ── Analytics ──────────────────────────────────────────────────────────────

    public long getTotalScreenTimeToday() {
        LocalDate today = LocalDate.now();
        return sessionRepo.dailyTotalsByPackage(today)
            .stream().mapToLong(row -> (Long) row[1]).sum();
    }

    public Map<String, Long> getDailyBreakdown(LocalDate date) {
        Map<String, Long> result = new LinkedHashMap<>();
        for (Object[] row : sessionRepo.dailyTotalsByPackage(date)) {
            result.put((String) row[0], (Long) row[1]);
        }
        return result;
    }

    public long getResistedUrgesToday() {
        return sessionRepo.totalResistedUrgesOnDate(LocalDate.now());
    }

    public long getActiveFrictionCount() {
        return profileRepo.findByEnabledTrue().stream()
            .filter(p -> p.getInterventions().stream().anyMatch(i -> i.isEnabled()))
            .count();
    }

    /**
     * Single endpoint for React Native home screen (Today's Pulse section).
     * Returns all dashboard data in one call.
     */
    public Map<String, Object> getDashboardSummary() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        long todayMs     = getTotalScreenTimeToday();
        long yesterdayMs = getDailyBreakdown(yesterday).values().stream().mapToLong(Long::longValue).sum();
        double pctChange = yesterdayMs > 0
            ? ((double)(todayMs - yesterdayMs) / yesterdayMs) * 100 : 0;

        // Top 3 apps by usage today
        List<Map<String, Object>> topApps = new ArrayList<>();
        sessionRepo.dailyTotalsByPackage(today).stream()
            .sorted((a, b) -> Long.compare((Long) b[1], (Long) a[1]))
            .limit(3)
            .forEach(row -> {
                String pkg = (String) row[0];
                Map<String, Object> entry = new LinkedHashMap<>();
                entry.put("packageName", pkg);
                entry.put("appName", profileRepo.findById(pkg)
                    .map(AppProfileEntity::getAppName).orElse(pkg));
                entry.put("usedMs", row[1]);
                topApps.add(entry);
            });

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalScreenTimeMs",        todayMs);
        summary.put("resistedUrges",             getResistedUrgesToday());
        summary.put("activeFrictionApps",        getActiveFrictionCount());
        summary.put("percentChangeVsYesterday",  Math.round(pctChange));
        summary.put("dailyBreakdown",            getDailyBreakdown(today));
        summary.put("topApps",                   topApps);
        return summary;
    }

    public List<UsageSessionEntity> getWeeklyHistory() {
        return sessionRepo.findByDateRange(LocalDate.now().minusDays(6), LocalDate.now());
    }
}
