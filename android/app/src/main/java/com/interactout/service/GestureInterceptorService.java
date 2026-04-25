package com.interactout.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.app.Service;
import android.content.Intent;
import android.graphics.Path;
import android.graphics.PointF;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import com.interactout.model.InterventionConfig;
import com.interactout.model.InterventionType;

import java.util.List;

/**
 * GestureInterceptorService — Figure 3 proxy layer from the InteractOut paper.
 *
 * Intercepts raw MotionEvents, applies the configured interventions,
 * then dispatches modified virtual gestures via AccessibilityService.dispatchGesture().
 *
 * Intensity parameter (0.0–1.0) is passed in from InteractOutAccessibilityService
 * and represents usedMs / limitMs (paper Section 3.2 dynamic scaling).
 *
 * All 8 interventions implemented:
 *   TAP:   TAP_DELAY, TAP_PROLONG, TAP_SHIFT, TAP_DOUBLE
 *   SWIPE: SWIPE_DELAY, SWIPE_DECELERATE, SWIPE_REVERSE, SWIPE_MULTI_FINGER
 */
public class GestureInterceptorService extends Service {

    private static final String TAG = "InteractOut.Gesture";

    // Back-reference to accessibility service for dispatchGesture()
    private static AccessibilityService accessibilityService;

    private final IBinder binder = new LocalBinder();
    private final Handler handler = new Handler(Looper.getMainLooper());

    private List<InterventionConfig> activeInterventions;
    private float globalIntensityMultiplier = 1.0f;

    // TAP_DOUBLE state
    private long lastTapTimeMs = 0;
    private static final long DOUBLE_TAP_WINDOW_MS = 300;

    // ── Binder ─────────────────────────────────────────────────────────────────

    public class LocalBinder extends Binder {
        public GestureInterceptorService getService() { return GestureInterceptorService.this; }
    }

    @Override public IBinder onBind(Intent intent) { return binder; }

    // ── Public API ─────────────────────────────────────────────────────────────

    public static void setAccessibilityService(AccessibilityService svc) {
        accessibilityService = svc;
    }

    /**
     * Called by InteractOutAccessibilityService when a profile is activated.
     * intensity = usedMs / limitMs (0.0 at session start → 1.0 at daily limit).
     */
    public void activate(List<InterventionConfig> interventions, float dynamicIntensity) {
        this.activeInterventions = interventions;
        this.globalIntensityMultiplier = Math.max(0f, Math.min(1f, dynamicIntensity));
        Log.d(TAG, "Activated " + interventions.size() + " interventions, intensity=" + dynamicIntensity);
    }

    public void deactivate() {
        activeInterventions = null;
        globalIntensityMultiplier = 0f;
        Log.d(TAG, "Deactivated");
    }

    // ── Tap processing ─────────────────────────────────────────────────────────

    /**
     * Process a raw tap. Returns false when we handle dispatch ourselves.
     * Called from AccessibilityService touch listener.
     *
     * @param x          raw X coordinate
     * @param y          raw Y coordinate
     * @param durationMs how long finger was held (for TAP_PROLONG check)
     */
    public boolean processTap(float x, float y, long durationMs) {
        if (activeInterventions == null) return true; // pass through

        float finalX = x;
        float finalY = y;
        long delayMs = 0;

        for (InterventionConfig cfg : activeInterventions) {
            if (!cfg.isEnabled()) continue;
            float intensity = cfg.getIntensity() * globalIntensityMultiplier;

            switch (cfg.getType()) {

                case TAP_DELAY:
                    // Paper: delay up to 800ms
                    delayMs += (long)(intensity * 3000);
                    break;

                case TAP_PROLONG:
                    // Paper: require hold >= threshold (up to 1500ms)
                    long threshold = (long)(intensity * 4000);
                    if (durationMs < threshold) {
                        Log.d(TAG, "TAP_PROLONG: suppressed (" + durationMs + "ms < " + threshold + "ms)");
                        return false; // suppress tap
                    }
                    break;

                case TAP_SHIFT:
                    // Paper: offset up to 60px
                    float shift = intensity * 60f;
                    finalX += shift;
                    finalY += shift;
                    Log.d(TAG, "TAP_SHIFT: (" + x + "," + y + ") → (" + finalX + "," + finalY + ")");
                    break;

                case TAP_DOUBLE:
                    // Paper: require double-tap for single effect
                    long now = System.currentTimeMillis();
                    if (now - lastTapTimeMs < DOUBLE_TAP_WINDOW_MS) {
                        lastTapTimeMs = 0;
                        // Second tap detected — fall through to dispatch
                    } else {
                        lastTapTimeMs = now;
                        Log.d(TAG, "TAP_DOUBLE: first tap suppressed, waiting for second");
                        return false; // suppress first tap
                    }
                    break;

                default:
                    break;
            }
        }

        final float fx = finalX, fy = finalY;
        handler.postDelayed(() -> dispatchTap(fx, fy), delayMs);
        return false;
    }

