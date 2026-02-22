package com.lonley.dev.vault.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.automirrored.outlined.Notes
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.Subscriptions
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.drop
import com.lonley.dev.vault.model.PasswordEntry
import com.lonley.dev.vault.model.PlanType
import com.lonley.dev.vault.model.SettingsState
import com.lonley.dev.vault.model.nextRenewalDate
import com.lonley.dev.vault.util.HapticHelper

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
fun PasswordDetailScreen(
    entry: PasswordEntry,
    settingsState: SettingsState = SettingsState(),
    onDismiss: () -> Unit,
    onEditClick: () -> Unit,
    onDelete: (id: String) -> Unit,
    onCopyToClipboard: (String) -> Unit,
    onToggleFavorite: () -> Unit = {},
    onToggleReminder: () -> Unit = {}
) {
    val hapticView = LocalView.current
    var passwordVisible by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Header row: entry name + favorite star + delete icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = entry.name.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = {
                        HapticHelper.performClick(hapticView, settingsState.hapticsEnabled)
                        onToggleFavorite()
                    }
                ) {
                    Icon(
                        imageVector = if (entry.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = if (entry.isFavorite) "Remove from favorites" else "Add to favorites",
                        tint = if (entry.isFavorite) Color(0xFFFFC107) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = {
                        HapticHelper.performClick(hapticView, settingsState.hapticsEnabled)
                        showDeleteConfirm = true
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete entry",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Text(
                text = if (entry.isSubscription) "Your subscription." else "Your saved credential.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Added ${formatTimestamp(entry.createdAt)}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Scrollable content
            val scrollState = rememberScrollState()

            LaunchedEffect(settingsState.hapticsEnabled, settingsState.scrollVibrations) {
                if (settingsState.hapticsEnabled && settingsState.scrollVibrations["passwordDetail"] == true) {
                    snapshotFlow { scrollState.value / 50 }
                        .drop(1)
                        .collect { HapticHelper.performScrollTick(hapticView, true) }
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
            ) {
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

                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color = dividerColor,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        // Username row
                        DetailFieldRow(
                            icon = Icons.Outlined.Person,
                            iconTint = MaterialTheme.colorScheme.primary,
                            label = "Username",
                            value = entry.username,
                            trailingContent = {
                                IconButton(onClick = {
                                    HapticHelper.performClick(hapticView, settingsState.hapticsEnabled)
                                    onCopyToClipboard(entry.username)
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy username",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        )

                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color = dividerColor,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        // Password row
                        DetailFieldRow(
                            icon = Icons.Outlined.Lock,
                            iconTint = MaterialTheme.colorScheme.primary,
                            label = "Password",
                            value = if (passwordVisible) entry.password else "\u2022".repeat(entry.password.length.coerceAtMost(16)),
                            trailingContent = {
                                IconButton(onClick = {
                                    HapticHelper.performClick(hapticView, settingsState.hapticsEnabled)
                                    passwordVisible = !passwordVisible
                                }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                IconButton(onClick = {
                                    HapticHelper.performClick(hapticView, settingsState.hapticsEnabled)
                                    onCopyToClipboard(entry.password)
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy password",
                                        tint = MaterialTheme.colorScheme.primary,
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
                                iconTint = MaterialTheme.colorScheme.primary,
                                label = "Comments",
                                value = entry.comments
                            )
                        }

                        // Subscription details (conditional)
                        if (entry.isSubscription) {
                            if (entry.planType != null) {
                                HorizontalDivider(
                                    thickness = 0.5.dp,
                                    color = dividerColor,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                                DetailFieldRow(
                                    icon = Icons.Outlined.Repeat,
                                    iconTint = MaterialTheme.colorScheme.tertiary,
                                    label = "Plan Type",
                                    value = entry.planType.label
                                )
                            }

                            if (!entry.price.isNullOrBlank()) {
                                HorizontalDivider(
                                    thickness = 0.5.dp,
                                    color = dividerColor,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                                DetailFieldRow(
                                    icon = Icons.Outlined.AttachMoney,
                                    iconTint = MaterialTheme.colorScheme.tertiary,
                                    label = "Price",
                                    value = entry.price
                                )
                            }

                            if (!entry.subscriptionEmail.isNullOrBlank()) {
                                HorizontalDivider(
                                    thickness = 0.5.dp,
                                    color = dividerColor,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                                DetailFieldRow(
                                    icon = Icons.Outlined.Email,
                                    iconTint = MaterialTheme.colorScheme.tertiary,
                                    label = "Subscription Email",
                                    value = entry.subscriptionEmail
                                )
                            }

                            if (entry.startDate != null) {
                                HorizontalDivider(
                                    thickness = 0.5.dp,
                                    color = dividerColor,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                                DetailFieldRow(
                                    icon = Icons.Outlined.CalendarToday,
                                    iconTint = MaterialTheme.colorScheme.tertiary,
                                    label = "Start Date",
                                    value = formatTimestamp(entry.startDate)
                                )
                            }

                            val nextRenewal = entry.nextRenewalDate()
                            if (nextRenewal != null) {
                                HorizontalDivider(
                                    thickness = 0.5.dp,
                                    color = dividerColor,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                                DetailFieldRow(
                                    icon = Icons.Outlined.Event,
                                    iconTint = MaterialTheme.colorScheme.tertiary,
                                    label = "Next Renewal",
                                    value = formatTimestamp(nextRenewal)
                                )
                            }

                            HorizontalDivider(
                                thickness = 0.5.dp,
                                color = dividerColor,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
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
                                        .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Notifications,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.tertiary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Renewal Reminder",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (entry.reminderEnabled) "Enabled" else "Disabled",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Switch(
                                    checked = entry.reminderEnabled,
                                    onCheckedChange = {
                                        HapticHelper.performClick(hapticView, settingsState.hapticsEnabled)
                                        onToggleReminder()
                                    }
                                )
                            }
                        }
                    }
                }

                // Bottom spacing so content doesn't sit behind buttons
                Spacer(modifier = Modifier.height(100.dp))
            }
        }

        // Fixed bottom buttons: Cancel + Edit side by side
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = {
                    HapticHelper.performClick(hapticView, settingsState.hapticsEnabled)
                    onDismiss()
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
                    HapticHelper.performClick(hapticView, settingsState.hapticsEnabled)
                    onEditClick()
                },
                modifier = Modifier
                    .weight(1f)
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
        }
    }

    // Delete confirmation dialog
    if (showDeleteConfirm) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Entry") },
            text = { Text("Are you sure you want to delete \"${entry.name}\"? This cannot be undone.") },
            confirmButton = {
                val confirmView = LocalView.current
                TextButton(
                    onClick = {
                        HapticHelper.performClick(confirmView, settingsState.hapticsEnabled)
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
                val dismissView = LocalView.current
                TextButton(onClick = {
                    HapticHelper.performClick(dismissView, settingsState.hapticsEnabled)
                    showDeleteConfirm = false
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordEditScreen(
    entry: PasswordEntry,
    settingsState: SettingsState = SettingsState(),
    onCancel: () -> Unit,
    onSave: (
        id: String, name: String, username: String, password: String,
        website: String?, comments: String?,
        isFavorite: Boolean, isSubscription: Boolean, planType: PlanType?,
        price: String?, subscriptionEmail: String?, startDate: Long?,
        reminderEnabled: Boolean
    ) -> Unit,
    onDelete: (id: String) -> Unit = {},
    onToggleFavorite: () -> Unit = {}
) {
    val hapticView = LocalView.current
    var passwordVisible by remember { mutableStateOf(false) }

    var name by remember(entry) { mutableStateOf(entry.name) }
    var username by remember(entry) { mutableStateOf(entry.username) }
    var password by remember(entry) { mutableStateOf(entry.password) }
    var website by remember(entry) { mutableStateOf(entry.website ?: "") }
    var comments by remember(entry) { mutableStateOf(entry.comments ?: "") }

    // Subscription fields
    var isFavorite by remember(entry) { mutableStateOf(entry.isFavorite) }
    var isSubscription by remember(entry) { mutableStateOf(entry.isSubscription) }
    var selectedPlanType by remember(entry) { mutableStateOf(entry.planType) }
    var price by remember(entry) { mutableStateOf(entry.price ?: "") }
    var subscriptionEmail by remember(entry) { mutableStateOf(entry.subscriptionEmail ?: "") }
    var startDate by remember(entry) { mutableStateOf(entry.startDate) }
    var reminderEnabled by remember(entry) { mutableStateOf(entry.reminderEnabled) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    var nameDirty by remember { mutableStateOf(false) }
    var usernameDirty by remember { mutableStateOf(false) }
    var passwordDirty by remember { mutableStateOf(false) }

    val isFormValid = name.isNotBlank() && username.isNotBlank() && password.isNotBlank()
    val fieldShape = MaterialTheme.shapes.extraLarge

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Header row: title + favorite star + delete icon
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Edit entry",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = {
                    HapticHelper.performClick(hapticView, settingsState.hapticsEnabled)
                    isFavorite = !isFavorite
                    onToggleFavorite()
                }
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                    tint = if (isFavorite) Color(0xFFFFC107) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(
                onClick = {
                    HapticHelper.performClick(hapticView, settingsState.hapticsEnabled)
                    showDeleteConfirm = true
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete entry",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
        Text(
            text = if (isSubscription) "Modify your subscription." else "Modify your saved credential.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Scrollable form
        val scrollState = rememberScrollState()

        LaunchedEffect(settingsState.hapticsEnabled, settingsState.scrollVibrations) {
            if (settingsState.hapticsEnabled && settingsState.scrollVibrations["editEntry"] == true) {
                snapshotFlow { scrollState.value / 50 }
                    .drop(1)
                    .collect { HapticHelper.performScrollTick(hapticView, true) }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
        ) {
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
                    IconButton(onClick = {
                        HapticHelper.performClick(hapticView, settingsState.hapticsEnabled)
                        passwordVisible = !passwordVisible
                    }) {
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

            Spacer(modifier = Modifier.height(16.dp))

            // Subscription toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Subscriptions,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Subscription",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Switch(
                    checked = isSubscription,
                    onCheckedChange = {
                        HapticHelper.performClick(hapticView, settingsState.hapticsEnabled)
                        isSubscription = it
                    }
                )
            }

            AnimatedVisibility(visible = isSubscription) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))

                    // Plan type chips
                    Text(
                        text = "Plan Type",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PlanType.entries.forEach { plan ->
                            FilterChip(
                                selected = selectedPlanType == plan,
                                onClick = {
                                    HapticHelper.performClick(hapticView, settingsState.hapticsEnabled)
                                    selectedPlanType = plan
                                },
                                label = { Text(plan.label) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it },
                        label = { Text("Price (Optional)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.AttachMoney,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = fieldShape
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = subscriptionEmail,
                        onValueChange = { subscriptionEmail = it },
                        label = { Text("Subscription Email (Optional)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Email,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = fieldShape
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Start date
                    val dateText = if (startDate != null) {
                        val sdf = java.text.SimpleDateFormat("MMM d, yyyy", java.util.Locale.getDefault())
                        sdf.format(java.util.Date(startDate!!))
                    } else "Select Start Date"

                    OutlinedTextField(
                        value = dateText,
                        onValueChange = {},
                        label = { Text("Start Date") },
                        readOnly = true,
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.CalendarToday,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { if (it.isFocused) showDatePicker = true },
                        shape = fieldShape
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Reminder toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Notifications,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Remind before renewal",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Switch(
                            checked = reminderEnabled,
                            onCheckedChange = {
                                HapticHelper.performClick(hapticView, settingsState.hapticsEnabled)
                                reminderEnabled = it
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Fixed bottom buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FilledTonalButton(
                onClick = {
                    HapticHelper.performClick(hapticView, settingsState.hapticsEnabled)
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
                    HapticHelper.performClick(hapticView, settingsState.hapticsEnabled)
                    onSave(
                        entry.id, name, username, password,
                        website.ifBlank { null }, comments.ifBlank { null },
                        isFavorite, isSubscription, selectedPlanType,
                        price.ifBlank { null }, subscriptionEmail.ifBlank { null },
                        startDate, reminderEnabled
                    )
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
    }

    // Date picker dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = startDate ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    startDate = datePickerState.selectedDateMillis
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Delete confirmation dialog
    if (showDeleteConfirm) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Entry") },
            text = { Text("Are you sure you want to delete \"${entry.name}\"? This cannot be undone.") },
            confirmButton = {
                val confirmView = LocalView.current
                TextButton(
                    onClick = {
                        HapticHelper.performClick(confirmView, settingsState.hapticsEnabled)
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
                val dismissView = LocalView.current
                TextButton(onClick = {
                    HapticHelper.performClick(dismissView, settingsState.hapticsEnabled)
                    showDeleteConfirm = false
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}
