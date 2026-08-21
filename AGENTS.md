# 🤖 My Tasks — Agent Engineering Guide & Runbook

Welcome to **My Tasks (کارهای من)**. This document contains project-specific architecture rules, CLI runbooks, and coding standards for developing within the Antigravity standalone environment.

---

## 🛠️ Environment & Tooling

- **Language:** Kotlin 2.2.10 (Java 17/21 compatible)
- **UI Framework:** 100% Jetpack Compose (Material 3)
- **Persistence:** AndroidX Room with Coroutines Flow
- **Build System:** Gradle 9.7.0 / Android Gradle Plugin (AGP) 9.2.1
- **Package / Application ID:** `com.mytasks.ai`

---

## ⚡ CLI Build & Test Runbook

When developing without Android Studio, use these Gradle commands from the workspace root (`E:\programming\todoapp`):

### 1. Build Verification
```powershell
# Compile Debug APK
.\gradlew.bat assembleDebug

# Compile Release APK (with R8 Proguard optimizations)
.\gradlew.bat assembleRelease
```

### 2. Running Automated Tests
```powershell
# Run all local JVM Unit Tests & Robolectric Tests
.\gradlew.bat testDebugUnitTest

# Run specific unit test class
.\gradlew.bat testDebugUnitTest --tests "com.example.ExampleUnitTest"
```

### 3. Code Quality & Lint
```powershell
# Run Android Lint analysis
.\gradlew.bat lintDebug
```

---

## 🏗️ Architecture Invariants

1. **State Reactivity:**
   - Always expose Room data streams as `Flow` in [`TodoDao.kt`](file:///e:/programming/todoapp/app/src/main/java/com/example/data/TodoDao.kt) and convert to `StateFlow` in [`TodoViewModel.kt`](file:///e:/programming/todoapp/app/src/main/java/com/example/ui/TodoViewModel.kt) via `stateIn(WhileSubscribed(5000))`.
   - Never mutate state directly in UI composables; dispatch actions to ViewModel functions.

2. **Compose Performance:**
   - Always memoize expensive data structures (e.g. subtask groupings, category task partitioning) with `remember` or `derivedStateOf` before rendering in `LazyColumn`.
   - Avoid creating new object instances or running regex/formatting in composition loops.

3. **Dual Calendar & Digit Localization:**
   - All dates displayed to Persian (`fa`) users must pass through [`JalaliCalendar.kt`](file:///e:/programming/todoapp/app/src/main/java/com/example/util/JalaliCalendar.kt) and [`toPersianDigits()`](file:///e:/programming/todoapp/app/src/main/java/com/example/util/JalaliCalendar.kt).
   - If the device language is English (`en`), fallback cleanly to Gregorian date formats and Latin digits.

4. **Alarms & Background Receivers:**
   - Any modification to task reminder time must be scheduled via [`ReminderScheduler.kt`](file:///e:/programming/todoapp/app/src/main/java/com/example/util/ReminderScheduler.kt).
   - When a task is marked complete or deleted, its pending `AlarmManager` intent must be cancelled immediately.

5. **Secrets & Keys:**
   - Never hardcode Gemini API keys in repository files. Use `.env` or Gradle BuildConfig properties generated via `secrets-gradle-plugin`.
