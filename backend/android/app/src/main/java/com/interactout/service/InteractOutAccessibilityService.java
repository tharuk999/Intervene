package com.interactout.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import com.interactout.api.ApiClient;
import com.interactout.model.AppProfile;
import com.interactout.model.InterventionConfig;
import com.interactout.model.UsageSession;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Core Android AccessibilityService.
 *
 * Responsibilities:
 *  1. Detect foreground app changes (TYPE_WINDOW_STATE_CHANGED)
 *  2. Poll backend for current usage vs limit
 *  3. Compute dynamic intensity = usedMs / limitMs (paper Section 3.2)
 *  4. Activate GestureInterceptorService with correct intensity
 *  5. Report sessions to backend when app changes away
 *  6. Trigger BypassNotificationManager when limit approached/exceeded
 *
 * Must be declared in AndroidManifest.xml and enabled by user in
 * Settings → Accessibility → Downloaded apps → InteractOut.
 */
public class InteractOutAccessibilityService extends AccessibilityService {

    private static final String TAG = "InteractOut.A11y";
    private static final float BYPASS_NOTIFICATION_THRESHOLD = 0.85f; // 85% of limit

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Map<String, AppProfile> profileCache = new HashMap<>();

    private GestureInterceptorService gestureService;
    private BypassNotificationManager bypassManager;
    private boolean gestureServiceBound = false;

    // Current foreground app tracking
    private String currentForegroundPkg = null;
    private long sessionStartMs = 0;

    // ── Lifecycle ──────────────────────────────────────────────────────────────

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();

        // Configure which events we receive
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        info.flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS;
        info.notificationTimeout = 100;
        setServiceInfo(info);

        // Register this instance with GestureInterceptorService
        GestureInterceptorService.setAccessibilityService(this);

        // Bind to GestureInterceptorService
        Intent intent = new Intent(this, GestureInterceptorService.class);
        bindService(intent, gestureConnection, Context.BIND_AUTO_CREATE);

        // Init bypass manager
        bypassManager = new BypassNotificationManager(this);

        // Load profiles from backend
        refreshProfiles();

        Log.d(TAG, "AccessibilityService connected");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (gestureServiceBound) {
            unbindService(gestureConnection);
        }
        executor.shutdown();
    }

    // ── Event handling ─────────────────────────────────────────────────────────

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return;
        if (event.getPackageName() == null) return;

        String newPkg = event.getPackageName().toString();
        if (newPkg.equals(currentForegroundPkg)) return; // same app, ignore

        // ── App changed away — report session ────────────────────────────────
        if (currentForegroundPkg != null && sessionStartMs > 0) {
            long durationMs = System.currentTimeMillis() - sessionStartMs;
            final String reportPkg = currentForegroundPkg;
            final long reportDuration = durationMs;
            executor.submit(() ->
                    ApiClient.get().reportSession(
                            new UsageSession(reportPkg, reportDuration, System.currentTimeMillis())
                    )
            );
        }

        currentForegroundPkg = newPkg;
        sessionStartMs = System.currentTimeMillis();

        // ── Check if new app has interventions ───────────────────────────────
        activateForPackage(newPkg);
    }

    @Override
    public void onInterrupt() {
        Log.d(TAG, "Interrupted");
    }

    // ── Intervention activation ────────────────────────────────────────────────

    /**
     * MODIFIED: Always use max intensity (1.0) regardless of usage.
     * Original logic fetched usedMs/limitMs from backend and computed dynamic scaling.
     * Now we just activate interventions at full strength if profile enabled.
     */
    private void activateForPackage(String packageName) {
        AppProfile profile = profileCache.get(packageName);
        if (profile == null || !profile.isEnabled()) {
            if (gestureService != null) gestureService.deactivate();
            return;
        }

        executor.submit(() -> {
            try {
                // Check bypass first
                boolean hasBypass = ApiClient.get().checkBypass(packageName);
                if (hasBypass) {
                    if (gestureService != null) gestureService.deactivate();
                    return;
                }

                // Always max intensity (1.0) — no dynamic scaling
                float maxIntensity = 1.0f;

                List<InterventionConfig> enabled = new ArrayList<>();
                for (InterventionConfig cfg : profile.getInterventions()) {
                    if (cfg.isEnabled()) enabled.add(cfg);
                }

                if (!enabled.isEmpty() && gestureService != null) {
                    gestureService.activate(enabled, maxIntensity);
                }

                Log.d(TAG, "Activated for " + packageName + " intensity=1.0 (MAX)");
            } catch (Exception e) {
                Log.e(TAG, "activateForPackage error: " + e.getMessage());
            }
        });
    }

    // ── Profile refresh ────────────────────────────────────────────────────────

    private void refreshProfiles() {
        executor.submit(() -> {
            try {
                List<AppProfile> profiles = ApiClient.get().fetchActiveProfiles();
                profileCache.clear();
                for (AppProfile p : profiles) {
                    profileCache.put(p.getPackageName(), p);
                }
                Log.d(TAG, "Loaded " + profiles.size() + " profiles from backend");
            } catch (Exception e) {
                Log.e(TAG, "refreshProfiles error: " + e.getMessage());
            }
        });
    }

    // ── Service connection ─────────────────────────────────────────────────────

    private final ServiceConnection gestureConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            gestureService = ((GestureInterceptorService.LocalBinder) service).getService();
            gestureServiceBound = true;
            Log.d(TAG, "GestureInterceptorService bound");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            gestureServiceBound = false;
            gestureService = null;
        }
    };
}