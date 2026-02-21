package com.lonley.dev.vault.data

import android.content.SharedPreferences
import com.lonley.dev.vault.model.AccentColor
import com.lonley.dev.vault.model.FontScale
import com.lonley.dev.vault.model.SettingsState
import com.lonley.dev.vault.ui.theme.ThemeMode

class SettingsPreferences(private val prefs: SharedPreferences) {

    fun load(): SettingsState {
        val themeMode = try {
            ThemeMode.valueOf(prefs.getString(KEY_THEME_MODE, ThemeMode.System.name)!!)
        } catch (_: Exception) {
            ThemeMode.System
        }

        val accentColor = try {
            AccentColor.valueOf(prefs.getString(KEY_ACCENT_COLOR, AccentColor.Auto.name)!!)
        } catch (_: Exception) {
            AccentColor.Auto
        }

        val language = prefs.getString(KEY_LANGUAGE, "English") ?: "English"
        val hapticsEnabled = prefs.getBoolean(KEY_HAPTICS, true)

        val fontScale = try {
            FontScale.valueOf(prefs.getString(KEY_FONT_SCALE, FontScale.Default.name)!!)
        } catch (_: Exception) {
            FontScale.Default
        }

        val scrollVibrations = mapOf(
            "home" to prefs.getBoolean(KEY_SCROLL_HOME, false),
            "vault" to prefs.getBoolean(KEY_SCROLL_VAULT, false),
            "settings" to prefs.getBoolean(KEY_SCROLL_SETTINGS, false)
        )

        return SettingsState(
            themeMode = themeMode,
            accentColor = accentColor,
            language = language,
            hapticsEnabled = hapticsEnabled,
            fontScale = fontScale,
            scrollVibrations = scrollVibrations
        )
    }

    fun save(state: SettingsState) {
        prefs.edit()
            .putString(KEY_THEME_MODE, state.themeMode.name)
            .putString(KEY_ACCENT_COLOR, state.accentColor.name)
            .putString(KEY_LANGUAGE, state.language)
            .putBoolean(KEY_HAPTICS, state.hapticsEnabled)
            .putString(KEY_FONT_SCALE, state.fontScale.name)
            .putBoolean(KEY_SCROLL_HOME, state.scrollVibrations["home"] ?: false)
            .putBoolean(KEY_SCROLL_VAULT, state.scrollVibrations["vault"] ?: false)
            .putBoolean(KEY_SCROLL_SETTINGS, state.scrollVibrations["settings"] ?: false)
            .apply()
    }

    companion object {
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_ACCENT_COLOR = "accent_color"
        private const val KEY_LANGUAGE = "language"
        private const val KEY_HAPTICS = "haptics_enabled"
        private const val KEY_SCROLL_HOME = "scroll_vibration_home"
        private const val KEY_SCROLL_VAULT = "scroll_vibration_vault"
        private const val KEY_SCROLL_SETTINGS = "scroll_vibration_settings"
        private const val KEY_FONT_SCALE = "font_scale"
    }
}
