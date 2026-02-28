package com.lonley.dev.vault.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ChangePasswordScreen(
    errorMessage: String? = null,
    onChangePassword: (oldPassword: CharArray, newPassword: CharArray) -> Unit,
    onCancel: () -> Unit
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    val displayError = errorMessage ?: validationError

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Change Password",
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Enter your current password and choose a new one. Your vault will be re-encrypted with the new password.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            PasswordTextField(
                value = currentPassword,
                onValueChange = {
                    currentPassword = it
                    validationError = null
                },
                label = "Current Password",
                isError = errorMessage != null,
                supportingText = if (errorMessage != null) {
                    { Text(errorMessage) }
                } else null,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            PasswordTextField(
                value = newPassword,
                onValueChange = {
                    newPassword = it
                    validationError = null
                },
                label = "New Password",
                modifier = Modifier.fillMaxWidth()
            )

            if (newPassword.isNotEmpty()) {
                PasswordStrengthMeter(password = newPassword)
            }

            Spacer(modifier = Modifier.height(16.dp))

            PasswordTextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    validationError = null
                },
                label = "Confirm New Password",
                isError = confirmPassword.isNotBlank() && newPassword != confirmPassword,
                supportingText = if (confirmPassword.isNotBlank() && newPassword != confirmPassword) {
                    { Text("Passwords do not match") }
                } else if (displayError != null && errorMessage == null) {
                    { Text(displayError) }
                } else null,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        Button(
            onClick = {
                when {
                    currentPassword.isEmpty() -> {
                        validationError = "Current password cannot be empty"
                    }
                    newPassword.isEmpty() -> {
                        validationError = "New password cannot be empty"
                    }
                    newPassword != confirmPassword -> {
                        validationError = "New passwords do not match"
                    }
                    newPassword == currentPassword -> {
                        validationError = "New password must be different from current password"
                    }
                    else -> {
                        validationError = null
                        onChangePassword(
                            currentPassword.toCharArray(),
                            newPassword.toCharArray()
                        )
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
                .height(56.dp),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Text(
                text = "Change Password",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
