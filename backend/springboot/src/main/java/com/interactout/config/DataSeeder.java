package com.interactout.config;

import com.interactout.model.*;
import com.interactout.repository.AppProfileRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Seeds database with realistic default profiles matching interventions.tsx APPS array.
 * Runs only when table is empty (idempotent).
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final AppProfileRepository repo;

    public DataSeeder(AppProfileRepository repo) { this.repo = repo; }

    @Override
    public void run(String... args) {
        if (repo.count() > 0) return;

        List<AppProfileEntity> profiles = new ArrayList<>();

        // Instagram — 3 active interventions, 2h limit
        profiles.add(build("com.instagram.android", "Instagram", true, 2 * 3_600_000L, List.of(
            intervention(InterventionType.TAP_DELAY,        true,  0.3f),
            intervention(InterventionType.TAP_SHIFT,        true,  0.45f),
            intervention(InterventionType.SWIPE_DECELERATE, true,  0.5f)
        )));

        // TikTok — 1 active, 1h limit
        profiles.add(build("com.zhiliaoapp.musically", "TikTok", true, 1 * 3_600_000L, List.of(
            intervention(InterventionType.SWIPE_DECELERATE, true,  0.7f),
            intervention(InterventionType.SWIPE_DELAY,      false, 0.4f),
            intervention(InterventionType.TAP_PROLONG,      false, 0.5f)
        )));

        // YouTube — 2 active, 3h limit
        profiles.add(build("com.google.android.youtube", "YouTube", true, 3 * 3_600_000L, List.of(
            intervention(InterventionType.TAP_DELAY,        true,  0.25f),
            intervention(InterventionType.SWIPE_DECELERATE, true,  0.5f)
        )));

        // Twitter — disabled, no limit yet
        profiles.add(build("com.twitter.android", "Twitter / X", false, 0L, List.of(
            intervention(InterventionType.TAP_DELAY, false, 0.3f),
            intervention(InterventionType.TAP_SHIFT, false, 0.3f)
        )));

        // Reddit — disabled
        profiles.add(build("com.reddit.frontpage", "Reddit", false, 0L, List.of(
            intervention(InterventionType.SWIPE_MULTI_FINGER, false, 0.5f),
            intervention(InterventionType.TAP_DOUBLE,         false, 0.0f)
        )));

        repo.saveAll(profiles);
        System.out.println("[InteractOut] Seeded " + profiles.size() + " profiles.");
    }

    private AppProfileEntity build(String pkg, String name, boolean enabled,
                                    long limitMs, List<InterventionEntity> interventions) {
        AppProfileEntity p = new AppProfileEntity();
        p.setPackageName(pkg);
        p.setAppName(name);
        p.setEnabled(enabled);
        p.setDailyLimitMs(limitMs);
        p.syncInterventions(interventions);
        return p;
    }

    private InterventionEntity intervention(InterventionType type, boolean enabled, float intensity) {
        InterventionEntity e = new InterventionEntity();
        e.setType(type);
        e.setEnabled(enabled);
        e.setIntensity(intensity);
        return e;
    }
}
