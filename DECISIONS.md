# 🏛️ Architectural Decision Records (ADRs) — My Tasks

This file records all significant architectural decisions, their context, rationale, and consequences.

---

## ADR-001: 100% Jetpack Compose Architecture
* **Status:** Accepted
* **Context:** Needed modern, declarative UI with smooth 60FPS animations, reactive state updates, and dynamic glassmorphism without XML overhead.
* **Decision:** Implement all screens, dialogs, bottom sheets, and design system components entirely using Jetpack Compose with Material 3.
* **Consequences:** Simplified single-activity architecture, clean separation of UI from logic, eliminated layout inflation delays.

---

## ADR-002: In-House Pure Kotlin Jalali Calendar Engine
* **Status:** Accepted
* **Context:** Persian users require accurate Solar Hijri dates and month names. Third-party Java calendar libraries often introduce large method counts, outdated APIs, or timezone quirks.
* **Decision:** Implement a lightweight, zero-dependency Gregorian $\leftrightarrow$ Jalali arithmetic engine in [`JalaliCalendar.kt`](file:///e:/programming/todoapp/app/src/main/java/com/example/util/JalaliCalendar.kt) with leap-year 33-year cycle accuracy.
* **Consequences:** Zero external binary bloat, O(1) mathematical conversions, seamless fallback to Gregorian when English locale is active.

---

## ADR-003: ToneGenerator for Zero-Latency Haptic-Audio Feedback
* **Status:** Accepted
* **Context:** Loading and releasing MediaPlayer or SoundPool for small button taps, check completions, and delete sounds adds unnecessary CPU wakeups and memory overhead.
* **Decision:** Utilize Android's low-level `android.media.ToneGenerator` in [`SoundManager.kt`](file:///e:/programming/todoapp/app/src/main/java/com/example/util/SoundManager.kt).
* **Consequences:** Instantaneous response (<5ms), zero asset file size, robust fallback across Android 7 to Android 15+.

---

## ADR-004: Exact Alarms with BroadcastReceiver Architecture
* **Status:** Accepted
* **Context:** Todo reminders must trigger precisely at the scheduled second, even under Android Doze mode.
* **Decision:** Use `AlarmManager.setExactAndAllowWhileIdle` paired with a high-priority `NotificationChannel`, `PendingIntent` actions for "Done"/"Snooze", and a `BootReceiver` for reboot persistence.
* **Consequences:** Reliable notifications; requires `SCHEDULE_EXACT_ALARM` / `USE_EXACT_ALARM` permissions and user awareness on aggressive battery managers (MIUI/HyperOS).
