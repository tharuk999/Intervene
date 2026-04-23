package com.interactout.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.util.Log;

import com.interactout.api.ApiClient;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * BypassNotificationManager — implements paper Figure 4.
 *
 * When a user approaches or exceeds their daily limit, shows a persistent
 * notification with two action buttons:
 *   "1 More Minute"   → grants 60 000ms bypass
 *   "15 More Minutes" → grants 900 000ms bypass
 *
 * Bypass is stored in the Spring Boot backend (UsageService.bypassGrants map).
 * Android AccessibilityService checks bypass before applying interventions.
 */
public class BypassNotificationManager {

    private static final String TAG     = "InteractOut.Bypass";
    private static final String CHANNEL = "interactout_bypass";
    private static final int    NOTIF_ID = 1001;

    private static final String ACTION_BYPASS_1MIN   = "com.interactout.BYPASS_1MIN";
    private static final String ACTION_BYPASS_15MIN  = "com.interactout.BYPASS_15MIN";
    private static final String EXTRA_PACKAGE        = "packageName";

    private final Context context;
    private final NotificationManager notifManager;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public BypassNotificationManager(Context context) {
        this.context = context.getApplicationContext();
        this.notifManager =
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createChannel();
        registerReceiver();
    }

    // ── Public API ─────────────────────────────────────────────────────────────

    /**
     * Show bypass notification. Call when usedMs / limitMs >= threshold (paper: 85%).
     *
     * @param packageName target app
     * @param appName     human-readable name for notification title
     * @param usedMs      current usage
     * @param limitMs     daily limit
     */
    public void showBypassNotification(String packageName, String appName,
                                        long usedMs, long limitMs) {
        long remainingMs = Math.max(0, limitMs - usedMs);
        String remaining = remainingMs < 60_000
            ? "limit reached"
            : remainingMs / 60_000 + " min remaining";

        PendingIntent bypass1 = buildBypassIntent(packageName, ACTION_BYPASS_1MIN);
        PendingIntent bypass15 = buildBypassIntent(packageName, ACTION_BYPASS_15MIN);

        Notification notification = new Notification.Builder(context, CHANNEL)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Screen time limit — " + appName)
            .setContentText(remaining + " · Pause & Intent")
            .setOngoing(true)
            .setPriority(Notification.PRIORITY_HIGH)
            .addAction(new Notification.Action.Builder(
                null, "1 More Minute", bypass1).build())
            .addAction(new Notification.Action.Builder(
                null, "15 More Minutes", bypass15).build())
            .build();

        notifManager.notify(NOTIF_ID, notification);
        Log.d(TAG, "Bypass notification shown for " + packageName + " (" + remaining + ")");
    }

    public void dismissNotification() {
        notifManager.cancel(NOTIF_ID);
    }

    // ── Internal ───────────────────────────────────────────────────────────────

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(
                CHANNEL,
                "Screen Time Alerts",
                NotificationManager.IMPORTANCE_HIGH
            );
            ch.setDescription("Shown when approaching daily screen time limit");
            notifManager.createNotificationChannel(ch);
        }
    }

    private PendingIntent buildBypassIntent(String packageName, String action) {
        Intent intent = new Intent(action);
        intent.putExtra(EXTRA_PACKAGE, packageName);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        return PendingIntent.getBroadcast(context, packageName.hashCode(), intent, flags);
    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_BYPASS_1MIN);
        filter.addAction(ACTION_BYPASS_15MIN);

        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context ctx, Intent intent) {
                String pkg    = intent.getStringExtra(EXTRA_PACKAGE);
                String action = intent.getAction();
                if (pkg == null || action == null) return;

                long durationMs = ACTION_BYPASS_1MIN.equals(action) ? 60_000L : 900_000L;
                String label    = ACTION_BYPASS_1MIN.equals(action) ? "1 minute" : "15 minutes";

                executor.submit(() -> {
                    try {
                        // POST bypass to Spring Boot backend
                        okhttp3.OkHttpClient http = new okhttp3.OkHttpClient();
                        String body = "{\"packageName\":\"" + pkg +
                            "\",\"durationMs\":" + durationMs + "}";
                        okhttp3.RequestBody rb = okhttp3.RequestBody.create(
                            body,
                            okhttp3.MediaType.get("application/json")
                        );
                        okhttp3.Request req = new okhttp3.Request.Builder()
                            .url("http://10.0.2.2:8080/api/usage/bypass")
                            .post(rb)
                            .build();
                        http.newCall(req).execute().close();
                        Log.d(TAG, "Bypass granted: " + pkg + " for " + label);
                        dismissNotification();
                    } catch (Exception e) {
                        Log.e(TAG, "grantBypass failed: " + e.getMessage());
                    }
                });
            }
        }, filter);
    }
}
