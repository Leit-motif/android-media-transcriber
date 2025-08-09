# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep Room database classes
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *

# Keep Retrofit models
-keep class com.audioscribe.app.data.model.** { *; }

# Keep Firebase classes
-keep class com.google.firebase.** { *; }

# Keep WorkManager workers
-keep class com.audioscribe.app.worker.** { *; }

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
