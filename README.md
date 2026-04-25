# Intervene

> A research-backed Android application that introduces intentional friction into habitual social media use, built on the **InteractOut** framework.

---

## Overview

**Intervene** is a digital wellbeing app that applies evidence-based *interaction-level* friction to reduce mindless phone use. Rather than blocking apps outright, Intervene subtly modifies how you physically interact with them, delaying taps, reversing swipes, requiring multi-finger gestures, making habitual scrolling conscious and effortful.

The app is grounded in the **InteractOut** research paper, which demonstrates that micro-friction interventions at the gesture layer are more effective at promoting reflective pausing than blunt time-limit blockers.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Mobile Frontend | React Native (Expo) |
| Styling | NativeWind (Tailwind CSS for React Native) |
| Navigation | Expo Router (file-based) |
| Local Storage | AsyncStorage |
| Background Sync | Expo Background Task + Task Manager |
| Native Bridge | Android NativeModules (`ProfilesModule`) |

---

## Features

- **App Directory** - Lists all installed apps with today's usage time; highlights apps with active friction profiles
- **Intervention Setup** - Per-app configuration of 8 gesture-level interventions with adjustable intensity sliders
- **Friction Types:**
    - `TAP_DELAY` - Adds a configurable delay before a tap registers
    - `TAP_PROLONG` - Requires holding for a set duration to trigger
    - `TAP_SHIFT` - Offsets the registered tap location
    - `TAP_DOUBLE` - Requires double-tap for a single interaction
    - `SWIPE_DELAY` - Introduces pauses between consecutive swipes
    - `SWIPE_DECELERATE` - Increases scrolling friction at high velocity
    - `SWIPE_REVERSE` - Reverses swipe direction
    - `SWIPE_MULTI_FINGER` - Requires two-finger scrolling
- **Intention Summary** - Live friction coefficient display per app profile
- **Usage Dashboard** - Daily screen time, resisted urges, and active friction app count
- **Settings** - Appearance, font size, mindful alerts, focus blocks, and privacy controls
- **Background Sync** - Queued usage sessions synced to the backend via background task

---

## Prerequisites

Before running the app, ensure the following are installed:

