package com.anonymous.Intervene;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.accessibilityservice.AccessibilityGestureEvent;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.view.accessibility.AccessibilityRecord;
import android.util.Log;

public class MyAccessibilityService extends AccessibilityService {

    private static final String TAG = "InteractOut.AccessibilityService";

    @Override
    public void onCreate() {
        Log.d(TAG, "Accessibility Service Created");
    }
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
//        int gestureId = gestureEvent.getGestureId();
//        String gesture = gestureEvent.gestureIdToString(gestureId);
        Log.d(TAG, "Event: " + AccessibilityEvent.eventTypeToString(event.getEventType()));
        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_SCROLLED){
            Log.d(TAG, "Type of scrolling: " + "x: " + event.getScrollDeltaX() + " y " + event.getScrollDeltaY());
        }
    }

    @Override
    public void onInterrupt() {
    }

    @Override
    public void onServiceConnected() {
        // Set the type of events that this service wants to listen to. Others
        // aren't passed to this service.
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();

        info.eventTypes =
                AccessibilityEvent.TYPE_VIEW_CLICKED |
                        AccessibilityEvent.TYPE_VIEW_FOCUSED |
                        AccessibilityEvent.TYPE_VIEW_SCROLLED |
                        AccessibilityEvent.TYPE_VIEW_LONG_CLICKED |
                        AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED |
                        AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED |
                        AccessibilityEvent.TYPE_VIEW_TEXT_TRAVERSED_AT_MOVEMENT_GRANULARITY |
                        AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED |
//                        AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED |
                        AccessibilityEvent.TYPE_WINDOWS_CHANGED |

                        // Notification
                        AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED |

                        // Exploration / hover
                        AccessibilityEvent.TYPE_VIEW_HOVER_ENTER |
                        AccessibilityEvent.TYPE_VIEW_HOVER_EXIT |
                        AccessibilityEvent.TYPE_VIEW_TARGETED_BY_SCROLL |

                        // Touch interaction
                        AccessibilityEvent.TYPE_TOUCH_INTERACTION_START |
                        AccessibilityEvent.TYPE_TOUCH_INTERACTION_END |

                        // Touch exploration gestures
                        AccessibilityEvent.TYPE_TOUCH_EXPLORATION_GESTURE_START |
                        AccessibilityEvent.TYPE_TOUCH_EXPLORATION_GESTURE_END |

                        // Gesture detection
                        AccessibilityEvent.TYPE_GESTURE_DETECTION_START |
                        AccessibilityEvent.TYPE_GESTURE_DETECTION_END;

        // If you only want this service to work with specific apps, set their
        // package names here. Otherwise, when the service is activated, it listens
        // to events from all apps.

        // Set the type of feedback your service provides.
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_SPOKEN;

        // Default services are invoked only if no package-specific services are
        // present for the type of AccessibilityEvent generated. This service is
        // app-specific, so the flag isn't necessary. For a general-purpose service,
        // consider setting the DEFAULT flag.
         info.flags = AccessibilityServiceInfo.FLAG_SERVICE_HANDLES_DOUBLE_TAP;

//         info.flags = AccessibilityServiceInfo.FLAG_REQUEST_TOUCH_EXPLORATION_MODE | AccessibilityServiceInfo.FLAG_SERVICE_HANDLES_DOUBLE_TAP;
//        getServiceInfo().flags = AccessibilityServiceInfo.FLAG_REQUEST_TOUCH_EXPLORATION_MODE;


        info.notificationTimeout = 100;

        this.setServiceInfo(info);

    }

    @Override
    public boolean onGesture (AccessibilityGestureEvent gestureEvent){
        int gestureId = gestureEvent.getGestureId();
        String gesture = gestureEvent.gestureIdToString(gestureId);
        Log.d(TAG, "Gesture captured: " + gesture);
        return true;
    }
}