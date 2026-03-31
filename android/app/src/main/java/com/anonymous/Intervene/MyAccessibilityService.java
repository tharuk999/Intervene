package com.anonymous.Intervene;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;

public class MyAccessibilityService extends AccessibilityService {
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
    }

    @Override
    public void onInterrupt() {
    }

    @Override
    public void onServiceConnected() {
        // Set the type of events that this service wants to listen to. Others
        // aren't passed to this service.
        info.eventTypes = AccessibilityEvent.TYPE_VIEW_CLICKED |
                AccessibilityEvent.TYPE_VIEW_FOCUSED;

        // If you only want this service to work with specific apps, set their
        // package names here. Otherwise, when the service is activated, it listens
        // to events from all apps.
        info.packageNames = new String[]
                {"com.example.android.myFirstApp", "com.example.android.mySecondApp"};

        // Set the type of feedback your service provides.
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_SPOKEN;

        // Default services are invoked only if no package-specific services are
        // present for the type of AccessibilityEvent generated. This service is
        // app-specific, so the flag isn't necessary. For a general-purpose service,
        // consider setting the DEFAULT flag.

        // info.flags = AccessibilityServiceInfo.DEFAULT;

        info.notificationTimeout = 100;

        this.setServiceInfo(info);

    }
}