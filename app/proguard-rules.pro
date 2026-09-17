# MediaPipe and protobuf use generated/native integration at runtime.
-keep class com.google.mediapipe.** { *; }
-keep class com.google.protobuf.** { *; }
-dontwarn com.google.mediapipe.**
-dontwarn com.google.protobuf.**

# CameraX discovers parts of its implementation dynamically.
-keep class androidx.camera.** { *; }
-dontwarn androidx.camera.**

-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod

-keepclassmembers class com.nyxiaglow.app.** {
	<init>(...);
}
