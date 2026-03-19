package com.lonley.dev.vault.data

import android.content.SharedPreferences
import com.lonley.dev.vault.model.AccentColor
import com.lonley.dev.vault.model.AutoLockTimeout
import com.lonley.dev.vault.model.FontScale
import com.lonley.dev.vault.model.SettingsState
import com.lonley.dev.vault.model.SwipeAction
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

        val autoLockTimeout = try {
            AutoLockTimeout.valueOf(prefs.getString(KEY_AUTO_LOCK_TIMEOUT, AutoLockTimeout.OneMinute.name)!!)
        } catch (_: Exception) {
            AutoLockTimeout.OneMinute
        }

        val scrollVibrations = mapOf(
            "home" to prefs.getBoolean(KEY_SCROLL_HOME, false),
            "vault" to prefs.getBoolean(KEY_SCROLL_VAULT, false),
            "settings" to prefs.getBoolean(KEY_SCROLL_SETTINGS, false),
            "passwordDetail" to prefs.getBoolean(KEY_SCROLL_PASSWORD_DETAIL, false),
            "editEntry" to prefs.getBoolean(KEY_SCROLL_EDIT_ENTRY, false)
        )

        val swipeLeftAction = try {
            SwipeAction.valueOf(prefs.getString(KEY_SWIPE_LEFT_ACTION, SwipeAction.Edit.name)!!)
        } catch (_: Exception) {
            SwipeAction.Edit
        }

        val swipeRightAction = try {
            SwipeAction.valueOf(prefs.getString(KEY_SWIPE_RIGHT_ACTION, SwipeAction.CopyPassword.name)!!)
        } catch (_: Exception) {
            SwipeAction.CopyPassword
        }

        return SettingsState(
            themeMode = themeMode,
            accentColor = accentColor,
            language = language,
            hapticsEnabled = hapticsEnabled,
            fontScale = fontScale,
            autoLockTimeout = autoLockTimeout,
            scrollVibrations = scrollVibrations,
            swipeLeftAction = swipeLeftAction,
            swipeRightAction = swipeRightAction
        )
    }

    fun save(state: SettingsState) {
        prefs.edit()
            .putString(KEY_THEME_MODE, state.themeMode.name)
            .putString(KEY_ACCENT_COLOR, state.accentColor.name)
            .putString(KEY_LANGUAGE, state.language)
            .putBoolean(KEY_HAPTICS, state.hapticsEnabled)
            .putString(KEY_FONT_SCALE, state.fontScale.name)
            .putString(KEY_AUTO_LOCK_TIMEOUT, state.autoLockTimeout.name)
            .putBoolean(KEY_SCROLL_HOME, state.scrollVibrations["home"] ?: false)
            .putBoolean(KEY_SCROLL_VAULT, state.scrollVibrations["vault"] ?: false)
            .putBoolean(KEY_SCROLL_SETTINGS, state.scrollVibrations["settings"] ?: false)
            .putBoolean(KEY_SCROLL_PASSWORD_DETAIL, state.scrollVibrations["passwordDetail"] ?: false)
            .putBoolean(KEY_SCROLL_EDIT_ENTRY, state.scrollVibrations["editEntry"] ?: false)
            .putString(KEY_SWIPE_LEFT_ACTION, state.swipeLeftAction.name)
            .putString(KEY_SWIPE_RIGHT_ACTION, state.swipeRightAction.name)
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
        private const val KEY_SCROLL_PASSWORD_DETAIL = "scroll_vibration_password_detail"
        private const val KEY_SCROLL_EDIT_ENTRY = "scroll_vibration_edit_entry"
        private const val KEY_FONT_SCALE = "font_scale"
        private const val KEY_AUTO_LOCK_TIMEOUT = "auto_lock_timeout"
        private const val KEY_SWIPE_LEFT_ACTION = "swipe_left_action"
        private const val KEY_SWIPE_RIGHT_ACTION = "swipe_right_action"
    }
}
