package com.lonley.dev.vault.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
)

fun scaledTypography(scale: Float): Typography {
    val default = Typography()
    if (scale == 1.0f) return default

    return Typography(
        displayLarge = default.displayLarge, // Keep headings unscaled
        displayMedium = default.displayMedium.copy(
            fontSize = default.displayMedium.fontSize * scale,
            lineHeight = default.displayMedium.lineHeight * scale
        ),
        displaySmall = default.displaySmall.copy(
            fontSize = default.displaySmall.fontSize * scale,
            lineHeight = default.displaySmall.lineHeight * scale
        ),
        headlineLarge = default.headlineLarge.copy(
            fontSize = default.headlineLarge.fontSize * scale,
            lineHeight = default.headlineLarge.lineHeight * scale
        ),
        headlineMedium = default.headlineMedium.copy(
            fontSize = default.headlineMedium.fontSize * scale,
            lineHeight = default.headlineMedium.lineHeight * scale
        ),
        headlineSmall = default.headlineSmall.copy(
            fontSize = default.headlineSmall.fontSize * scale,
            lineHeight = default.headlineSmall.lineHeight * scale
        ),
        titleLarge = default.titleLarge.copy(
            fontSize = default.titleLarge.fontSize * scale,
            lineHeight = default.titleLarge.lineHeight * scale
        ),
        titleMedium = default.titleMedium.copy(
            fontSize = default.titleMedium.fontSize * scale,
            lineHeight = default.titleMedium.lineHeight * scale
        ),
        titleSmall = default.titleSmall.copy(
            fontSize = default.titleSmall.fontSize * scale,
            lineHeight = default.titleSmall.lineHeight * scale
        ),
        bodyLarge = default.bodyLarge.copy(
            fontSize = default.bodyLarge.fontSize * scale,
            lineHeight = default.bodyLarge.lineHeight * scale
        ),
        bodyMedium = default.bodyMedium.copy(
            fontSize = default.bodyMedium.fontSize * scale,
            lineHeight = default.bodyMedium.lineHeight * scale
        ),
        bodySmall = default.bodySmall.copy(
            fontSize = default.bodySmall.fontSize * scale,
            lineHeight = default.bodySmall.lineHeight * scale
        ),
        labelLarge = default.labelLarge.copy(
            fontSize = default.labelLarge.fontSize * scale,
            lineHeight = default.labelLarge.lineHeight * scale
        ),
        labelMedium = default.labelMedium.copy(
            fontSize = default.labelMedium.fontSize * scale,
            lineHeight = default.labelMedium.lineHeight * scale
        ),
        labelSmall = default.labelSmall.copy(
            fontSize = default.labelSmall.fontSize * scale,
            lineHeight = default.labelSmall.lineHeight * scale
        )
    )
}
