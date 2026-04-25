package com.anonymous.Intervene;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ProfilesModule extends ReactContextBaseJavaModule {

    private static final String TAG   = "InteractOut.Profiles";
    private static final String PREFS = "interactout_profiles";

    // Default interventions template — all disabled, intensity 0.5
    private static final String[] INTERVENTION_TYPES = {
            "TAP_DELAY", "TAP_PROLONG", "TAP_SHIFT", "TAP_DOUBLE",
            "SWIPE_DELAY", "SWIPE_DECELERATE", "SWIPE_REVERSE", "SWIPE_MULTI_FINGER"
    };

    public ProfilesModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public String getName() {
        return "ProfilesModule";
    }

    // ── Get all apps (used apps first, then rest of installed) ────────────────

    @ReactMethod
    public void getAllApps(Promise promise) {
        try {
            PackageManager pm = getReactApplicationContext().getPackageManager();
            UsageStatsManager usm = (UsageStatsManager) getReactApplicationContext().getSystemService(Context.USAGE_STATS_SERVICE);

            // Get used apps from today only
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            long startOfDay = cal.getTimeInMillis();
            long now = System.currentTimeMillis();
            Map<String, UsageStats> usageMap = usm.queryAndAggregateUsageStats(startOfDay, now);

            // Build used apps list sorted by usage
            List<WritableMap> usedApps    = new ArrayList<>();
            List<WritableMap> unusedApps  = new ArrayList<>();

            List<ApplicationInfo> installed = pm.getInstalledApplications(0);
            for (ApplicationInfo info : installed) {
//                Log.d(TAG, "app name: " + pm.getApplicationLabel(info).toString());
                boolean isSystem = (info.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
                boolean isUpdated = (info.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0;

                if (isSystem && !isUpdated) continue;
                if (info.packageName.equals(getReactApplicationContext().getPackageName())) continue;

                String appName = pm.getApplicationLabel(info).toString();
                long usedMs = usageMap.containsKey(info.packageName) ? usageMap.get(info.packageName).getTotalTimeInForeground() : 0;

                WritableMap app = Arguments.createMap();
                app.putString("packageName", info.packageName);
                app.putString("appName", appName);
                app.putDouble("usedMs", usedMs);
                app.putBoolean("hasProfile", hasProfile(info.packageName));
//                DEBUGGING ONLY
                Log.d(TAG, "App name: " + appName + " usedMs: " + usedMs + "package name: " + info.packageName);

                if (usedMs > 0) usedApps.add(app);
                else unusedApps.add(app);
            }

            // Sort used apps by usage descending
            Collections.sort(usedApps, (a, b) ->
                    Double.compare(b.getDouble("usedMs"), a.getDouble("usedMs")));

            // Sort unused apps alphabetically
            Collections.sort(unusedApps, (a, b) ->
                    a.getString("appName").compareToIgnoreCase(b.getString("appName")));

            WritableArray result = Arguments.createArray();
            for (WritableMap app : usedApps)   result.pushMap(app);
            for (WritableMap app : unusedApps) result.pushMap(app);

            Log.d(TAG, "getAllApps: " + usedApps.size() + " used, " + unusedApps.size() + " unused");
            promise.resolve(result);
        } catch (Exception e) {
            Log.e(TAG, "getAllApps error: " + e.getMessage());
            promise.reject("GET_APPS_ERROR", e.getMessage());
        }
    }

    // ── Profile CRUD ───────────────────────────────────────────────────────────

    @ReactMethod
    public void getProfile(String packageName, Promise promise) {
        try {
            SharedPreferences prefs = getPrefs();
            String json = prefs.getString("profile_" + packageName, null);

            if (json != null) {
                promise.resolve(jsonToWritableMap(new JSONObject(json)));
            } else {
                // Return default empty profile
                promise.resolve(buildDefaultProfile(packageName));
            }
        } catch (Exception e) {
            Log.e(TAG, "getProfile error: " + e.getMessage());
            promise.reject("GET_PROFILE_ERROR", e.getMessage());
        }
    }

    @ReactMethod
    public void saveProfile(ReadableMap profile, Promise promise) {
        try {
            String packageName = profile.getString("packageName");
            JSONObject json = readableMapToJson(profile);
            getPrefs().edit()
                    .putString("profile_" + packageName, json.toString())
                    .apply();
            Log.d(TAG, "saveProfile: " + packageName);
            promise.resolve(true);
        } catch (Exception e) {
            Log.e(TAG, "saveProfile error: " + e.getMessage());
            promise.reject("SAVE_PROFILE_ERROR", e.getMessage());
        }
    }

    @ReactMethod
    public void getAllProfiles(Promise promise) {
        try {
            SharedPreferences prefs = getPrefs();
            WritableArray result = Arguments.createArray();
            for (Map.Entry<String, ?> entry : prefs.getAll().entrySet()) {
                if (!entry.getKey().startsWith("profile_")) continue;
                JSONObject json = new JSONObject((String) entry.getValue());
                result.pushMap(jsonToWritableMap(json));
            }
            promise.resolve(result);
        } catch (Exception e) {
            Log.e(TAG, "getAllProfiles error: " + e.getMessage());
            promise.reject("GET_PROFILES_ERROR", e.getMessage());
        }
    }

    @ReactMethod
    public void ping(Promise promise) {
        Log.d(TAG, "ping called from JS");
        promise.resolve("pong");
    }

    @ReactMethod public void addListener(String eventName) {}
    @ReactMethod public void removeListeners(Integer count) {}

    // ── Helpers ────────────────────────────────────────────────────────────────

    private boolean hasProfile(String packageName) {
        return getPrefs().contains("profile_" + packageName);
    }

    private SharedPreferences getPrefs() {
        return getReactApplicationContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    private WritableMap buildDefaultProfile(String packageName) {
        try {
            PackageManager pm = getReactApplicationContext().getPackageManager();
            String appName;
            try {
                appName = pm.getApplicationLabel(
                        pm.getApplicationInfo(packageName, 0)).toString();
            } catch (Exception e) {
                appName = packageName;
            }

            JSONObject profile = new JSONObject();
            profile.put("packageName", packageName);
            profile.put("appName", appName);
            profile.put("enabled", false);
            profile.put("dailyLimitMs", 0);

            JSONArray interventions = new JSONArray();
            for (String type : INTERVENTION_TYPES) {
                JSONObject intervention = new JSONObject();
                intervention.put("type", type);
                intervention.put("enabled", false);
                intervention.put("intensity", 0.5);
                interventions.put(intervention);
            }
            profile.put("interventions", interventions);

            return jsonToWritableMap(profile);
        } catch (Exception e) {
            Log.e(TAG, "buildDefaultProfile error: " + e.getMessage());
            return Arguments.createMap();
        }
    }

    // Convert JSONObject → WritableMap recursively
    private WritableMap jsonToWritableMap(JSONObject json) throws Exception {
        WritableMap map = Arguments.createMap();
        java.util.Iterator<String> keys = json.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            Object val = json.get(key);
            if (val instanceof JSONObject)      map.putMap(key, jsonToWritableMap((JSONObject) val));
            else if (val instanceof JSONArray)  map.putArray(key, jsonToWritableArray((JSONArray) val));
            else if (val instanceof Boolean)    map.putBoolean(key, (Boolean) val);
            else if (val instanceof Integer)    map.putInt(key, (Integer) val);
            else if (val instanceof Double)     map.putDouble(key, (Double) val);
            else if (val instanceof Long)       map.putDouble(key, ((Long) val).doubleValue());
            else                                map.putString(key, val.toString());
        }
        return map;
    }

    // Convert JSONArray → WritableArray recursively
    private com.facebook.react.bridge.WritableArray jsonToWritableArray(JSONArray arr) throws Exception {
        com.facebook.react.bridge.WritableArray out = Arguments.createArray();
        for (int i = 0; i < arr.length(); i++) {
            Object val = arr.get(i);
            if (val instanceof JSONObject)     out.pushMap(jsonToWritableMap((JSONObject) val));
            else if (val instanceof JSONArray) out.pushArray(jsonToWritableArray((JSONArray) val));
            else if (val instanceof Boolean)   out.pushBoolean((Boolean) val);
            else if (val instanceof Integer)   out.pushInt((Integer) val);
            else if (val instanceof Double)    out.pushDouble((Double) val);
            else if (val instanceof Long)      out.pushDouble(((Long) val).doubleValue());
            else                               out.pushString(val.toString());
        }
        return out;
    }

    // Convert ReadableMap → JSONObject recursively
    private JSONObject readableMapToJson(ReadableMap map) throws Exception {
        JSONObject json = new JSONObject();
        com.facebook.react.bridge.ReadableMapKeySetIterator it = map.keySetIterator();
        while (it.hasNextKey()) {
            String key = it.nextKey();
            switch (map.getType(key)) {
                case Boolean: json.put(key, map.getBoolean(key)); break;
                case Number:  json.put(key, map.getDouble(key));  break;
                case String:  json.put(key, map.getString(key));  break;
                case Map:     json.put(key, readableMapToJson(map.getMap(key))); break;
                case Array:   json.put(key, readableArrayToJson(map.getArray(key))); break;
                default: break;
            }
        }
        return json;
    }

    private JSONArray readableArrayToJson(com.facebook.react.bridge.ReadableArray arr) throws Exception {
        JSONArray json = new JSONArray();
        for (int i = 0; i < arr.size(); i++) {
            switch (arr.getType(i)) {
                case Boolean: json.put(arr.getBoolean(i)); break;
                case Number:  json.put(arr.getDouble(i));  break;
                case String:  json.put(arr.getString(i));  break;
                case Map:     json.put(readableMapToJson(arr.getMap(i))); break;
                case Array:   json.put(readableArrayToJson(arr.getArray(i))); break;
                default: break;
            }
        }
        return json;
    }
}