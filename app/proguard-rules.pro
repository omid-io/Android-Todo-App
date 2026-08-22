# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep Room entities and database classes
-keep class com.example.data.** { *; }
-keepclassmembers class com.example.data.** { *; }

# Keep Backup & Moshi serialization models
-keep class com.example.ui.BackupData { *; }
-keepclassmembers class com.example.ui.BackupData { *; }
-keep class com.squareup.moshi.** { *; }
-keepclassmembers class * {
    @com.squareup.moshi.Json *;
}
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod
