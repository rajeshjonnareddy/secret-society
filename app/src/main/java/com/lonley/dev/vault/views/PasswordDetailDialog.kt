package com.lonley.dev.vault.views

import android.graphics.drawable.ColorDrawable
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.automirrored.outlined.Notes
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.lonley.dev.vault.model.PasswordEntry
import com.lonley.dev.vault.ui.theme.ThemeMode
import com.lonley.dev.vault.ui.theme.VaultTheme

private fun formatTimestamp(epochMs: Long): String {
    val sdf = java.text.SimpleDateFormat("MMM d, yyyy", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(epochMs))
}

@Composable
private fun DetailFieldRow(
    icon: ImageVector,
    iconTint: Color,
    label: String,
    value: String,
    trailingContent: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(iconTint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        trailingContent?.invoke()
    }
}

@Composable
fun PasswordDetailDialog(
    entry: PasswordEntry,
    startInEditMode: Boolean = false,
    themeMode: ThemeMode = ThemeMode.System,
    onDismiss: () -> Unit,
    onSave: (id: String, name: String, username: String, password: String, website: String?, comments: String?) -> Unit,
    onDelete: (id: String) -> Unit,
    onCopyToClipboard: (String) -> Unit
) {
    var isEditing by remember { mutableStateOf(startInEditMode) }
    var passwordVisible by remember { mutableStateOf(false) }

    var name by remember { mutableStateOf(entry.name) }
    var username by remember { mutableStateOf(entry.username) }
    var password by remember { mutableStateOf(entry.password) }
    var website by remember { mutableStateOf(entry.website ?: "") }
    var comments by remember { mutableStateOf(entry.comments ?: "") }

    var nameDirty by remember { mutableStateOf(false) }
    var usernameDirty by remember { mutableStateOf(false) }
    var passwordDirty by remember { mutableStateOf(false) }

    var showDeleteConfirm by remember { mutableStateOf(false) }

    val isFormValid = name.isNotBlank() && username.isNotBlank() && password.isNotBlank()
    val fieldShape = MaterialTheme.shapes.extraLarge

    val resetFields = {
        name = entry.name
        username = entry.username
        password = entry.password
        website = entry.website ?: ""
        comments = entry.comments ?: ""
        nameDirty = false
        usernameDirty = false
        passwordDirty = false
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        // Enable edge-to-edge on the dialog window and make background transparent
        val dialogView = LocalView.current
        SideEffect {
            val dialogWindow = dialogView.parent as? android.view.View
            dialogWindow?.let { view ->
                (view.context as? android.app.Dialog)?.window?.let { window ->
                    WindowCompat.setDecorFitsSystemWindows(window, false)
                    window.setLayout(
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.MATCH_PARENT
                    )
                    window.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
                    window.setDimAmount(0f)
                }
            }
        }

        VaultTheme(themeMode = themeMode) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 24.dp)
            ) {
                // ── Scrollable content ──
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(modifier = Modifier.height(12.dp))

                    // ── Brand heading with close button ──
                    val headingText = if (isEditing) "Edit entry" else entry.name
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = headingText.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.displayLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(
                            onClick = {
                                if (isEditing) {
                                    resetFields()
                                    isEditing = false
                                } else {
                                    onDismiss()
                                }
                            }
                        ) {
                            Text("Cancel")
                        }
                    }

                    Text(
                        text = if (isEditing) "Modify your saved credential." else "Your saved credential.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (!isEditing) {
                        Text(
                            text = "Added ${formatTimestamp(entry.createdAt)}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    if (isEditing) {
                        // ── Edit mode ──
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Name") },
                            singleLine = true,
                            isError = nameDirty && name.isBlank(),
                            supportingText = if (nameDirty && name.isBlank()) {
                                { Text("Name is required") }
                            } else null,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Badge,
                                    contentDescription = null
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { if (!it.isFocused) nameDirty = true },
                            shape = fieldShape
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text("Username") },
                            singleLine = true,
                            isError = usernameDirty && username.isBlank(),
                            supportingText = if (usernameDirty && username.isBlank()) {
                                { Text("Username is required") }
                            } else null,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Person,
                                    contentDescription = null
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { if (!it.isFocused) usernameDirty = true },
                            shape = fieldShape
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
                            singleLine = true,
                            isError = passwordDirty && password.isBlank(),
                            supportingText = if (passwordDirty && password.isBlank()) {
                                { Text("Password is required") }
                            } else null,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Lock,
                                    contentDescription = null
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                        contentDescription = if (passwordVisible) "Hide password" else "Show password"
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { if (!it.isFocused) passwordDirty = true },
                            shape = fieldShape
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = website,
                            onValueChange = { website = it },
                            label = { Text("Website (Optional)") },
                            singleLine = true,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Language,
                                    contentDescription = null
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = fieldShape
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = comments,
                            onValueChange = { comments = it },
                            label = { Text("Comments (Optional)") },
                            singleLine = false,
                            minLines = 2,
                            maxLines = 4,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.Notes,
                                    contentDescription = null
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = fieldShape
                        )
                    } else {
                        // ── View mode: consolidated GlassCard ──
                        val dividerColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)

                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                // Name row
                                DetailFieldRow(
                                    icon = Icons.Outlined.Badge,
                                    iconTint = MaterialTheme.colorScheme.primary,
                                    label = "Name",
                                    value = entry.name
                                )

                                // Divider
                                HorizontalDivider(
                                    thickness = 0.5.dp,
                                    color = dividerColor,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )

                                // Username row
                                DetailFieldRow(
                                    icon = Icons.Outlined.Person,
                                    iconTint = MaterialTheme.colorScheme.secondary,
                                    label = "Username",
                                    value = entry.username,
                                    trailingContent = {
                                        IconButton(onClick = { onCopyToClipboard(entry.username) }) {
                                            Icon(
                                                imageVector = Icons.Default.ContentCopy,
                                                contentDescription = "Copy username",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                )

                                // Divider
                                HorizontalDivider(
                                    thickness = 0.5.dp,
                                    color = dividerColor,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )

                                // Password row
                                DetailFieldRow(
                                    icon = Icons.Outlined.Lock,
                                    iconTint = MaterialTheme.colorScheme.tertiary,
                                    label = "Password",
                                    value = if (passwordVisible) entry.password else "\u2022".repeat(entry.password.length.coerceAtMost(16)),
                                    trailingContent = {
                                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                            Icon(
                                                imageVector = if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                                contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        IconButton(onClick = { onCopyToClipboard(entry.password) }) {
                                            Icon(
                                                imageVector = Icons.Default.ContentCopy,
                                                contentDescription = "Copy password",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                )

                                // Website row (conditional)
                                if (!entry.website.isNullOrBlank()) {
                                    HorizontalDivider(
                                        thickness = 0.5.dp,
                                        color = dividerColor,
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )

                                    DetailFieldRow(
                                        icon = Icons.Outlined.Language,
                                        iconTint = MaterialTheme.colorScheme.primary,
                                        label = "Website",
                                        value = entry.website
                                    )
                                }

                                // Comments row (conditional)
                                if (!entry.comments.isNullOrBlank()) {
                                    HorizontalDivider(
                                        thickness = 0.5.dp,
                                        color = dividerColor,
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )

                                    DetailFieldRow(
                                        icon = Icons.AutoMirrored.Outlined.Notes,
                                        iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        label = "Comments",
                                        value = entry.comments
                                    )
                                }
                            }
                        }
                    }

                    // Bottom spacing so content doesn't sit behind buttons
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // ── Fixed bottom buttons ──
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(bottom = 24.dp)
                ) {
                    if (isEditing) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            FilledTonalButton(
                                onClick = {
                                    resetFields()
                                    isEditing = false
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
                                    onSave(entry.id, name, username, password, website.ifBlank { null }, comments.ifBlank { null })
                                },
                                enabled = isFormValid,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp),
                                shape = MaterialTheme.shapes.extraLarge
                            ) {
                                Text(
                                    text = "Save",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    } else {
                        Button(
                            onClick = { isEditing = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = MaterialTheme.shapes.extraLarge
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Edit Entry",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        FilledTonalButton(
                            onClick = { showDeleteConfirm = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = MaterialTheme.shapes.extraLarge,
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Delete Entry",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteConfirm) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Entry") },
            text = { Text("Are you sure you want to delete \"${entry.name}\"? This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete(entry.id)
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
