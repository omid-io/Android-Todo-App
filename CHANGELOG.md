# 📜 Changelog — My Tasks (کارهای من)

All notable changes to the **My Tasks** project will be documented in this file.

---

## [1.0.6] — 2026-08-30
### Added
- **Continuous Spring Drag-and-Drop Reordering:** Category reordering with continuous touch tracking on `☰`, hardware-accelerated spring animations (`Modifier.animateItem`), dynamic elevation shadows, and scaling.
- **Dedicated Edit Category Dialog:** New composable in `CategoryDialog.kt` supporting simultaneous editing of category name and 11-color Material palette with isolated keyed state (`remember(category.id)`).

### Fixed
- **Room Database Race Condition:** Replaced individual category update loops with atomic batch `@Update suspend fun updateCategories(...)` in a single SQLite transaction, preventing intermediate out-of-order Room Flow emissions.
- **Ghost Touch / Mismatched Dialog Trigger:** Fixed category selection race conditions and stale remember states when clicking Edit or Delete right after reordering.
- **Dialog Lifecycle Cleanup:** Sanitized `selectedCategoryForAction` to prevent memory leaks and dialog bleeding.

---

## [1.0.5] — 2026-08-29
### Added
- **Minimal Glassmorphism Redesign:**
  - Unified frosted glass card for frameless task title and auto-expanding notes.
  - Horizontal scrolling category chips (`LazyRow`) with active glow border, color indicators, and `+ New Category` chip.
  - Expandable Shamsi Date 📅 and Time 🕒 reminder pills with repeat chips.
- **On-Demand Update Checker:** In-app GitHub Release update checker in Settings that notifies users of new releases with direct APK download prompts.
- **Instant Full-Height Sheet Expansion:** `skipPartiallyExpanded = true` for Settings and Category Manager bottom sheets.
- **Subtask Expansion Persistence:** Maintained collapsible state across lazy list scrolls using `rememberSaveable`.

### Fixed
- Fixed keyboard action focus progression in Add Task sheet.
- Cleaned up repository metadata and passed Gitleaks security scans.

---

## [1.0.4] — 2026-08-28
### Added
- Redesigned Add Task layout with top action save button and navigation bar safe insets.
- Scrollable settings sheet with nested scroll support.

### Fixed
- Prevented keyboard from pushing bottom sheet buttons off-screen.
- Corrected Persian numeral formatting in badge counts.

---

## [1.0.3] — 2026-08-27
### Added
- Sound effects feedback via `ToneGenerator` for tap, completion, and deletion actions.
- Shamsi (Jalali) leap year calculations covering 1400–1420.
- Dynamic Dark and Light themes with mesh canvas gradient shaders.

---

## [1.0.2] — 2026-08-26
### Added
- Offline JSON backup export and Storage Access Framework (SAF) restore.
- Recurrence alarms (Daily, Weekly, Custom Every X Days).
- Notification action buttons for "Done" and "Snooze (+10m)".

---

## [1.0.1] — 2026-08-25
### Added
- Initial production release with 100% Jetpack Compose and Room Persistence.
- Automated GitHub Actions build and release workflow.
