package com.lonley.dev.vault.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import android.os.Build
import com.lonley.dev.vault.ui.theme.StrengthFair
import com.lonley.dev.vault.ui.theme.StrengthGood
import com.lonley.dev.vault.ui.theme.StrengthStrong
import com.lonley.dev.vault.ui.theme.StrengthWeak
import com.lonley.dev.vault.ui.theme.VaultTheme
import com.lonley.dev.vault.util.HapticHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateVaultContent(
    onConfirm: (vaultName: String, username: String, email: String, masterPassword: String, encryptionType: String) -> Unit,
    onCancel: () -> Unit,
    hapticsEnabled: Boolean = false
) {
    val view = LocalView.current
    var vaultName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var masterPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var masterPasswordFocused by remember { mutableStateOf(false) }

    // Dirty tracking — errors show only after the user has left the field
    var vaultNameDirty by remember { mutableStateOf(false) }
    var usernameDirty by remember { mutableStateOf(false) }
    var masterPasswordDirty by remember { mutableStateOf(false) }
    var confirmPasswordDirty by remember { mutableStateOf(false) }

    val encryptionTypes = buildList {
        add("AES-256-GCM")
        add("AES-256-CBC")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            add("ChaCha20-Poly1305")
        }
    }
    var selectedEncryption by remember { mutableStateOf(encryptionTypes[0]) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    val passwordsMatch = masterPassword == confirmPassword
    val isFormValid = vaultName.isNotBlank() && username.isNotBlank() &&
            masterPassword.isNotBlank() && confirmPassword.isNotBlank() && passwordsMatch

    val fieldShape = MaterialTheme.shapes.extraLarge

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        Text(
            text = "Create Vault",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Set up your secure, encrypted vault.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Vault Name
        OutlinedTextField(
            value = vaultName,
            onValueChange = { vaultName = it },
            label = { Text("Vault Name") },
            singleLine = true,
            isError = vaultNameDirty && vaultName.isBlank(),
            supportingText = if (vaultNameDirty && vaultName.isBlank()) {
                { Text("Vault name is required") }
            } else null,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { if (!it.isFocused) vaultNameDirty = true },
            shape = fieldShape
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Username
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            singleLine = true,
            isError = usernameDirty && username.isBlank(),
            supportingText = if (usernameDirty && username.isBlank()) {
                { Text("Username is required") }
            } else null,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { if (!it.isFocused) usernameDirty = true },
            shape = fieldShape
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email (Optional)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = fieldShape
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Master Password
        OutlinedTextField(
            value = masterPassword,
            onValueChange = { masterPassword = it },
            label = { Text("Master Password") },
            singleLine = true,
            isError = masterPasswordDirty && masterPassword.isBlank(),
            supportingText = if (masterPasswordDirty && masterPassword.isBlank()) {
                { Text("Master password is required") }
            } else null,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = {
                    HapticHelper.performClick(view, hapticsEnabled)
                    passwordVisible = !passwordVisible
                }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged {
                    masterPasswordFocused = it.isFocused
                    if (!it.isFocused) masterPasswordDirty = true
                },
            shape = fieldShape
        )

        // Info note — visible when master password is focused
        AnimatedVisibility(visible = masterPasswordFocused) {
            Row(
                modifier = Modifier.padding(top = 8.dp, start = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "This is your identity. Save it securely — it cannot be recovered.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }

        // Password strength meter
        if (masterPassword.isNotEmpty()) {
            PasswordStrengthMeter(password = masterPassword)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Confirm Password
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            singleLine = true,
            isError = confirmPasswordDirty && (confirmPassword.isBlank() || !passwordsMatch),
            supportingText = when {
                confirmPasswordDirty && confirmPassword.isBlank() -> {
                    { Text("Confirm password is required") }
                }
                confirmPasswordDirty && !passwordsMatch -> {
                    { Text("Passwords do not match") }
                }
                else -> null
            },
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = {
                    HapticHelper.performClick(view, hapticsEnabled)
                    confirmPasswordVisible = !confirmPasswordVisible
                }) {
                    Icon(
                        imageVector = if (confirmPasswordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged {
                    if (!it.isFocused && confirmPassword.isNotEmpty()) confirmPasswordDirty = true
                },
            shape = fieldShape
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Encryption Type Dropdown
        ExposedDropdownMenuBox(
            expanded = dropdownExpanded,
            onExpandedChange = { dropdownExpanded = it }
        ) {
            OutlinedTextField(
                value = selectedEncryption,
                onValueChange = {},
                readOnly = true,
                label = { Text("Encryption Type") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                shape = fieldShape
            )
            ExposedDropdownMenu(
                expanded = dropdownExpanded,
                onDismissRequest = { dropdownExpanded = false }
            ) {
                encryptionTypes.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type) },
                        onClick = {
                            HapticHelper.performClick(view, hapticsEnabled)
                            selectedEncryption = type
                            dropdownExpanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FilledTonalButton(
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
                    onConfirm(vaultName, username, email, masterPassword, selectedEncryption)
                },
                enabled = isFormValid,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Text(
                    text = "Create Vault",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun PasswordStrengthMeter(password: String) {
    val strength = calculateStrength(password)

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

private data class PasswordStrength(val score: Float, val label: String, val color: Color)

private fun calculateStrength(password: String): PasswordStrength {
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

@Preview(showBackground = true)
@Composable
private fun CreateVaultContentPreview() {
    VaultTheme {
        CreateVaultContent(
            onConfirm = { _, _, _, _, _ -> },
            onCancel = {}
        )
    }
}
