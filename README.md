<div align="center">
  <img src="screenshots/logo.png" width="128" height="128" alt="My Tasks Logo" />
  <h1>✨ My Tasks / کارهای من</h1>
  <p><b>Ultra-Modern Glassmorphism Todo & Task Manager for Android</b></p>

  <p>
    <a href="#-my-tasks--modern-android-todo-app"><b>🇺🇸 English Documentation</b></a> • 
    <a href="#-کارهای-من--اپلیکیشن-مدیریت-وظایف-اندروید"><b>🇮🇷 مستندات فارسی</b></a>
  </p>

  <p>
    <a href="https://kotlinlang.org/"><img src="https://img.shields.io/badge/Kotlin-2.0.21-purple.svg?style=flat&logo=kotlin" alt="Kotlin 2.0.21" /></a>
    <a href="https://developer.android.com/jetpack/compose"><img src="https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4.svg?style=flat&logo=jetpackcompose" alt="Jetpack Compose" /></a>
    <a href="https://developer.android.com/training/data-storage/room"><img src="https://img.shields.io/badge/Room-Persistence-3DDC84.svg?style=flat&logo=android" alt="Room Persistence" /></a>
    <a href="https://github.com/omid-io/Android-Todo-App/actions"><img src="https://img.shields.io/badge/CI%2FCD-GitHub%20Actions-brightgreen.svg?style=flat&logo=githubactions" alt="CI/CD" /></a>
    <a href="https://github.com/omid-io/Android-Todo-App/releases/latest"><img src="https://img.shields.io/badge/Release-v1.0.3-orange.svg?style=flat" alt="Latest Release" /></a>
    <a href="LICENSE"><img src="https://img.shields.io/badge/License-MIT-green.svg?style=flat" alt="License MIT" /></a>
  </p>
</div>

---

# 🇺🇸 My Tasks — Modern Android Todo App

**My Tasks** is a high-performance, offline-first task management application for Android built with **100% Jetpack Compose** and **Material 3**. Engineered around a stunning **Ultra-Glassmorphism** visual theme, it delivers a fluid 60FPS user experience, native dual-calendar scheduling (Jalali Solar Hijri & Gregorian), exact alarm reminders, and fully automated cloud CI/CD releases.

---

## 📸 Visual Showcase

<div align="center">
  <table>
    <tr>
      <td width="25%"><img src="screenshots/mainscreen.png" alt="Main Dashboard" /></td>
      <td width="25%"><img src="screenshots/darkmode.png" alt="Dark Mode" /></td>
      <td width="25%"><img src="screenshots/settings.png" alt="Settings & Backup" /></td>
      <td width="25%"><img src="screenshots/add-task.png" alt="Add Task Dialog" /></td>
    </tr>
    <tr>
      <td align="center"><b>Dashboard</b><br/>Category groups & active tasks</td>
      <td align="center"><b>Dark Mode</b><br/>Frosted glass glow & mesh shaders</td>
      <td align="center"><b>Settings</b><br/>Theme, language & JSON backup</td>
      <td align="center"><b>Task Entry</b><br/>Subtasks, Jalali picker & recurrence</td>
    </tr>
  </table>
</div>

---

## 💎 Key Features

* **🎨 Ultra-Glassmorphism UI:** Frosted glass cards, translucent Floating Action Button (FAB), multi-layer mesh canvas gradients, and smooth spring micro-animations.
* **🗓️ Dual-Calendar Engine (Jalali & Gregorian):** In-house, zero-dependency Persian solar calendar with high-precision leap-year calculations (covering years 1400–1420) and localized Persian numeral conversion (`toPersianDigits`).
* **⏰ Smart Exact Reminders & Recurrence:** Exact alarms scheduled via `AlarmManager.setExactAndAllowWhileIdle` with recurrence rules (Daily, Weekly, Custom Every X Days) and interactive notification action buttons ("Done" & "Snooze +10m").
* **📋 Hierarchical Subtasks & Task Ordering:** Multi-level subtasks with progress tracking, collapsible activity rows, and ergonomic keyboard `Done / Enter` submission.
* **🔄 Reactive & Offline-First:** Powered by AndroidX Room with Coroutine Flows for instant, flicker-free UI updates.
* **💾 Safe Backup & Restore:** Complete JSON export and Storage Access Framework (SAF) import to preserve user data without third-party server dependency.
* **🚀 Automated CI/CD Release Pipeline:** Every tagged commit automatically triggers GitHub Actions to compile, test, optimize via R8 Proguard, sign, and publish release APKs.

