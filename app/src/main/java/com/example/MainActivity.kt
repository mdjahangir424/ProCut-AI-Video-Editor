package com.example

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.core.firebase.configureAppCheck
import com.example.navigation.AppNavigation
import com.example.ui.theme.ProCutTheme
import java.util.Locale

class MainActivity : ComponentActivity() {

    private var currentLang by mutableStateOf("bn") // Defaults to Bengali as requested
    private var isDarkTheme by mutableStateOf(true)

    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("procut_prefs", Context.MODE_PRIVATE)
        val lang = prefs.getString("app_language", "bn") ?: "bn"
        val locale = Locale.Builder().setLanguage(lang).build()
        Locale.setDefault(locale)
        val config = Configuration(newBase.resources.configuration)
        config.setLocale(locale)
        super.attachBaseContext(newBase.createConfigurationContext(config))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // STEP 1: App Check initialization with launch intent before UI/ViewModels
        configureAppCheck(this, intent)

        enableEdgeToEdge()

        val prefs = getSharedPreferences("procut_prefs", Context.MODE_PRIVATE)
        currentLang = prefs.getString("app_language", "bn") ?: "bn"
        isDarkTheme = prefs.getBoolean("dark_theme", true)

        setContent {
            ProCutTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(
                        currentLanguage = currentLang,
                        onLanguageChange = { newLang ->
                            currentLang = newLang
                            prefs.edit().putString("app_language", newLang).apply()
                            recreate()
                        },
                        isDarkTheme = isDarkTheme,
                        onThemeToggle = { newTheme ->
                            isDarkTheme = newTheme
                            prefs.edit().putBoolean("dark_theme", newTheme).apply()
                        }
                    )
                }
            }
        }
    }
}
