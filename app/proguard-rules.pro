# Compose rules
-keep class androidx.compose.** { *; }

# Coil rules
-keep class coil.** { *; }
-dontwarn coil.**

# OkHttp rules
-keepattributes Signature
-keepattributes *Annotation*
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**
