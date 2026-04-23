# Setup

   ```bash
   .\start-backend.ps1  # run backend
   
   npm install          # install dependencies 
   
   npx expo start       # run frontend
   # OR 
   npx expo run:android # run frontend through android emulator
   ```

# Intervene Frontend

This is an [Expo](https://expo.dev) project created with [`create-expo-app`](https://www.npmjs.com/package/create-expo-app).

   
# Intervene Backend

Java backend for the Intervene app, implementing all 8 gesture intervention techniques
from the CHI '24 paper "InteractOut: Leveraging Interaction Proxies as Input Manipulation
Strategies for Reducing Smartphone Overuse".

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                         REACT NATIVE (Expo)                     │
│   index.tsx · interventions.tsx · InterventionsSetupScreen.tsx  │
│                     useInterveneApi.ts (hook)                 │
└──────────────────────┬──────────────────────────────────────────┘
                       │  HTTP REST  (port 8080)
┌──────────────────────▼──────────────────────────────────────────┐
│                    SPRING BOOT BACKEND                          │
│  ProfileController  ·  UsageController                         │
│  ProfileService     ·  UsageService                            │
│  AppProfileEntity   ·  UsageSessionEntity                      │
│  H2 Database (dev)  →  MySQL (prod)                            │
└──────────────────────┬──────────────────────────────────────────┘
                       │  HTTP REST (polling + reporting)
┌──────────────────────▼──────────────────────────────────────────┐
│               ANDROID ACCESSIBILITY SERVICE                     │
│  InterveneAccessibilityService (foreground app detection)    │
│  GestureInterceptorService       (proxy layer = Figure 3)      │
│  BypassNotificationManager       (Figure 4 bypass menu)        │
│  ApiClient                       (OkHttp → Spring Boot)        │
└─────────────────────────────────────────────────────────────────┘
         ↕ dispatchGesture() API
    Real apps (Instagram, TikTok, YouTube, etc.)
```

---

## File Structure

Place files exactly as shown. Backend is fully separate from the Expo frontend.

```
Intervene/
│
├── app/                          ← EXISTING: Expo React Native frontend
│   ├── index.tsx
│   ├── interventions.tsx
│   ├── intervention-setup.tsx
│   ├── settings.tsx
│   └── _layout.tsx
│
├── components/                   ← EXISTING: React Native components
│   ├── navbar.tsx
│   ├── InterventionsSetupScreen.tsx
│   └── SettingsScreen.tsx
│
├── hooks/                        ← NEW: Add this hook
│   └── useInterveneApi.ts      ← connects React Native → Spring Boot
│
├── backend/          ← NEW: entire backend lives here
│   │
│   ├── springboot/               ← Spring Boot REST API
│   │   ├── pom.xml
│   │   └── src/main/
│   │       ├── java/com/interactout/
│   │       │   ├── InteractOutApplication.java
│   │       │   ├── controller/
│   │       │   │   ├── ProfileController.java   GET/POST/PUT/DELETE /api/profiles
│   │       │   │   └── UsageController.java     GET/POST /api/usage/*
│   │       │   ├── service/
│   │       │   │   ├── ProfileService.java      CRUD + intervention logic
│   │       │   │   └── UsageService.java        usage tracking, bypass, dashboard
│   │       │   ├── model/
│   │       │   │   ├── InterventionType.java    enum: all 8 types
│   │       │   │   ├── AppProfileEntity.java    JPA entity (app_profiles table)
│   │       │   │   ├── InterventionEntity.java  JPA entity (interventions table)
│   │       │   │   └── UsageSessionEntity.java  JPA entity (usage_sessions table)
│   │       │   └── repository/
│   │       │       ├── AppProfileRepository.java
│   │       │       └── UsageSessionRepository.java
│   │       └── resources/
│   │           └── application.properties
│   │
│   └── android/                  ← Android native module (Accessibility Service)
│       └── app/src/main/
│           ├── AndroidManifest/
│           │   └── AndroidManifest.xml         permissions + service declarations
│           ├── res/xml/
│           │   └── accessibility_service_config.xml
│           └── java/com/interactout/
│               ├── service/
│               │   ├── InteractOutAccessibilityService.java  ← CORE: proxy layer
│               │   ├── GestureInterceptorService.java        ← all 8 interventions
│               │   └── BypassNotificationManager.java        ← Figure 4 bypass menu
│               ├── api/
│               │   └── ApiClient.java                        ← OkHttp → Spring Boot
│               └── model/
│                   ├── InterventionType.java   (mirrors Spring Boot enum)
│                   ├── InterventionConfig.java
│                   ├── AppProfile.java
│                   └── UsageSession.java
```

---

## REST API Reference

All endpoints at `http://localhost:8080/api`

### Profiles

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET    | `/profiles` | All profiles (Android polls on startup) |
| GET    | `/profiles/active` | Only enabled profiles |
| GET    | `/profiles/{pkg}` | Single profile |
| POST   | `/profiles` | Create / update profile from React Native UI |
| PUT    | `/profiles/{pkg}/enabled` | Toggle app on/off |
| PUT    | `/profiles/{pkg}/interventions/{type}/intensity` | Update slider value |
| DELETE | `/profiles/{pkg}` | Remove profile |

### Usage & Dashboard

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST   | `/usage/session` | Report session end (Android → backend) |
| POST   | `/usage/resisted` | Record resisted urge |
| GET    | `/usage/today/{pkg}` | usedMs + limitMs (Android checks before intervening) |
| POST   | `/usage/bypass` | Grant 1-min or 15-min bypass |
| GET    | `/usage/bypass/{pkg}` | Check if bypass is active |
| GET    | `/usage/dashboard` | All home screen data in one call |
| GET    | `/usage/history/weekly` | Last 7 days for charts |

---

## The 8 Interventions (from paper)

| InterventionType | Paper Name | What it does | Intensity mapping |
|-----------------|-----------|--------------|------------------|
| `TAP_DELAY` | Tap/Swipe Delay | Postpone tap dispatch | 0→0ms, 1→800ms |
| `TAP_PROLONG` | Tap Prolong | Require long-press to register | 0→0ms, 1→1500ms threshold |
| `TAP_SHIFT` | Tap Shift | Offset tap location by (dx,dy) | 0→0px, 1→60px |
| `TAP_DOUBLE` | Tap Double | Require double-tap for single effect | boolean |
| `SWIPE_DELAY` | Swipe Delay | Postpone swipe dispatch | 0→0ms, 1→600ms |
| `SWIPE_DECELERATE` | Swipe Deceleration | Multiply swipe duration | 0→1x, 1→4x |
| `SWIPE_REVERSE` | Swipe Reverse | Reverse trajectory direction | boolean |
| `SWIPE_MULTI_FINGER` | Swipe Multiple Fingers | Require N fingers | 0→2, 1→4 fingers |

---

## Setup Instructions (Windows PowerShell)

### 1. Start the Spring Boot backend

```powershell
cd interactout-backend\springboot
.\mvnw.cmd spring-boot:run
# Server starts at http://localhost:8080
# H2 console: http://localhost:8080/h2-console
```

If no mvnw exists yet:
```powershell
# Install Maven first, then:
mvn spring-boot:run
```

### 2. Integrate the React Native hook

Copy `hooks/useInteractOutApi.ts` into your Expo project's `hooks/` folder.

Use in any screen:
```tsx
import { useInteractOutApi } from '@/hooks/useInteractOutApi';

export default function HomeScreen() {
  const { dashboard, loading } = useInteractOutApi();

  // dashboard.totalScreenTimeMs  → "Screen Intent" card
  // dashboard.resistedUrges      → "RESISTED URGES" card
  // dashboard.activeFrictionApps → "FRICTION ACTIVE" card
  // dashboard.percentChangeVsYesterday → "-22% vs yesterday" badge
}
```

### 3. Android Accessibility Service setup

The Android module must be integrated into your Expo project as a **bare workflow** or
**native module**. Steps:

```powershell
# Eject to bare workflow (if not already)
npx expo prebuild

# Copy android files into:
# android/app/src/main/java/com/interactout/
# android/app/src/main/res/xml/accessibility_service_config.xml
# Merge AndroidManifest.xml entries into android/app/src/main/AndroidManifest.xml

# Add OkHttp + Gson to android/app/build.gradle:
# implementation 'com.squareup.okhttp3:okhttp:4.12.0'
# implementation 'com.google.code.gson:gson:2.10.1'

# Build and run
npx expo run:android
```

After install, user must enable the service:
**Settings → Accessibility → Downloaded apps → InteractOut → Enable**

### 4. Real device vs emulator

| Scenario | BASE_URL in ApiClient.java and useInteractOutApi.ts |
|----------|------------------------------------------------------|
| Android Emulator | `http://10.0.2.2:8080/api` |
| Real device (same WiFi) | `http://YOUR_PC_LAN_IP:8080/api` |
| Production | `https://your-domain.com/api` |

Find your LAN IP: `ipconfig` → IPv4 Address under your WiFi adapter.

---

## Dynamic Intervention Intensity (Paper Section 3.2)

The paper describes interventions that **ramp up gradually** as usage approaches the limit.
This is implemented in `InteractOutAccessibilityService.activateForPackage()`:

```
intensity = usedMs / limitMs   (0.0 at session start → 1.0 at limit)
```

This intensity is passed to `GestureInterceptorService.applyInterventionsWithIntensity()`,
which scales every intervention parameter proportionally. Users notice very subtle friction
at first, increasing as they approach their set limit.

---

## Database Schema (auto-created by Hibernate)

```sql
-- app_profiles
CREATE TABLE app_profiles (
    package_name  VARCHAR(255) PRIMARY KEY,
    app_name      VARCHAR(255),
    enabled       BOOLEAN,
    daily_limit_ms BIGINT
);

-- interventions
CREATE TABLE interventions (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    package_name VARCHAR(255) REFERENCES app_profiles(package_name),
    type         VARCHAR(50),   -- InterventionType enum name
    enabled      BOOLEAN,
    intensity    FLOAT
);

-- usage_sessions
CREATE TABLE usage_sessions (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    package_name   VARCHAR(255),
    duration_ms    BIGINT,
    timestamp_ms   BIGINT,
    session_date   DATE,
    resisted_urges INT,
    INDEX idx_pkg_date (package_name, session_date)
);
```