---

## 🛠️ Architecture & Tech Stack

```
com.example
├── data/              # Room Database, DAOs, Entities & Repository Layer
│   ├── AppDatabase.kt
│   ├── Entities.kt    # Category, Task, Subtask
│   ├── TodoDao.kt
│   └── TodoRepository.kt
├── ui/                # Jetpack Compose UI Screens, Dialogs & ViewModel
│   ├── MainTodoScreen.kt
│   ├── TaskBottomSheet.kt
│   ├── CategoryManagerBottomSheet.kt
│   ├── ShamsiDatePicker.kt
│   ├── SettingsBottomSheet.kt
│   ├── ReminderManagementScreen.kt
│   ├── GlassmorphismModifier.kt
│   └── TodoViewModel.kt
└── util/              # Scheduling, Audio Feedback & Calendar Helpers
    ├── JalaliCalendar.kt
    ├── ReminderScheduler.kt
    ├── ReminderReceiver.kt
    ├── NotificationActionReceiver.kt
    ├── BootReceiver.kt
    └── SoundManager.kt
```

* **Language:** Kotlin 2.0.21
* **UI Framework:** Jetpack Compose (Material 3)
* **Persistence:** AndroidX Room 2.6.1 with Coroutines Flow
* **Code Shrinker:** ProGuard / R8 optimization
* **Build System:** Gradle 8.11.1 / Android Gradle Plugin (AGP) 8.8.2
* **Target Environment:** Android 7.0+ (API 24 to 35)

---

## ⚡ CLI Build & Test Runbook

You can build and test the application directly using the Gradle wrapper:

```powershell
# 1. Compile Release APK with R8 code shrinking
./gradlew assembleRelease

# 2. Run Local JVM Unit Tests & Robolectric Suites
./gradlew testDebugUnitTest

# 3. Perform Android Lint Quality Gate
./gradlew lintDebug
```

---

## 📦 Direct Downloads

