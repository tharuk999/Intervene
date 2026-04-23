package com.interactout.controller;

import com.interactout.model.UsageSessionEntity;
import com.interactout.service.UsageService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/usage")
@CrossOrigin(origins = "*")
public class UsageController {

    private final UsageService usageService;

    public UsageController(UsageService usageService) {
        this.usageService = usageService;
    }

    /**
     * POST /api/usage/session
     * Body: { packageName, durationMs, timestampMs }
     * Called by Android AccessibilityService when foreground session ends.
     */
    @PostMapping("/session")
    public UsageSessionEntity recordSession(@RequestBody Map<String, Object> body) {
        String pkg       = (String) body.get("packageName");
        long durationMs  = ((Number) body.get("durationMs")).longValue();
        long timestampMs = ((Number) body.get("timestampMs")).longValue();
        return usageService.recordSession(pkg, durationMs, timestampMs);
    }

    /**
     * POST /api/usage/resisted
     * Body: { packageName }
     * Called when user resists opening app (increments resisted urges counter).
     */
    @PostMapping("/resisted")
    public Map<String, String> recordResisted(@RequestBody Map<String, String> body) {
        usageService.recordResistedUrge(body.get("packageName"));
        return Map.of("status", "recorded");
    }

    /**
     * GET /api/usage/today/{packageName}
     * Android polls before each app open: returns usedMs, limitMs, bypassActive.
     * Used to compute dynamic intensity = usedMs / limitMs (paper Section 3.2).
     */
    @GetMapping("/today/{packageName}")
    public UsageService.UsageStatus getUsageToday(@PathVariable String packageName) {
        return usageService.getUsageToday(packageName);
    }

    /**
     * POST /api/usage/bypass
     * Body: { packageName, durationMs }
     * Paper Figure 4: "1 more minute" (60000) / "15 more minutes" (900000).
     */
    @PostMapping("/bypass")
    public Map<String, String> grantBypass(@RequestBody Map<String, Object> body) {
        String pkg      = (String) body.get("packageName");
        long durationMs = ((Number) body.get("durationMs")).longValue();
        usageService.grantBypass(pkg, durationMs);
        return Map.of("status", "granted", "packageName", pkg);
    }

    /**
     * GET /api/usage/bypass/{packageName}
     * Returns { active: true/false } — Android checks before suppressing gestures.
     */
    @GetMapping("/bypass/{packageName}")
    public Map<String, Boolean> checkBypass(@PathVariable String packageName) {
        return Map.of("active", usageService.hasBypass(packageName));
    }

    /**
     * GET /api/usage/dashboard
     * Single endpoint consumed by React Native home screen.
     * Returns totalScreenTimeMs, resistedUrges, activeFrictionApps,
     *         percentChangeVsYesterday, dailyBreakdown, topApps.
     */
    @GetMapping("/dashboard")
    public Map<String, Object> getDashboard() {
        return usageService.getDashboardSummary();
    }

    /**
     * GET /api/usage/history/weekly
     * Last 7 days of sessions — for charting in future.
     */
    @GetMapping("/history/weekly")
    public List<UsageSessionEntity> getWeeklyHistory() {
        return usageService.getWeeklyHistory();
    }
}
