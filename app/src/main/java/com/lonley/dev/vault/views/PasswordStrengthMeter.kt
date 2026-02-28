package com.lonley.dev.vault.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lonley.dev.vault.ui.theme.StrengthFair
import com.lonley.dev.vault.ui.theme.StrengthGood
import com.lonley.dev.vault.ui.theme.StrengthStrong
import com.lonley.dev.vault.ui.theme.StrengthWeak

data class PasswordStrength(val score: Float, val label: String, val color: Color)

fun calculatePasswordStrength(password: String): PasswordStrength {
    var score = 0
    if (password.length >= 6) score++
    if (password.length >= 10) score++
    if (password.any { it.isUpperCase() }) score++
    if (password.any { it.isLowerCase() }) score++
    if (password.any { it.isDigit() }) score++
    if (password.any { !it.isLetterOrDigit() }) score++

    return when {
        score <= 2 -> PasswordStrength(0.25f, "Weak", StrengthWeak)
        score <= 3 -> PasswordStrength(0.5f, "Fair", StrengthFair)
        score <= 4 -> PasswordStrength(0.75f, "Good", StrengthGood)
        else -> PasswordStrength(1f, "Strong", StrengthStrong)
    }
}

@Composable
fun PasswordStrengthMeter(password: String) {
    val strength = calculatePasswordStrength(password)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        LinearProgressIndicator(
            progress = { strength.score },
            modifier = Modifier
                .weight(1f)
                .height(8.dp),
            color = strength.color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap = StrokeCap.Round,
        )
        Text(
            text = strength.label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = strength.color
        )
    }
}
