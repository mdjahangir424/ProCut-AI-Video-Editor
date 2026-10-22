# ProCut AI Video Editor

An advanced, native Android video editing application built with Jetpack Compose, Material 3, and AndroidX Media3 (ExoPlayer & Transformer). ProCut delivers a high-precision multi-track timeline, hardware-accelerated video export, and offline-first AI speech and script tools.

---

## Official Creator & Ownership Information

- **Creator & Lead Developer:** Md Jahangir Alam
- **Contact Phone:** +8801778600028
- **Contact Email:** mdjahangiralam2528@gmail.com
- **App Name:** ProCut AI Video Editor
- **Version:** 1.0.0 (Build 100)

### Copyright Notice
> **© 2026 Md Jahangir Alam. All rights reserved to the extent permitted by applicable law.**  
> *ProCut AI Video Editor — Created and maintained by Md Jahangir Alam.*

---

## Key Features

1. **Multi-Track Timeline:**
   - Frame-accurate video trimming (start/end millisecond handles).
   - Instant split at current playhead position with logical timeline reordering.
   - Video track, background audio track, and timed text overlay tracks.
   - Real-time undo/redo history stack (up to 20 levels).
   - Timecode ruler with scalable pinch/touch zoom.

2. **Video Engine (AndroidX Media3):**
   - ExoPlayer preview with `MediaItem.ClippingConfiguration` synchronization.
   - Hardware-accelerated MP4 rendering using AndroidX Media3 Transformer.
   - H.264 video codec and AAC audio encoding.
   - Preset resolutions: 720p HD, 1080p Full HD, and 4K Ultra HD.
   - Multiple aspect ratios: 9:16 (TikTok/Reels), 16:9 (YouTube), 1:1 (Square), 4:5 (Portrait), 3:4 (Feed).
   - 90° clip rotation and cinematic color grading filters.

3. **AI Studio (Offline & Cloud):**
   - Automated paced subtitle generator for Bengali (বাংলা) and English.
   - High-CTR viral title generator for short-form video creators.
   - 30-second short-form video script & hook builder.
   - Vocal clarity & room noise reduction audio presets.
   - Zero unauthorized media uploads — 100% on-device processing by default.

4. **Localization & Theming:**
   - Full Unicode Bengali (বাংলা) and English localization with runtime language switching.
   - Cinematic dark theme optimized for video editing contrasts.

---

## Google Play Store Listing Copy

### Title (30 characters max)
`ProCut AI Video Editor`

### Short Description (80 characters max)
`Professional multi-track video editor with trimming, AI subtitles & fast export.`

### Full Description
ProCut AI Video Editor puts professional mobile video production in your hands. Engineered with the latest AndroidX Media3 framework, ProCut lets content creators cut, trim, split, and export stunning videos for TikTok, YouTube Shorts, Reels, and Instagram without quality loss.

**Features:**
- **Precision Trimming & Splitting:** Cut any clip at the exact playhead position with smooth playback.
- **Multiple Aspect Ratios:** Easily toggle between 9:16, 16:9, 1:1, 4:5, and 3:4 aspect ratios.
- **Dynamic Text & Captions:** Add customizable, styled text overlays with size and color controls.
- **AI Studio Tools:** Generate viral video titles, 30s video hooks, and auto-subtitles in English and Bengali.
- **Fast MP4 Export:** Export in 720p or 1080p Full HD with real-time progress and direct sharing.
- **Privacy First:** All video editing is processed locally on your phone.

**Developed by:** Md Jahangir Alam  
**Support Email:** mdjahangiralam2528@gmail.com  
**Contact:** +8801778600028  
**Copyright:** © 2026 Md Jahangir Alam. All rights reserved.

---

## Third-Party Open-Source Attribution

This software incorporates open-source components developed by third parties:
- **AndroidX Media3 (ExoPlayer & Transformer):** The Android Open Source Project — Apache License 2.0
- **Jetpack Compose & Material 3:** Google LLC — Apache License 2.0
- **AndroidX Room Database:** The Android Open Source Project — Apache License 2.0
- **Coil Compose:** Coil Contributors — Apache License 2.0
- **Kotlin & Coroutines:** JetBrains s.r.o. — Apache License 2.0

*Note: All third-party trademarks and open-source libraries belong to their respective owners.*

---

## Build & Test Instructions

### Minimum Requirements
- **JDK:** OpenJDK 17 or higher
- **Android SDK:** Compile SDK 36, Target SDK 36, Minimum SDK 26 (Android 8.0)
- **Gradle:** 8.13+ with Android Gradle Plugin 9.1.1

### Build Debug APK
```bash
./gradlew assembleDebug
```
The resulting APK will be at:
`app/build/outputs/apk/debug/app-debug.apk`

### Run Local Unit Tests
```bash
./gradlew testDebugUnitTest
```
