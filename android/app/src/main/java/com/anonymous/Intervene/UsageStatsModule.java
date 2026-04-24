package com.anonymous.Intervene;

import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.util.Log;
import com.interactout.service.*;


import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class UsageStatsModule extends ReactContextBaseJavaModule {

    private static final String TAG   = "InteractOut.Usage";
    private static final String PREFS = "interactout_prefs";

    public UsageStatsModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public String getName() {
        return "UsageStatsModule";
    }

    // ── Permission checks ──────────────────────────────────────────────────────

    @ReactMethod
    public void isUsagePermissionGranted(Promise promise) {
        promise.resolve(hasUsagePermission());
    }

    @ReactMethod
    public void openUsageSettings() {
        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getReactApplicationContext().startActivity(intent);
    }

    @ReactMethod
    public void isAccessibilityServiceEnabled(Promise promise) {
        String service = getReactApplicationContext().getPackageName()
                + "/" + InteractOutAccessibilityService.class.getCanonicalName();
        String enabledServices = Settings.Secure.getString(
                getReactApplicationContext().getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        );
        boolean enabled = false;
        if (enabledServices != null) {
            for (String s : enabledServices.split(":")) {
                if (s.equalsIgnoreCase(service)) { enabled = true; break; }
            }
        }
        Log.d(TAG, "isAccessibilityServiceEnabled: " + enabled);
        promise.resolve(enabled);
    }

    @ReactMethod
    public void openAccessibilitySettings() {
        Log.d(TAG, "openAccessibilitySettings called");
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getReactApplicationContext().startActivity(intent);
    }

    // ── Main dashboard call ────────────────────────────────────────────────────

    /**
     * Returns everything the home screen needs in one call:
     * {
     *   totalScreenTimeMs,
     *   percentChangeVsYesterday,
     *   activeFrictionApps,
     *   resistedUrges,
     *   apps: [{ packageName, appName, usedMs }]
     * }
     */
    @ReactMethod
    public void getDashboard(Promise promise) {
        if (!hasUsagePermission()) {
            promise.reject("NO_PERMISSION", "Usage access not granted");
            return;
        }

        try {
            UsageStatsManager usm = (UsageStatsManager)
                    getReactApplicationContext().getSystemService(Context.USAGE_STATS_SERVICE);
            PackageManager pm = getReactApplicationContext().getPackageManager();

            // Today: midnight → now
            long[] today     = getDayRange(0);
            long[] yesterday = getDayRange(-1);

            Map<String, UsageStats> todayStats     = usm.queryAndAggregateUsageStats(today[0],     today[1]);
            Map<String, UsageStats> yesterdayStats = usm.queryAndAggregateUsageStats(yesterday[0], yesterday[1]);

            // Filter to user-installed + used apps, build list
            List<WritableMap> appList = new ArrayList<>();
            long totalToday     = 0;
            long totalYesterday = 0;

            for (Map.Entry<String, UsageStats> entry : todayStats.entrySet()) {
                String pkg   = entry.getKey();
                long usedMs  = entry.getValue().getTotalTimeInForeground();
                if (usedMs < 60_000) continue; // skip apps used < 1 min
                if (isSystemApp(pm, pkg)) continue;

                String appName = getAppName(pm, pkg);

                WritableMap app = Arguments.createMap();
                app.putString("packageName", pkg);
                app.putString("appName", appName);
                app.putDouble("usedMs", usedMs);
                appList.add(app);

                totalToday += usedMs;
            }

            // Sort by usedMs descending
            Collections.sort(appList, (a, b) ->
                    Double.compare(b.getDouble("usedMs"), a.getDouble("usedMs"))
            );

            // Yesterday total
            for (UsageStats s : yesterdayStats.values()) {
                totalYesterday += s.getTotalTimeInForeground();
            }

            // % change vs yesterday
            int pctChange = 0;
            if (totalYesterday > 0) {
                pctChange = (int) (((totalToday - totalYesterday) * 100.0) / totalYesterday);
            }

            // Resisted urges from SharedPreferences
            SharedPreferences prefs = getReactApplicationContext()
                    .getSharedPreferences(PREFS, Context.MODE_PRIVATE);
            int resistedUrges = prefs.getInt("resisted_urges_today", 0);

            // Active friction apps — count from accessibility service profile cache
            int frictionApps = 0;
            if (InteractOutAccessibilityService.instance != null) {
                frictionApps = InteractOutAccessibilityService.instance.getActiveFrictionAppCount();
            }

            // Build apps WritableArray
            WritableArray appsArray = Arguments.createArray();
            for (WritableMap app : appList) {
                appsArray.pushMap(app);
            }

            // Build result
            WritableMap result = Arguments.createMap();
            result.putDouble("totalScreenTimeMs",        totalToday);
            result.putDouble("totalYesterdayMs",         totalYesterday);
            result.putInt("percentChangeVsYesterday",    pctChange);
            result.putInt("activeFrictionApps",          frictionApps);
            result.putInt("resistedUrges",               resistedUrges);
            result.putArray("apps",                      appsArray);

            Log.d(TAG, "getDashboard: totalToday=" + totalToday + " apps=" + appList.size());
            promise.resolve(result);

        } catch (Exception e) {
            Log.e(TAG, "getDashboard error: " + e.getMessage());
            promise.reject("DASHBOARD_ERROR", e.getMessage());
        }
    }

    // ── Resisted urges (called by accessibility service when intervention fires) ──

    @ReactMethod
    public void incrementResistedUrges(Promise promise) {
        SharedPreferences prefs = getReactApplicationContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        int current = prefs.getInt("resisted_urges_today", 0);
        prefs.edit().putInt("resisted_urges_today", current + 1).apply();
        Log.d(TAG, "resistedUrges incremented to " + (current + 1));
        promise.resolve(current + 1);
    }

    @ReactMethod
    public void ping(Promise promise) {
        Log.d(TAG, "ping called from JS");
        promise.resolve("pong");
    }

    @ReactMethod public void addListener(String eventName) {}
    @ReactMethod public void removeListeners(Integer count) {}

    // ── Helpers ────────────────────────────────────────────────────────────────

    private boolean hasUsagePermission() {
        AppOpsManager aom = (AppOpsManager)
                getReactApplicationContext().getSystemService(Context.APP_OPS_SERVICE);
        int mode = aom.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                getReactApplicationContext().getPackageName()
        );
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    /** Returns [startMs, endMs] for a day offset (0=today, -1=yesterday) */
    private long[] getDayRange(int offsetDays) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, offsetDays);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long start = cal.getTimeInMillis();
        long end   = offsetDays == 0
                ? System.currentTimeMillis()
                : start + 86_400_000L;
        return new long[]{ start, end };
    }

    private boolean isSystemApp(PackageManager pm, String pkg) {
        try {
            ApplicationInfo info = pm.getApplicationInfo(pkg, 0);
            return (info.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
        } catch (PackageManager.NameNotFoundException e) {
            return true;
        }
    }

    private String getAppName(PackageManager pm, String pkg) {
        try {
            return pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString();
        } catch (PackageManager.NameNotFoundException e) {
            return pkg;
        }
    }
}