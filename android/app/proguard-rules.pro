# ProGuard rules for TimeoutTileApp

# Prevent ProGuard from stripping your TileService class
-keep class com.absie.timeouttileapp.** { *; }

# Optional: Retain Android framework classes used in reflection or by Toast
-keep class android.widget.Toast { *; }
-keepclassmembers class android.provider.Settings$System { *; }
