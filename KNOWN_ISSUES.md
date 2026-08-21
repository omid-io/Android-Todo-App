# ⚠️ Known Issues & Technical Debt — My Tasks

This document tracks known limitations, platform-specific behaviors, and technical debt items.

---

## 1. Magic AI FAB Integration State
* **Category:** UI / Feature Connection
* **Description:** [`MagicAiFab.kt`](file:///e:/programming/todoapp/app/src/main/java/com/example/ui/MagicAiFab.kt) and [`AiTaskBottomSheet.kt`](file:///e:/programming/todoapp/app/src/main/java/com/example/ui/AiTaskBottomSheet.kt) are fully implemented with Gemini prompt handlers, but [`MainTodoScreen.kt`](file:///e:/programming/todoapp/app/src/main/java/com/example/ui/MainTodoScreen.kt) currently renders a standard floating action button.
* **Workaround / Resolution Plan:** Replace the default FAB box in `MainTodoScreen.kt` with `MagicAiFab` so long-pressing triggers the natural-language task parser.

---

## 2. Android 14+ (API 34+) Exact Alarm Permission
* **Category:** Background Service / OS Permissions
* **Description:** On Android 14+, apps targeting API 34+ do not automatically receive `SCHEDULE_EXACT_ALARM` unless pre-granted or requested via system settings.
* **Current Behavior:** [`ReminderScheduler.kt`](file:///e:/programming/todoapp/app/src/main/java/com/example/util/ReminderScheduler.kt) catches `SecurityException` and falls back to inexact `alarmManager.set(...)`.
* **Resolution Plan:** Check `alarmManager.canScheduleExactAlarms()` and guide users to the Android settings toggle if denied.

---

## 3. Aggressive OEM Battery Killers (Xiaomi MIUI/HyperOS, Huawei, Oppo)
* **Category:** OS Customization
* **Description:** Aggressive power management on custom Android skins can prevent background `BroadcastReceivers` from firing.
* **Current Mitigation:** The in-app Xiaomi Troubleshooting Guide in [`ReminderManagementScreen.kt`](file:///e:/programming/todoapp/app/src/main/java/com/example/ui/ReminderManagementScreen.kt) gives users clear steps to enable "Autostart" and set battery saver to "No Restrictions".
