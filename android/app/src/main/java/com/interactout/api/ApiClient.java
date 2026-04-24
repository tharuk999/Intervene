package com.interactout.api;

import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.interactout.model.AppProfile;
import com.interactout.model.UsageSession;
import okhttp3.*;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//
//*
// * HTTP client connecting Android AccessibilityService → Spring Boot backend.
// *
// * All calls are synchronous — run them off the main thread
// * (e.g. in an Executor or coroutine launched from the service).
// *
// * BASE_URL:
// *   Android Emulator → http://10.0.2.2:8080/api
// *   Real device (same WiFi) → http://YOUR_LAN_IP:8080/api
//

public class ApiClient {

    private static final String TAG = "InteractOut.ApiClient";
    // Change to your PC's LAN IP when testing on a real device
    private static final String BASE_URL = "http://10.0.2.2:8080/api";
    private static final MediaType JSON = MediaType.get("application/json");

    private static ApiClient instance;
    private final OkHttpClient http;
    private final Gson gson;

    private ApiClient() {
        http = new OkHttpClient.Builder()
            .connectTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
            .build();
        gson = new GsonBuilder().create();
    }

    public static synchronized ApiClient get() {
        if (instance == null) instance = new ApiClient();
        return instance;
    }

    // ── Profiles ───────────────────────────────────────────────────────────────


    public List<AppProfile> fetchActiveProfiles() {
        try {
            Request req = new Request.Builder().url(BASE_URL + "/profiles/active").build();
            try (Response res = http.newCall(req).execute()) {
                if (!res.isSuccessful() || res.body() == null) return new ArrayList<>();
                Type listType = new TypeToken<List<AppProfile>>(){}.getType();
                return gson.fromJson(res.body().string(), listType);
            }
        } catch (IOException e) {
            Log.e(TAG, "fetchActiveProfiles failed: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public AppProfile fetchProfile(String packageName) {
        try {
            Request req = new Request.Builder()
                .url(BASE_URL + "/profiles/" + packageName).build();
            try (Response res = http.newCall(req).execute()) {
                if (!res.isSuccessful() || res.body() == null) return null;
                return gson.fromJson(res.body().string(), AppProfile.class);
            }
        } catch (IOException e) {
            Log.e(TAG, "fetchProfile failed: " + e.getMessage());
            return null;
        }
    }

    // ── Usage ──────────────────────────────────────────────────────────────────


    public Map<String, Object> getUsageToday(String packageName) {
        try {
            Request req = new Request.Builder()
                .url(BASE_URL + "/usage/today/" + packageName).build();
            try (Response res = http.newCall(req).execute()) {
                if (!res.isSuccessful() || res.body() == null) return Map.of();
                Type mapType = new TypeToken<Map<String, Object>>(){}.getType();
                return gson.fromJson(res.body().string(), mapType);
            }
        } catch (IOException e) {
            Log.e(TAG, "getUsageToday failed: " + e.getMessage());
            return Map.of();
        }
    }

    public void reportSession(UsageSession session) {
        try {
            String json = gson.toJson(session);
            RequestBody body = RequestBody.create(json, JSON);
            Request req = new Request.Builder()
                .url(BASE_URL + "/usage/session").post(body).build();
            http.newCall(req).execute().close();
        } catch (IOException e) {
            Log.e(TAG, "reportSession failed: " + e.getMessage());
        }
    }


    public void reportResistedUrge(String packageName) {
        try {
            Map<String, String> payload = new HashMap<>();
            payload.put("packageName", packageName);
            RequestBody body = RequestBody.create(gson.toJson(payload), JSON);
            Request req = new Request.Builder()
                .url(BASE_URL + "/usage/resisted").post(body).build();
            http.newCall(req).execute().close();
        } catch (IOException e) {
            Log.e(TAG, "reportResistedUrge failed: " + e.getMessage());
        }
    }

    // ── Bypass ─────────────────────────────────────────────────────────────────

    public boolean checkBypass(String packageName) {
        try {
            Request req = new Request.Builder()
                .url(BASE_URL + "/usage/bypass/" + packageName).build();
            try (Response res = http.newCall(req).execute()) {
                if (!res.isSuccessful() || res.body() == null) return false;
                Type mapType = new TypeToken<Map<String, Boolean>>(){}.getType();
                Map<String, Boolean> data = gson.fromJson(res.body().string(), mapType);
                return Boolean.TRUE.equals(data.get("active"));
            }
        } catch (IOException e) {
            Log.e(TAG, "checkBypass failed: " + e.getMessage());
            return false;
        }
    }
}
