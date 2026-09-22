# LexiGuess Production ProGuard / R8 Rules

# Room SQLite
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**
-keep class androidx.room.** { *; }
-keep class com.rank.lexi.data.db.** { *; }

# Dagger / Hilt
-keep class * extends dagger.hilt.internal.GeneratedComponent
-keep class * extends dagger.hilt.internal.ComponentManager
-keep class * implements dagger.hilt.internal.GeneratedComponent
-keep class * implements dagger.hilt.internal.ComponentManager
-keepnames class com.rank.lexi.** { *; }
-keepclassmembers class * {
    @javax.inject.Inject <init>(...);
    @dagger.Provides <methods>;
}

# Coroutines
-keepclassmembernames class kotlinx.coroutines.internal.MainDispatcherFactory {
    java.lang.String getLoadPriority();
    kotlinx.coroutines.MainCoroutineDispatcher createDispatcher(java.util.List);
}
-dontwarn kotlinx.coroutines.**

# Retrofit & OkHttp
-dontwarn okio.**
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions
-keepattributes *Annotation*
-keepclassmembers,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# DataStore
-keepclassmembers class * extends androidx.datastore.preferences.core.Preferences { *; }

# Jetpack Compose — rely on AAR consumer ProGuard rules.

# Procedural AudioTrack
-keepclassmembers class com.rank.lexi.ui.audio.SoundManager {
    public *;
}

# Start.io SDK
-keep class com.startapp.** { *; }
-dontwarn com.startapp.**
-keepattributes Exceptions, InnerClasses, Signature, Deprecated, SourceFile, LineNumberTable, *Annotation*, EnclosingMethod
