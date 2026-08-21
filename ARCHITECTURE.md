# 🏛️ Architecture & System Design — My Tasks

This document describes the technical architecture, data model, component boundaries, and subsystem interactions of the **My Tasks (کارهای من)** Android application.

---

## 📐 High-Level Architecture Overview

The application follows Google's recommended **Clean Architecture / MVVM** pattern with unidirectional data flow (UDF):

```
┌─────────────────────────────────────────────────────────────┐
│                       UI Layer                              │
│  - MainTodoScreen (Jetpack Compose + Glassmorphism UI)      │
│  - TaskBottomSheet / ShamsiDatePicker / TimePicker          │
│  - ReminderManagementScreen / SettingsBottomSheet          │
│  - MagicAiFab / AiTaskBottomSheet (Gemini Task Parser)      │
└──────────────────────────────┬──────────────────────────────┘
                               │ StateFlow (Observables) / Actions
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                    ViewModel Layer                          │
│  - TodoViewModel (StateFlow holders, coroutine scopes)      │
│  - Backup/Restore serialization (Moshi JSON Engine)         │
└──────────────────────────────┬──────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                   Repository Layer                          │
│  - TodoRepository (Coordinates Room DAO & System Services)  │
└──────────────┬──────────────────────────────┬───────────────┘
               │                              │
               ▼                              ▼
┌──────────────────────────────┐ ┌─────────────────────────────┐
│        Room Database         │ │      System Services        │
│  - AppDatabase (v3)          │ │  - AlarmManager (Exact)     │
│  - TodoDao                   │ │  - ToneGenerator Audio      │
│  - Category/Task/Subtask     │ │  - Gemini Generative AI     │
└──────────────────────────────┘ └─────────────────────────────┘
```

---

## 🗄️ Database & Domain Entities

### 1. `Category` Entity
* **Table:** `categories`
* **Fields:** `id` (PK), `name` (Unique Index), `colorHex`, `isDefault`, `orderIndex` (v3).

### 2. `Task` Entity
* **Table:** `tasks`
* **Fields:** `id` (PK), `title`, `description`, `categoryId` (Index), `isCompleted`, `reminderTime` (Epoch Millis), `repeatType` (e.g. `none`, `daily`, `every_other_day:X`, `weekly`), `createdAt`.

### 3. `Subtask` Entity
* **Table:** `subtasks`
* **Fields:** `id` (PK), `taskId` (Index), `title`, `isCompleted`.

---

## ⏰ Alarm & Background Subsystem

```
User creates/updates Task with reminderTime
               │
               ▼
   ReminderScheduler.schedule()
               │
               ▼
   AlarmManager.setExactAndAllowWhileIdle()
               │ (Fires at reminderTime)
               ▼
   ReminderReceiver (BroadcastReceiver)
   ├─► ToneGenerator Alert Tone
   ├─► NotificationManager (High Importance Channel)
   │     ├─ Action "Done" ──► NotificationActionReceiver (Marks Task Completed)
   │     └─ Action "Snooze" ─► NotificationActionReceiver (+10 min rescheduling)
   └─► If Recurring: Calculates next time & updates Room + reschedules
```

* **Reboot Resilience:** [`BootReceiver.kt`](file:///e:/programming/todoapp/app/src/main/java/com/example/util/BootReceiver.kt) catches `BOOT_COMPLETED` and `QUICKBOOT_POWERON` to re-register all upcoming active alarms in Room.

---

## 📅 Solar Hijri (Jalali) Calendar Engine

* **File:** [`JalaliCalendar.kt`](file:///e:/programming/todoapp/app/src/main/java/com/example/util/JalaliCalendar.kt)
* **Conversion Arithmetic:** Self-contained Gregorian $\leftrightarrow$ Jalali conversions with 33-year cycle leap-year calculations.
* **Localization Support:** Dual calendar rendering (Shamsi for `fa`, Gregorian for `en`), localized weekday/month strings, and numeral conversions (`toPersianDigits()`).

---

## 🎨 Glassmorphism & UI Design System

* **File:** [`GlassmorphismModifier.kt`](file:///e:/programming/todoapp/app/src/main/java/com/example/ui/GlassmorphismModifier.kt)
* **Mesh Gradients:** Dynamic 3-circle radial gradients drawn on `Canvas` behind the main viewport.
* **Frosted Glass Cards:** Semi-transparent background alphas (`0.65f` dark / `0.75f` light), dual-tone gradient borders, and soft ambient drop shadows.
