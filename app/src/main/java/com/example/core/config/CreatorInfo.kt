package com.example.core.config

data class OpenSourceLicense(
    val libraryName: String,
    val author: String,
    val licenseType: String,
    val description: String
)

object CreatorInfo {
    const val DEVELOPER_NAME = "Md Jahangir Alam"
    const val CONTACT_PHONE = "+8801778600028"
    const val CONTACT_EMAIL = "mdjahangiralam2528@gmail.com"
    const val APP_NAME = "ProCut AI Video Editor"
    const val APP_VERSION = "1.0.0"
    const val BUILD_NUMBER = "100"
    const val RELEASE_YEAR = "2026"

    const val COPYRIGHT_NOTICE =
        "© 2026 Md Jahangir Alam. All rights reserved to the extent permitted by applicable law."

    const val CREATOR_ATTRIBUTION =
        "ProCut AI Video Editor — Created and maintained by Md Jahangir Alam."

    const val PRIVACY_SUMMARY =
        "ProCut AI Video Editor respects user privacy. Video and audio editing operations are processed entirely on-device using AndroidX Media3. No personal media files are uploaded to external servers without explicit user request and consent."

    const val TERMS_SUMMARY =
        "ProCut AI Video Editor is provided for personal and professional video creation. Users retain full copyright and ownership of the videos they edit and export with this application."

    val THIRD_PARTY_LICENSES = listOf(
        OpenSourceLicense(
            libraryName = "AndroidX Media3 (ExoPlayer & Transformer)",
            author = "The Android Open Source Project",
            licenseType = "Apache License 2.0",
            description = "High-performance media playback and hardware-accelerated video export pipeline."
        ),
        OpenSourceLicense(
            libraryName = "Jetpack Compose & Material 3",
            author = "Google LLC",
            licenseType = "Apache License 2.0",
            description = "Modern declarative Android UI toolkit and Material Design system."
        ),
        OpenSourceLicense(
            libraryName = "AndroidX Room Database",
            author = "The Android Open Source Project",
            licenseType = "Apache License 2.0",
            description = "SQLite object-relational mapping for local project persistence."
        ),
        OpenSourceLicense(
            libraryName = "Coil Compose",
            author = "Coil Contributors",
            licenseType = "Apache License 2.0",
            description = "Coroutine-powered asynchronous image and video frame loading."
        ),
        OpenSourceLicense(
            libraryName = "Kotlin & Kotlinx Coroutines",
            author = "JetBrains s.r.o.",
            licenseType = "Apache License 2.0",
            description = "Modern language features, structured concurrency, and reactive Flow."
        )
    )
}
