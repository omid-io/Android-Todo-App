# 📍 Project State — My Tasks (کارهای من)

## 📌 Current Version: `1.0.3` (Build Code: 4)

**Status:** Stable Release • Standalone Antigravity Development Mode

---

## ✅ Completed Capabilities

1. **Core Task Management:**
   - [x] Create, edit, delete tasks with title, description, category, and reminder timestamp.
   - [x] Hierarchical subtasks with progress indicator and collapsible activity chains.
   - [x] Task ordering: newest tasks appended (`createdAt ASC`).
   - [x] Completed tasks section with collapsible header and counter badge.
   - [x] Quick subtask input via keyboard Enter/Done action.
   - [x] Subtask expansion state persistence across LazyColumn scroll (`rememberSaveable`).

2. **Category Subsystem:**
   - [x] Custom color palettes (11 curated Material colors).
   - [x] Category reordering (`orderIndex` with Move Up / Down).
   - [x] Rename & delete category with cascaded task and orphan subtask removal (`deleteSubtasksByCategoryId`).
   - [x] Uncategorized tasks handling (`id: -1`).

3. **Reminders & Notifications:**
   - [x] Exact alarms via `AlarmManager.setExactAndAllowWhileIdle`.
   - [x] Recurrence engine (Daily, Weekly, Custom Every X Days).
   - [x] Notification action buttons: "Done" and "Snooze (+10m)".
   - [x] `BootReceiver` for alarm re-registration after system reboot.
   - [x] Dedicated reminder management screen with Xiaomi autostart & battery guide.

4. **Localization & Calendars:**
   - [x] Shamsi (Jalali) solar calendar date picker covering years 1400–1420.
   - [x] Full Persian digits formatting (`۰-۹`).
   - [x] English / Persian dynamic runtime language switching (`RTL / LTR`).

5. **Audio & Backup Utility:**
   - [x] Zero-latency sound feedback via `ToneGenerator` ([`SoundManager.kt`](file:///e:/programming/todoapp/app/src/main/java/com/example/util/SoundManager.kt)).
   - [x] JSON Backup export & SAF-based restore in Settings.

---

## 🚀 Active Roadmap & Upcoming Milestones (v1.1.0)

- [ ] **Android 14+ Alarm Permission Prompt:** Add graceful prompt for `canScheduleExactAlarms()` if revoked by user.
- [ ] **Glance Interactive Home Widget:** Home-screen widget for checking off daily tasks.
- [ ] **Swipe Actions:** Swipe-to-complete and swipe-to-snooze gestures on task rows.
