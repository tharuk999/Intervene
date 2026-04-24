package com.anonymous.Intervene;

import android.content.Intent;
import android.provider.Settings;
import android.util.Log;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import com.interactout.service.InteractOutAccessibilityService;

public class AccessibilityModule extends ReactContextBaseJavaModule {

    private static final String TAG = "InteractOut.Module";

    public AccessibilityModule(ReactApplicationContext reactContext) {
        super(reactContext);
        Log.d(TAG, "AccessibilityModule initialized");
    }

    @Override
    public String getName() {
        return "AccessibilityModule";
    }

    @ReactMethod
    public void isServiceEnabled(Promise promise) {
        String service = getReactApplicationContext().getPackageName()
                + "/" + InteractOutAccessibilityService.class.getCanonicalName();

        String enabledServices = Settings.Secure.getString(
                getReactApplicationContext().getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        );

        boolean enabled = false;
        if (enabledServices != null) {
            for (String s : enabledServices.split(":")) {
                if (s.equalsIgnoreCase(service)) {
                    enabled = true;
                    break;
                }
            }
        }

        Log.d(TAG, "isServiceEnabled: " + enabled);
        promise.resolve(enabled);
    }

    @ReactMethod
    public void openAccessibilitySettings() {
        Log.d(TAG, "openAccessibilitySettings called");
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getReactApplicationContext().startActivity(intent);
    }

    @ReactMethod
    public void ping(Promise promise) {
        Log.d(TAG, "ping called from JS");
        promise.resolve("pong");
    }

    @ReactMethod
    public void addListener(String eventName) {}

    @ReactMethod
    public void removeListeners(Integer count) {}
}