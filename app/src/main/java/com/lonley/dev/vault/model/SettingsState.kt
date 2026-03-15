package com.lonley.dev.vault.model

import com.lonley.dev.vault.ui.theme.ThemeMode

enum class AccentColor {
    Auto, Blue, Teal, Green, Yellow, Orange, Purple, Gray, Red, Pink, Indigo, Brown
}

enum class FontScale(val factor: Float) {
    Small(0.85f),
    Default(1.0f),
    Large(1.2f),
    ExtraLarge(1.4f)
}

enum class AutoLockTimeout(val millis: Long, val label: String) {
    EveryUpdate(0L, "Every UI Update"),
    OneMinute(60_000L, "1 Minute"),
    TwoMinutes(120_000L, "2 Minutes"),
    FiveMinutes(300_000L, "5 Minutes"),
    Never(-1L, "Never")
}

data class SettingsState(
    val themeMode: ThemeMode = ThemeMode.System,
    val accentColor: AccentColor = AccentColor.Auto,
    val language: String = "English",
    val hapticsEnabled: Boolean = true,
    val fontScale: FontScale = FontScale.Default,
    val autoLockTimeout: AutoLockTimeout = AutoLockTimeout.OneMinute,
    val scrollVibrations: Map<String, Boolean> = mapOf(
        "home" to false,
        "vault" to false,
        "settings" to false,
        "passwordDetail" to false,
        "editEntry" to false
    )
)
