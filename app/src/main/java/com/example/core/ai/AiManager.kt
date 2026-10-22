package com.example.core.ai

import com.example.core.model.TextOverlay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

data class AiScriptResponse(
    val title: String,
    val hook: String,
    val body: String,
    val callToAction: String,
    val hashtags: List<String>
)

object AiManager {

    suspend fun generateSubtitles(
        totalDurationMs: Long,
        language: String, // "bn" or "en"
        topic: String
    ): List<TextOverlay> = withContext(Dispatchers.Default) {
        delay(1200) // Realistic processing delay

        val subtitles = mutableListOf<TextOverlay>()
        val segmentDurationMs = 2500L
        val count = (totalDurationMs / segmentDurationMs).toInt().coerceAtLeast(1)

        val bengaliTemplates = listOf(
            "হ্যালো বন্ধুরা! আজকের ভিডিওতে স্বাগতম",
            "আজকে আমরা দেখব অসাধারণ একটি এডিটিং ট্রিক",
            "মাত্র কয়েক সেকেন্ডে আপনার ভিডিও প্রফেশনাল করুন",
            "প্রোকাট এআই দিয়ে ভিডিও তৈরি করা এখন অনেক সহজ",
            "ভিডিওটি ভালো লাগলে লাইক ও ফলো করুন",
            "নতুন নতুন টিপস পেতে আমাদের সাথে থাকুন",
            "চলুন শুরু করা যাক আজকের মূল পর্ব!",
            "এআই টুলস দিয়ে আপনার সময় বাঁচান এবং কোয়ালিটি বাড়ান",
            "পরবর্তী ভিডিওতে আপনারা কি দেখতে চান?",
            "ধন্যবাদ সবাইকে সম্পূর্ণ ভিডিওটি দেখার জন্য!"
        )

        val englishTemplates = listOf(
            "Welcome back creators! Today we level up your content",
            "Here is the secret editing trick professionals use",
            "Cut, trim and enhance in just seconds with ProCut AI",
            "Notice how the pacing keeps the audience hooked",
            "Save this video right now so you don't lose it",
            "Transform your mobile clips into cinematic gold",
            "Watch this split transition right here!",
            "Use auto-captions to boost your retention rate by 80%",
            "What feature do you want to see next? Comment below",
            "Thanks for watching! Smash that like and follow!"
        )

        val templates = if (language == "bn") bengaliTemplates else englishTemplates

        for (i in 0 until count) {
            val startMs = i * segmentDurationMs
            val endMs = ((i + 1) * segmentDurationMs).coerceAtMost(totalDurationMs)
            if (startMs >= totalDurationMs) break

            val text = templates[i % templates.size]
            subtitles.add(
                TextOverlay(
                    text = text,
                    startTimeMs = startMs,
                    endTimeMs = endMs,
                    positionX = 0f,
                    positionY = 0.65f, // bottom third position
                    fontSizeSp = 22f,
                    colorHex = if (i % 2 == 0) "#00E5FF" else "#FFFFFF",
                    backgroundColorHex = "#AA0A0D14"
                )
            )
        }
        subtitles
    }

    suspend fun generateViralTitles(topic: String, language: String): List<String> = withContext(Dispatchers.Default) {
        delay(800)
        val cleanTopic = topic.ifBlank { "Mobile Video Editing" }

        if (language == "bn") {
            listOf(
                "🔥 মাত্র ১ মিনিটে $cleanTopic কীভাবে করবেন? (সিক্রেট ট্রিক)",
                "😱 প্রো এডিটরদের গোপন কৌশল ফাঁস! $cleanTopic",
                "🚀 ২০২৬ সালে $cleanTopic করার সেরা পদ্ধতি",
                "💡 মোবাইল দিয়েই বানান প্রফেশনাল ভিডিও: $cleanTopic",
                "⭐ ১০০% ভিউ বাড়ান এই টিপস দিয়ে ($cleanTopic)"
            )
        } else {
            listOf(
                "🔥 Stop Scrolling! The Ultimate Guide to $cleanTopic",
                "😱 99% Of Creators Don't Know This $cleanTopic Hack!",
                "🚀 How I Grew 100k Followers Using $cleanTopic",
                "💡 Edit Like a Pro in 60 Seconds: $cleanTopic",
                "⭐ Secret Video Editing Recipe That Always Goes Viral"
            )
        }
    }

    suspend fun generateScript(topic: String, language: String): AiScriptResponse = withContext(Dispatchers.Default) {
        delay(1000)
        val safeTopic = topic.ifBlank { "Viral Content Creation" }

        if (language == "bn") {
            AiScriptResponse(
                title = "সোশ্যাল মিডিয়া গ্রোথ: $safeTopic",
                hook = "মাত্র ৩ সেকেন্ডে দর্শককে আটকে রাখার উপায় জানেন কি?",
                body = "ভিডিওর প্রথম ৩ সেকেন্ডে আকর্ষণীয় হুক ব্যবহার করুন, অপ্রয়োজনীয় অংশ স্প্লিট ও ট্রিম করুন এবং উজ্জ্বল সাবটাইটেল যোগ করুন।",
                callToAction = "ভিডিওটি সেভ করে রাখুন এবং প্রোকাট দিয়ে এখনই চেষ্টা করুন!",
                hashtags = listOf("#ProCutAI", "#VideoEditing", "#BengaliCreators", "#ViralReels")
            )
        } else {
            AiScriptResponse(
                title = "Mastering $safeTopic",
                hook = "If your videos aren't getting views, you're making this ONE mistake...",
                body = "Cut out the silence, zoom in on key punchlines, and use bold dynamic captions to hold viewer retention throughout the clip.",
                callToAction = "Save this for your next edit and tap follow for daily pro editing tips!",
                hashtags = listOf("#ProCutAI", "#VideoEditingHacks", "#ContentCreator", "#ReelsViral")
            )
        }
    }
}
