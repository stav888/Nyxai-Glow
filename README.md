# Nyxai Glow

Nyxai Glow is an Android beauty studio focused on two effects from the implementation brief:

- **Adaptive Glow** adjusts the live preview based on ambient luminance.
- **Texture Preserve** exposes a natural smoothing control designed to keep identity details visible.

## Build

Open this folder in Android Studio with JDK 17 and let Gradle sync. Run the `app` configuration on an Android 8.0+ device or emulator with a camera.

The current scaffold includes the CameraX preview and luminance analyzer. The MediaPipe face-landmarker and TFLite model engines should be connected after adding `face_landmarker.task` and a converted smoothing model to `app/src/main/assets`.