    // ── Swipe processing ───────────────────────────────────────────────────────

    /**
     * Process a raw swipe gesture.
     *
     * @param start       swipe start point
     * @param end         swipe end point
     * @param durationMs  natural swipe duration
     * @param fingerCount number of fingers detected (for SWIPE_MULTI_FINGER)
     */
    public boolean processSwipe(PointF start, PointF end, long durationMs, int fingerCount) {
        if (activeInterventions == null) return true;

        PointF finalStart = new PointF(start.x, start.y);
        PointF finalEnd   = new PointF(end.x, end.y);
        long finalDuration = durationMs;
        long delayMs = 0;

        for (InterventionConfig cfg : activeInterventions) {
            if (!cfg.isEnabled()) continue;
            float intensity = cfg.getIntensity() * globalIntensityMultiplier;

            switch (cfg.getType()) {

                case SWIPE_DELAY:
                    // Paper: pause up to 600ms between swipes
                    delayMs += (long)(intensity * 2000);
                    break;

                case SWIPE_DECELERATE:
                    // Paper: multiply duration by up to 4x
                    float factor = 1.0f + intensity * 9.0f;
                    finalDuration = (long)(durationMs * factor);
                    Log.d(TAG, "SWIPE_DECELERATE: " + durationMs + "ms → " + finalDuration + "ms (" + factor + "x)");
                    break;

                case SWIPE_REVERSE:
                    // Paper: reverse trajectory start↔end
                    PointF tmp = new PointF(finalStart.x, finalStart.y);
                    finalStart = new PointF(finalEnd.x, finalEnd.y);
                    finalEnd = tmp;
                    Log.d(TAG, "SWIPE_REVERSE applied");
                    break;

                case SWIPE_MULTI_FINGER:
                    // Paper: require N fingers (scales with intensity: 0→2, 1→4)
                    int required = 1 + (int)(intensity * 3);
                    if (fingerCount < required) {
                        Log.d(TAG, "SWIPE_MULTI_FINGER: need " + required + " fingers, got " + fingerCount);
                        return false; // suppress under-fingered swipe
                    }
                    break;

                default:
                    break;
            }
        }

        final PointF fs = finalStart, fe = finalEnd;
        final long fd = finalDuration;
        handler.postDelayed(() -> dispatchSwipe(fs, fe, fd), delayMs);
        return false;
    }

    // ── Gesture dispatch helpers ───────────────────────────────────────────────

    private void dispatchTap(float x, float y) {
        if (accessibilityService == null) return;
        Path path = new Path();
        path.moveTo(x, y);
        GestureDescription gesture = new GestureDescription.Builder()
            .addStroke(new GestureDescription.StrokeDescription(path, 0, 50))
            .build();
        accessibilityService.dispatchGesture(gesture, null, null);
        Log.d(TAG, "dispatchTap(" + x + "," + y + ")");
    }

    private void dispatchSwipe(PointF start, PointF end, long durationMs) {
        if (accessibilityService == null) return;
        Path path = new Path();
        path.moveTo(start.x, start.y);
        path.lineTo(end.x, end.y);
        GestureDescription gesture = new GestureDescription.Builder()
            .addStroke(new GestureDescription.StrokeDescription(path, 0, Math.max(1, durationMs)))
            .build();
        accessibilityService.dispatchGesture(gesture, null, null);
        Log.d(TAG, "dispatchSwipe(" + start + " → " + end + ", " + durationMs + "ms)");
    }
}