| Release Version | Package File | Architecture | Min SDK | Status |
| :--- | :--- | :--- | :--- | :--- |
| **v1.0.3 (Latest)** | [`MyTasks-v1.0.3.apk`](https://github.com/omid-io/Android-Todo-App/releases/download/v1.0.3/MyTasks-v1.0.3.apk) | Universal | Android 7.0+ (API 24+) | ✅ GitHub CI/CD Passed |
| **v1.0.2** | [`MyTasks-v1.0.2.apk`](https://github.com/omid-io/Android-Todo-App/releases/download/v1.0.2/MyTasks-v1.0.2.apk) | Universal | Android 7.0+ (API 24+) | ✅ GitHub CI/CD Passed |
| **v1.0.1** | [`MyTasks-v1.0.1.apk`](https://github.com/omid-io/Android-Todo-App/releases/download/v1.0.1/MyTasks-v1.0.1.apk) | Universal | Android 7.0+ (API 24+) | ✅ GitHub CI/CD Passed |

---

<br />

# 🇮🇷 کارهای من — اپلیکیشن مدیریت وظایف اندروید

**«کارهای من / My Tasks»** یک اپلیکیشن سبک، آفلاین، امن و بسیار سریع برای مدیریت وظایف روزانه در سیستم‌عامل اندروید است. این برنامه با استفاده از **۱۰۰٪ Jetpack Compose** و زبان **کاتلین** بر پایه معماری مدرن **Glassmorphism (طراحی شیشه‌ای)** توسعه داده شده تا محیطی چشم‌نواز، روان و بدون لگ را برای کاربران به ارمغان آورد.

---

## ✨ ویژگی‌های برجسته برنامه

* **🎨 رابط کاربری فوق‌مدرن شیشه‌ای (Glassmorphism):** استفاده از کارت‌های مات با افکت شیشه‌ای، دکمه شناور تمام‌شیشه‌ای، گرادینت‌های داینامیک Mesh Canvas و انیمیشن‌های روان ۶۰ فریم بر ثانیه.
* **🗓️ تقویم دوگانه هوشمند (شمسی و میلادی):** پیاده‌سازی موتور بومی و مستقل تقویم جلالی با محاسبه دقیق سال‌های کبیسه برای بازه سال‌های ۱۴۰۰ تا ۱۴۲۰ و تبدیل خودکار اعداد به ارقام فارسی.
* **⏰ یادآورها و آلارم‌های دقیق:** تنظیم هشدارهای زمان‌بندی‌شده بدون خطا با `AlarmManager` با امکان تکرار (روزانه، هفتگی و روزهای دلخواه) به همراه دکمه‌های اقدام سریع در اعلان‌ها («انجام شد» و «به تعویق انداختن ۱۰ دقیقه‌ای»).
* **📝 ساب‌تسک‌ها و ثبت سریع با کیبورد:** امکان تعریف بی‌نهایت زیرمجموعه کار برای هر تسک با نوار پیشرفت درصد انجام و ثبت سریع با زدن کلید اینتر کیبورد.
* **💾 پشتیبان‌گیری و بازیابی آفلاین:** خروجی گرفتن کامل از اطلاعات به فرمت استاندارد JSON و امکان بازگردانی در هر زمان بدون نیاز به اتصال اینترنت یا سرور ثالث.
* **🚀 خط لوله انتشار تمام‌اتوماتیک (CI/CD):** یکپارچه‌سازی کامل با سرورهای ابری GitHub Actions جهت کامپایل، تست، فشرده‌سازی با موتور R8 و انتشار خودکار پکیج‌های Release.

---

## 🛠️ مشخصات فنی و معماری

- **زبان توسعه:** کاتلین ۲.۰.۲۱ (Kotlin 2.0.21)
- **فریم‌ورک رابط کاربری:** Jetpack Compose با کامپوننت‌های Material 3
- **پایگاه داده داخلی:** AndroidX Room با قابلیت واکنش‌گرایی بر بستر Kotlin Flow
- **الگوی معماری:** MVVM + Repository Pattern
- **فشرده‌سازی باینری:** موتور بهینه‌ساز R8 ProGuard
- **حداقل اندروید مورد نیاز:** اندروید ۷.۰ به بالا (API 24+)

---

## 📥 دانلود و نصب مستقیم

شما می‌توانید همواره جدیدترین نسخه پکیج نصب برنامه را مستقیماً از صفحه [**Releaseهای گیت‌هاب**](https://github.com/omid-io/Android-Todo-App/releases) دریافت کنید:

👉 **[دانلود نسخه v1.0.3 (جدیدترین نگارش)](https://github.com/omid-io/Android-Todo-App/releases/download/v1.0.3/MyTasks-v1.0.3.apk)**

---

## 🤝 مشارکت و حمایت

اگر این پروژه برای شما مفید و کاربردی بوده است، با دادن یک **ستاره (⭐ Star)** در بالای صفحه گیت‌هاب از توسعه آن حمایت فرمایید!

<div align="center">
  <p>Made with ❤️ by Omid Zaferi • ساخته شده با ❤️ برای جامعه توسعه‌دهندگان</p>
</div>
