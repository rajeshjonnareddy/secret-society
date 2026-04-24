package com.lonley.dev.vault.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material3.FilterChip
import com.lonley.dev.vault.crypto.Bip39Wordlist
import com.lonley.dev.vault.util.HapticHelper
import java.security.SecureRandom

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordGeneratorDialog(
    onDismiss: () -> Unit,
    onAddPassword: (String) -> Unit,
    hapticsEnabled: Boolean = false,
    onInteraction: () -> Unit = {}
) {
    val view = LocalView.current
    val clipboardManager = LocalClipboardManager.current

    var selectedMode by remember { mutableStateOf("Random") }

    // Random mode options
    var includeUppercase by remember { mutableStateOf(true) }
    var includeLowercase by remember { mutableStateOf(true) }
    var includeNumbers by remember { mutableStateOf(true) }
    var includeSpecial by remember { mutableStateOf(false) }
    var length by remember { mutableFloatStateOf(16f) }

    // Passphrase mode options
    var passphraseWordCount by remember { mutableStateOf(12) }
    var separator by remember { mutableStateOf("-") }

    // PIN mode options
    var pinLength by remember { mutableFloatStateOf(6f) }

    var generatedPassword by remember { mutableStateOf("") }

    BasicAlertDialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp,
            modifier = Modifier.widthIn(max = 360.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Generate Password",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Mode selector
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf("Random", "Passphrase", "PIN").forEach { mode ->
                        FilterChip(
                            selected = selectedMode == mode,
                            onClick = {
                                HapticHelper.performClick(view, hapticsEnabled)
                                selectedMode = mode
                                generatedPassword = ""
                                onInteraction()
                            },
                            label = { Text(mode) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                when (selectedMode) {
                    "Random" -> {
                        // Toggle rows
                        ToggleRow(
                            label = "Uppercase (A-Z)",
                            checked = includeUppercase,
                            onCheckedChange = {
                                HapticHelper.performClick(view, hapticsEnabled)
                                includeUppercase = it
                                onInteraction()
                            }
                        )
                        ToggleRow(
                            label = "Lowercase (a-z)",
                            checked = includeLowercase,
                            onCheckedChange = {
                                HapticHelper.performClick(view, hapticsEnabled)
                                includeLowercase = it
                                onInteraction()
                            }
                        )
                        ToggleRow(
                            label = "Numbers (0-9)",
                            checked = includeNumbers,
                            onCheckedChange = {
                                HapticHelper.performClick(view, hapticsEnabled)
                                includeNumbers = it
                                onInteraction()
                            }
                        )
                        ToggleRow(
                            label = "Special (!@#\$%...)",
                            checked = includeSpecial,
                            onCheckedChange = {
                                HapticHelper.performClick(view, hapticsEnabled)
                                includeSpecial = it
                                onInteraction()
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Length: ${length.toInt()}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Slider(
                            value = length,
                            onValueChange = { length = it; onInteraction() },
                            valueRange = 8f..64f,
                            steps = 55,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    "Passphrase" -> {
                        Text(
                            text = "Word Count",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = passphraseWordCount == 12,
                                onClick = {
                                    HapticHelper.performClick(view, hapticsEnabled)
                                    passphraseWordCount = 12
                                    onInteraction()
                                },
                                label = { Text("12 Words") }
                            )
                            FilterChip(
                                selected = passphraseWordCount == 24,
                                onClick = {
                                    HapticHelper.performClick(view, hapticsEnabled)
                                    passphraseWordCount = 24
                                    onInteraction()
                                },
                                label = { Text("24 Words") }
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Separator",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("-", ".", "_", " ").forEach { sep ->
                                FilterChip(
                                    selected = separator == sep,
                                    onClick = {
                                        HapticHelper.performClick(view, hapticsEnabled)
                                        separator = sep
                                        onInteraction()
                                    },
                                    label = { Text(if (sep == " ") "space" else sep) }
                                )
                            }
                        }
                    }
                    "PIN" -> {
                        Text(
                            text = "Length: ${pinLength.toInt()}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Slider(
                            value = pinLength,
                            onValueChange = { pinLength = it; onInteraction() },
                            valueRange = 4f..8f,
                            steps = 3,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Generate button
                val canGenerate = when (selectedMode) {
                    "Random" -> includeUppercase || includeLowercase || includeNumbers || includeSpecial
                    else -> true
                }
                Button(
                    onClick = {
                        HapticHelper.performClick(view, hapticsEnabled)
                        onInteraction()
                        generatedPassword = when (selectedMode) {
                            "Passphrase" -> generatePassphrase(passphraseWordCount, separator)
                            "PIN" -> generatePin(pinLength.toInt())
                            else -> generatePassword(
                                length = length.toInt(),
                                upper = includeUppercase,
                                lower = includeLowercase,
                                numbers = includeNumbers,
                                special = includeSpecial
                            )
                        }
                    },
                    enabled = canGenerate,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    Text(
                        text = if (generatedPassword.isEmpty()) "Generate" else "Regenerate",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Generated password display
                if (generatedPassword.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = generatedPassword,
                        onValueChange = {},
                        readOnly = true,
                        singleLine = true,
                        label = { Text("Generated Password") },
                        trailingIcon = {
                            IconButton(onClick = {
                                HapticHelper.performClick(view, hapticsEnabled)
                                onInteraction()
                                clipboardManager.setText(AnnotatedString(generatedPassword))
                            }) {
                                Icon(
                                    imageVector = Icons.Outlined.ContentCopy,
                                    contentDescription = "Copy password",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    FilledTonalButton(
                        onClick = {
                            HapticHelper.performClick(view, hapticsEnabled)
                            onInteraction()
                            onAddPassword(generatedPassword)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = MaterialTheme.shapes.extraLarge
                    ) {
                        Text(
                            text = "Add Password",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = {
                        HapticHelper.performClick(view, hapticsEnabled)
                        onInteraction()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    Text(
                        text = "Cancel",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun ToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

private fun generatePassword(
    length: Int,
    upper: Boolean,
    lower: Boolean,
    numbers: Boolean,
    special: Boolean
): String {
    val upperChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    val lowerChars = "abcdefghijklmnopqrstuvwxyz"
    val numberChars = "0123456789"
    val specialChars = "!@#\$%^&*()-_=+[]{}|;:,.<>?"

    val pool = buildString {
        if (upper) append(upperChars)
        if (lower) append(lowerChars)
        if (numbers) append(numberChars)
        if (special) append(specialChars)
    }

    if (pool.isEmpty()) return ""

    val random = SecureRandom()
    return (1..length)
        .map { pool[random.nextInt(pool.length)] }
        .toCharArray()
        .let { String(it) }
}

private fun generatePassphrase(wordCount: Int, separator: String): String {
    val random = SecureRandom()
    val words = Bip39Wordlist.words
    return (1..wordCount)
        .map { words[random.nextInt(words.size)] }
        .joinToString(separator)
}

private fun generatePin(length: Int): String {
    val random = SecureRandom()
    return (1..length)
        .map { random.nextInt(10) }
        .joinToString("")
}
