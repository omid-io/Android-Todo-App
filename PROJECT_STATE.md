# 📍 Project State — My Tasks (کارهای من)

## 📌 Current Version: `1.0.1` (Build Code: 2)

**Status:** Stable Release • Standalone Antigravity Development Mode

---

## ✅ Completed Capabilities

1. **Core Task Management:**
   - [x] Create, edit, delete tasks with title, description, category, and reminder timestamp.
   - [x] Hierarchical subtasks with progress indicator and collapsible activity chains.
   - [x] Task ordering: newest tasks appended (`createdAt ASC`).
   - [x] Completed tasks section with collapsible header and counter badge.

2. **Category Subsystem:**
   - [x] Custom color palettes (11 curated Material colors).
   - [x] Category reordering (`orderIndex` with Move Up / Down).
   - [x] Rename & delete category with cascaded task removal.
   - [x] Uncategorized tasks handling (`id: -1`).

3. **Reminders & Notifications:**
   - [x] Exact alarms via `AlarmManager.setExactAndAllowWhileIdle`.
   - [x] Recurrence engine (Daily, Weekly, Custom Every X Days).
   - [x] Notification action buttons: "Done" and "Snooze (+10m)".
   - [x] `BootReceiver` for alarm re-registration after system reboot.
   - [x] Dedicated reminder management screen with Xiaomi autostart & battery guide.

4. **Localization & Calendars:**
   - [x] Shamsi (Jalali) solar calendar date picker with 33-year leap year handling.
   - [x] Full Persian digits formatting (`۰-۹`).
   - [x] English / Persian dynamic runtime language switching (`RTL / LTR`).

5. **AI Integration & Utility:**
   - [x] Gemini AI natural language task parsing module ([`AiManager.kt`](file:///e:/programming/todoapp/app/src/main/java/com/example/util/AiManager.kt)).
   - [x] Zero-latency sound feedback via `ToneGenerator` ([`SoundManager.kt`](file:///e:/programming/todoapp/app/src/main/java/com/example/util/SoundManager.kt)).
   - [x] JSON Backup export & SAF-based restore in Settings.

---

## 🚀 Active Roadmap & Upcoming Milestones (v1.1.0)

- [ ] **Connect Magic AI FAB:** Wire [`MagicAiFab.kt`](file:///e:/programming/todoapp/app/src/main/java/com/example/ui/MagicAiFab.kt) and [`AiTaskBottomSheet.kt`](file:///e:/programming/todoapp/app/src/main/java/com/example/ui/AiTaskBottomSheet.kt) directly into [`MainTodoScreen.kt`](file:///e:/programming/todoapp/app/src/main/java/com/example/ui/MainTodoScreen.kt) with long-press trigger.
- [ ] **Android 14+ Alarm Permission Prompt:** Add graceful prompt for `canScheduleExactAlarms()` if revoked by user.
- [ ] **TDD Unit Testing Suite:** Expand automated unit tests for `JalaliCalendar`, `AiManager` mock responses, and recurrence timestamp calculations.
- [ ] **Room ForeignKey Cascade Enforcement:** Strengthen SQLite foreign keys in schema.