- [Node.js](https://nodejs.org/) (v18 or later recommended)
- [Android Studio](https://developer.android.com/studio) with an AVD (Android Virtual Device) configured
- [Java JDK 17+](https://adoptium.net/) (required by the Android build toolchain)
- Android Emulator running **API 26+**

> **Note:** The app targets Android only. The native modules and accessibility services are Android-specific and will not function on iOS or web.

---

## Getting Started

### 1. Clone the repository

```powershell
git clone <repository-url>
cd intervene
```

### 2. Install dependencies

```powershell
npm install
```

### 3. Launch the app on the Android emulator

Start your Android Studio emulator, then run:

```powershell
npx expo run:android
```

Expo will build the native Android project and deploy it to the running emulator.

---

## Project Structure

```
├── app/
│   ├── _layout.tsx              # Root layout + background task registration
│   ├── index.tsx                # App directory screen (home)
│   ├── interventions.tsx        # Interventions list screen
│   ├── intervention-setup.tsx   # Re-export for Expo Router
│   ├── settings.tsx             # Re-export for Expo Router
│   └── global.css               # Tailwind base imports
│
├── components/
│   ├── InterventionsSetupScreen.tsx   # Per-app friction configuration UI
│   ├── SettingsScreen.tsx             # App settings UI
│   ├── navbar.tsx                     # Bottom navigation bar
│   ├── themed-text.tsx                # Theme-aware Text component
│   ├── themed-view.tsx                # Theme-aware View component
│   └── ...
│
├── hooks/
│   ├── useInterveneApi.ts        # REST API client for all backend endpoints
│   ├── use-color-scheme.ts       # Native color scheme hook
│   ├── use-color-scheme.web.ts   # Web-safe color scheme hook (hydration-aware)
│   └── use-theme-color.ts        # Theme color resolver
│
└── android/                      # Native Android project (managed by Expo)
    └── app/src/main/java/
        ├── com/anonymous/Intervene/
        │   ├── MainActivity.kt                  # React Native activity entry point
        │   ├── MainApplication.kt               # Package registration
        │   ├── ProfilesModule.java              # NativeModule: app list + profile CRUD
        │   ├── ProfilesPackage.java             # Package wrapper for ProfilesModule
        │   ├── UsageStatsModule.java            # NativeModule: usage stats + dashboard
        │   ├── UsageStatsPackage.java           # Package wrapper for UsageStatsModule
        │   ├── AccessibilityModule.java         # NativeModule: accessibility service control
        │   ├── AccessibilityPackage.java        # Package wrapper for AccessibilityModule
        │   └── MyAccessibilityService.java      # Accessibility service (event logging)
        │
        └── com/interactout/
            ├── model/
            │   ├── AppProfile.java              # Profile data model
            │   ├── InterventionConfig.java      # Per-intervention config model
            │   ├── InterventionType.java        # Enum of all 8 intervention types
            │   └── UsageSession.java            # Usage session data model
            ├── api/
            │   └── ApiClient.java
            └── service/
                ├── InteractOutAccessibilityService.java   # Core a11y service: app tracking + intervention activation
                ├── GestureInterceptorService.java         # Gesture proxy: applies friction to taps/swipes
                └── BypassNotificationManager.java         # Bypass notification + broadcast receiver
```

---

## Native Android Layer

The `android/` directory contains the full native Android project built alongside the React Native frontend. It is **not** auto-generated boilerplate, it holds the core intervention logic that makes Intervene work.

### NativeModules (JS <-> Android bridge)

Three custom `ReactContextBaseJavaModule` classes expose Android APIs to the JavaScript layer:

| Module | JS Access | Responsibilities |
|---|---|---|
| `ProfilesModule` | `NativeModules.ProfilesModule` | Lists installed apps with usage time, loads/saves friction profiles via `SharedPreferences` |
| `UsageStatsModule` | `NativeModules.UsageStatsModule` | Reads `UsageStatsManager`, builds dashboard data, checks/opens accessibility permissions |
| `AccessibilityModule` | `NativeModules.AccessibilityModule` | Checks accessibility service status, opens accessibility settings |

All three modules require the full native Android build (`npx expo run:android`) and will **not** work with Expo Go.

### Data Models (`com.interactout.model`)

- `AppProfile` - Package name, app name, enabled state, daily limit, and list of `InterventionConfig`
- `InterventionConfig` - Intervention type, enabled flag, and intensity (0.0–1.0)
- `InterventionType` - Enum of all 8 intervention types
- `UsageSession` - Package name, duration, and timestamp for session reporting

### Permissions Required

The following permissions must be granted by the user at runtime or via system settings:

| Permission | Purpose |
|---|---|
| `PACKAGE_USAGE_STATS` | Read per-app usage time via `UsageStatsManager` |
| `BIND_ACCESSIBILITY_SERVICE` | Run `InteractOutAccessibilityService` and `GestureInterceptorService` |
| `SYSTEM_ALERT_WINDOW` | Display overlay prompts (bypass flow) |

---

## Research Foundation

Intervene is based on **InteractOut**, a research framework exploring interaction-level interventions as a means of reducing problematic smartphone use. Unlike screen time blockers, InteractOut introduces friction at the gesture layer, making the physical act of scrolling and tapping slightly harder, to prompt conscious reflection without outright restriction.

Key findings from the paper inform the intervention types and intensity calibration implemented in this app.

---

## Known Limitations

- Android only, native module is not cross-platform
- Real device usage requires manually updating the `BASE_URL` constant to the host machine's LAN IP
- Background sync depends on Android's background task scheduling and may be deferred by the OS under battery optimization

---

## License

This project was developed for academic research purposes in conjunction with the InteractOut research paper. See `LICENSE` for details.