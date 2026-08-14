# Add project specific ProGuard rules here.

# Keep Room entities and DAOs
-keep class com.rob.houserental.model.** { *; }
-keep class com.rob.houserental.data.** { *; }

# Keep Google API Client models
-keepclassmembers class * {
    @com.google.api.client.util.Key <fields>;
}
