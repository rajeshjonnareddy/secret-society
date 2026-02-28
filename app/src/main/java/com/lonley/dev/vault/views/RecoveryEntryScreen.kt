package com.lonley.dev.vault.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lonley.dev.vault.util.HapticHelper

@Composable
fun RecoveryEntryScreen(
    errorMessage: String? = null,
    hapticsEnabled: Boolean = false,
    onRecover: (words: List<String>, newPassword: CharArray) -> Unit,
    onCancel: () -> Unit
) {
    val view = LocalView.current
    val wordFields = remember { Array(12) { mutableStateOf("") } }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    val allWordsFilled = wordFields.all { it.value.isNotBlank() }
    val passwordsMatch = newPassword == confirmPassword
    val isFormValid = allWordsFilled && newPassword.isNotBlank() && confirmPassword.isNotBlank() && passwordsMatch

    val fieldShape = MaterialTheme.shapes.extraLarge

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Recover Vault",
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Enter your 12-word recovery phrase and set a new master password.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            // 12 word fields in 2-column grid (6 rows × 2 columns)
            for (row in 0 until 6) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (col in 0 until 2) {
                        val index = row * 2 + col
                        OutlinedTextField(
                            value = wordFields[index].value,
                            onValueChange = { wordFields[index].value = it.lowercase().trim() },
                            label = { Text("Word ${index + 1}") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = fieldShape
                        )
                    }
                }
                if (row < 5) Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // New password
            PasswordTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = "New Master Password",
                modifier = Modifier.fillMaxWidth(),
                shape = fieldShape
            )

            if (newPassword.isNotEmpty()) {
                PasswordStrengthMeter(password = newPassword)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Confirm password
            PasswordTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = "Confirm Password",
                isError = confirmPassword.isNotBlank() && !passwordsMatch,
                supportingText = if (confirmPassword.isNotBlank() && !passwordsMatch) {
                    { Text("Passwords do not match") }
                } else null,
                modifier = Modifier.fillMaxWidth(),
                shape = fieldShape
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        HapticHelper.performClick(view, hapticsEnabled)
                        onCancel()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    Text(
                        text = "Cancel",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Button(
                    onClick = {
                        HapticHelper.performClick(view, hapticsEnabled)
                        val words = wordFields.map { it.value }
                        onRecover(words, newPassword.toCharArray())
                    },
                    enabled = isFormValid,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    Text(
                        text = "Recover",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(120.dp))
        }
    }
}

