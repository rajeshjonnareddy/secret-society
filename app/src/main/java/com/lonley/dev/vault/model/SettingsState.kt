package com.lonley.dev.vault.model

import com.lonley.dev.vault.ui.theme.ThemeMode

enum class AccentColor {
    Auto, Blue, Teal, Green, Yellow, Orange, Purple, Gray
}

data class SettingsState(
    val themeMode: ThemeMode = ThemeMode.System,
    val accentColor: AccentColor = AccentColor.Auto,
    val language: String = "English",
    val hapticsEnabled: Boolean = true,
    val scrollVibrations: Map<String, Boolean> = mapOf(
        "home" to false,
        "vault" to false,
        "settings" to false,
        "passwordDetail" to false,
        "editEntry" to false
    )
)
