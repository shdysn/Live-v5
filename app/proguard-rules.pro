# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Preserve Room entities and DAOs
-keepclassmembers class * extends androidx.room.RoomDatabase {
    *;
}
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }
-keep class pk.livecaster.app.core.storage.entity.** { *; }

# Preserve data models and DTOs
-keep class pk.livecaster.app.**.domain.model.** { *; }
-keep class pk.livecaster.app.**.data.dto.** { *; }
-keep class pk.livecaster.app.accounts.presentation.** { *; }

# Preserve Moshi adapters
-keepattributes *Annotation*
-keepclassmembers class * {
    @com.squareup.moshi.* <fields>;
    @com.squareup.moshi.* <methods>;
}
-keep class com.squareup.moshi.** { *; }


# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
