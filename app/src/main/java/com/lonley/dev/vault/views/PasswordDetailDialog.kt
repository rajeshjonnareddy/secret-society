package com.lonley.dev.vault.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.basicMarquee
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material3.Surface
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
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.drop
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import com.lonley.dev.vault.model.EntryType
import com.lonley.dev.vault.model.Network
import com.lonley.dev.vault.model.PasswordEntry
import com.lonley.dev.vault.model.PlanType
import com.lonley.dev.vault.model.SettingsState
import com.lonley.dev.vault.model.formatPrice
import com.lonley.dev.vault.model.nextRenewalDate
import com.lonley.dev.vault.util.HapticHelper
import com.lonley.dev.vault.util.formatTimestamp

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
    var walletAddressVisible by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

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
                    maxLines = 1,
                    modifier = Modifier
                        .weight(1f)
                        .basicMarquee(
                            iterations = Int.MAX_VALUE,
                            initialDelayMillis = 1000,
                            repeatDelayMillis = 2000
                        )
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
                text = when {
                    entry.isSubscription -> "Your subscription."
                    entry.entryType == EntryType.CryptoWallet -> "Your digital wallet."
                    else -> "Your saved credential."
                },
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

                        // Username row (conditional)
                        if (!entry.username.isNullOrBlank()) {
                            HorizontalDivider(
                                thickness = 0.5.dp,
                                color = dividerColor,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )

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
                        }

                        // Email row (conditional)
                        if (!entry.email.isNullOrBlank()) {
                            HorizontalDivider(
                                thickness = 0.5.dp,
                                color = dividerColor,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )

                            DetailFieldRow(
                                icon = Icons.Outlined.Email,
                                iconTint = MaterialTheme.colorScheme.primary,
                                label = "Email",
                                value = entry.email,
                                trailingContent = {
                                    IconButton(onClick = {
                                        HapticHelper.performClick(hapticView, settingsState.hapticsEnabled)
                                        onCopyToClipboard(entry.email)
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copy email",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            )
                        }

                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color = dividerColor,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        // Password / Digital Wallet row
                        when (entry.entryType) {
                            EntryType.CryptoWallet -> {
                                if (!entry.walletAddress.isNullOrBlank()) {
                                    val maskedAddress = if (entry.walletAddress.length > 10) {
                                        entry.walletAddress.take(6) + "••••" + entry.walletAddress.takeLast(4)
                                    } else {
                                        "••••••"
                                    }
                                    DetailFieldRow(
                                        icon = Icons.Outlined.AccountBalanceWallet,
                                        iconTint = MaterialTheme.colorScheme.primary,
                                        label = "Wallet Address",
                                        value = if (walletAddressVisible) entry.walletAddress else maskedAddress,
                                        trailingContent = {
                                            IconButton(onClick = {
                                                HapticHelper.performClick(hapticView, settingsState.hapticsEnabled)
                                                walletAddressVisible = !walletAddressVisible
                                            }) {
                                                Icon(
                                                    imageVector = if (walletAddressVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                                    contentDescription = if (walletAddressVisible) "Hide address" else "Show address",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                            IconButton(onClick = {
                                                HapticHelper.performClick(hapticView, settingsState.hapticsEnabled)
                                                onCopyToClipboard(entry.walletAddress)
                                            }) {
                                                Icon(
                                                    imageVector = Icons.Default.ContentCopy,
                                                    contentDescription = "Copy wallet address",
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
                                }
                                if (entry.network != null) {
                                    DetailFieldRow(
                                        icon = Icons.Outlined.Language,
                                        iconTint = MaterialTheme.colorScheme.primary,
                                        label = "Network",
                                        value = entry.network.label
                                    )
                                    HorizontalDivider(
                                        thickness = 0.5.dp,
                                        color = dividerColor,
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                }
                                if (!entry.exchange.isNullOrBlank()) {
                                    DetailFieldRow(
                                        icon = Icons.Outlined.Language,
                                        iconTint = MaterialTheme.colorScheme.primary,
                                        label = "Exchange",
                                        value = entry.exchange
                                    )
                                    HorizontalDivider(
                                        thickness = 0.5.dp,
                                        color = dividerColor,
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                }
                                if (entry.password.isNotBlank() && entry.password != (entry.seedPhrase ?: "")) {
                                    var walletPasswordVisible by remember { mutableStateOf(false) }
                                    DetailFieldRow(
                                        icon = Icons.Outlined.Lock,
                                        iconTint = MaterialTheme.colorScheme.primary,
                                        label = "Password",
                                        value = if (walletPasswordVisible) entry.password else "\u2022".repeat(entry.password.length.coerceAtMost(16)),
                                        trailingContent = {
                                            IconButton(onClick = {
                                                HapticHelper.performClick(hapticView, settingsState.hapticsEnabled)
                                                walletPasswordVisible = !walletPasswordVisible
                                            }) {
                                                Icon(
                                                    imageVector = if (walletPasswordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                                    contentDescription = if (walletPasswordVisible) "Hide password" else "Show password",
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
                                    HorizontalDivider(
                                        thickness = 0.5.dp,
                                        color = dividerColor,
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                }
                                PassphraseGridSection(
                                    passphrase = entry.seedPhrase ?: entry.password,
                                    visible = passwordVisible,
                                    onToggleVisibility = {
                                        HapticHelper.performClick(hapticView, settingsState.hapticsEnabled)
                                        passwordVisible = !passwordVisible
                                    },
                                    onCopy = {
                                        HapticHelper.performClick(hapticView, settingsState.hapticsEnabled)
                                        onCopyToClipboard(entry.seedPhrase ?: entry.password)
                                    }
                                )
                            }
                            else -> {
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
                            }
                        }

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
                                value = entry.website,
                                trailingContent = {
                                    IconButton(onClick = {
                                        HapticHelper.performClick(hapticView, settingsState.hapticsEnabled)
                                        onCopyToClipboard(entry.website)
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copy website",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
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
                                    iconTint = MaterialTheme.colorScheme.primary,
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
                                    iconTint = MaterialTheme.colorScheme.primary,
                                    label = "Price",
                                    value = "$${formatPrice(entry.price)}"
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
                                    iconTint = MaterialTheme.colorScheme.primary,
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
                                    iconTint = MaterialTheme.colorScheme.primary,
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
                                    iconTint = MaterialTheme.colorScheme.primary,
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
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Notifications,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
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

                Spacer(modifier = Modifier.height(24.dp))
            }

        // Bottom buttons: Cancel + Edit side by side
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 24.dp),
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
        id: String, name: String, username: String, email: String, password: String,
        website: String?, comments: String?,
        isFavorite: Boolean, isSubscription: Boolean, planType: PlanType?,
        price: String?, subscriptionEmail: String?, startDate: Long?,
        reminderEnabled: Boolean,
        entryType: EntryType, phraseWordCount: Int?,
        walletAddress: String?, seedPhrase: String?, network: Network?, exchange: String?
    ) -> Unit,
    onDelete: (id: String) -> Unit = {},
    onToggleFavorite: () -> Unit = {}
) {
    val hapticView = LocalView.current
    val focusManager = LocalFocusManager.current
    val dateFocusRequester = remember { FocusRequester() }
    var passwordVisible by remember { mutableStateOf(false) }

    var name by remember(entry) { mutableStateOf(entry.name) }
    var username by remember(entry) { mutableStateOf(entry.username ?: "") }
    var email by remember(entry) { mutableStateOf(entry.email ?: "") }
    var password by remember(entry) {
        // For wallet entries, if password matches the seed phrase it's a legacy entry — treat as no password
        mutableStateOf(
            if (entry.entryType == EntryType.CryptoWallet && entry.password == (entry.seedPhrase ?: "")) ""
            else entry.password
        )
    }
    var website by remember(entry) { mutableStateOf(entry.website ?: "") }
    var comments by remember(entry) { mutableStateOf(entry.comments ?: "") }

    // Entry type fields
    var selectedEntryType by remember(entry) { mutableStateOf(entry.entryType) }

    // Digital wallet fields
    var walletAddress by remember(entry) { mutableStateOf(entry.walletAddress ?: "") }
    var seedPhraseWords by remember(entry) {
        val existingWords = if (entry.entryType == EntryType.CryptoWallet && !entry.seedPhrase.isNullOrBlank()) {
            entry.seedPhrase.trim().split("\\s+".toRegex())
        } else emptyList()
        mutableStateOf(List(24) { i -> existingWords.getOrElse(i) { "" } })
    }
    var selectedSeedWordCount by remember(entry) {
        mutableStateOf(
            if (entry.entryType == EntryType.CryptoWallet && !entry.seedPhrase.isNullOrBlank()) {
                val count = entry.seedPhrase.trim().split("\\s+".toRegex()).size
                if (count > 12) 24 else 12
            } else 12
        )
    }
    var selectedNetwork by remember(entry) { mutableStateOf(entry.network) }
    var editExchange by remember(entry) { mutableStateOf(entry.exchange ?: "") }

    // Subscription fields
    var isFavorite by remember(entry) { mutableStateOf(entry.isFavorite) }
    var isSubscription by remember(entry) { mutableStateOf(entry.isSubscription) }
    var selectedPlanType by remember(entry) { mutableStateOf(entry.planType) }
    var price by remember(entry) {
        val raw = entry.price ?: ""
        // Convert old "9.99" format to raw digits "999"
        mutableStateOf(if ('.' in raw) raw.replace(".", "") else raw)
    }
    var subscriptionEmail by remember(entry) { mutableStateOf(entry.subscriptionEmail ?: "") }
    var startDate by remember(entry) { mutableStateOf(entry.startDate) }
    var reminderEnabled by remember(entry) { mutableStateOf(entry.reminderEnabled) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    var nameDirty by remember { mutableStateOf(false) }
    var passwordDirty by remember { mutableStateOf(false) }

    val hasSeedPhrase = seedPhraseWords.take(selectedSeedWordCount).all { it.isNotBlank() }
    val isFormValid = name.isNotBlank() && when (selectedEntryType) {
        EntryType.Password -> password.isNotBlank()
        EntryType.CryptoWallet -> walletAddress.isNotBlank() || hasSeedPhrase
        else -> false
    }
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
                text = name.ifBlank { entry.name }.replaceFirstChar { it.uppercaseChar() },
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                modifier = Modifier
                    .weight(1f)
                    .basicMarquee(
                        iterations = Int.MAX_VALUE,
                        initialDelayMillis = 1000,
                        repeatDelayMillis = 2000
                    )
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
            text = "Edit entry",
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
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
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
                label = { Text("Username (Optional)") },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = fieldShape
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email (Optional)") },
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

            Spacer(modifier = Modifier.height(16.dp))

            // Entry type selector
            Text(
                text = "Type",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = selectedEntryType == EntryType.Password,
                    onClick = {
                        HapticHelper.performClick(hapticView, settingsState.hapticsEnabled)
                        selectedEntryType = EntryType.Password
                    },
                    label = { Text("Password") }
                )
                FilterChip(
                    selected = selectedEntryType == EntryType.CryptoWallet,
                    onClick = {
                        HapticHelper.performClick(hapticView, settingsState.hapticsEnabled)
                        selectedEntryType = EntryType.CryptoWallet
                    },
                    label = { Text("Digital Wallet") }
                )
            }

            AnimatedVisibility(visible = selectedEntryType == EntryType.CryptoWallet) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = walletAddress,
                        onValueChange = { walletAddress = it },
                        label = { Text("Wallet Address") },
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.AccountBalanceWallet,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = fieldShape
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SeedPhraseInput(
                        words = seedPhraseWords,
                        wordCount = selectedSeedWordCount,
                        onWordChange = { index, value ->
                            seedPhraseWords = seedPhraseWords.toMutableList().also { it[index] = value }
                        },
                        onWordCountChange = { selectedSeedWordCount = it },
                        fieldShape = fieldShape,
                        hapticsEnabled = settingsState.hapticsEnabled
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    NetworkDropdown(
                        selectedNetwork = selectedNetwork,
                        onNetworkSelected = { selectedNetwork = it },
                        fieldShape = fieldShape,
                        hapticsEnabled = settingsState.hapticsEnabled
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = editExchange,
                        onValueChange = { editExchange = it },
                        label = { Text("Exchange (Optional)") },
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Language,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = fieldShape
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    PasswordTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Password (Optional)",
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = fieldShape
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (selectedEntryType) {
                EntryType.Password -> {
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
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = {
                                HapticHelper.performClick(hapticView, settingsState.hapticsEnabled)
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
                            .onFocusChanged { if (!it.isFocused) passwordDirty = true },
                        shape = fieldShape
                    )
                }
                EntryType.CryptoWallet -> {
                    // Seed phrase fields are in the wallet section above
                }
                else -> { /* Passphrase entries are migrated to CryptoWallet */ }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = website,
                onValueChange = { website = it },
                label = { Text("Website (Optional)") },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Language,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
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
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
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

                    val priceDisplayText = if (price.isEmpty()) "" else {
                        val cents = price.toLongOrNull() ?: 0L
                        "%.2f".format(cents / 100.0)
                    }
                    OutlinedTextField(
                        value = TextFieldValue(
                            text = priceDisplayText,
                            selection = TextRange(priceDisplayText.length)
                        ),
                        onValueChange = { newValue ->
                            val digitsOnly = newValue.text.filter { it.isDigit() }
                            if (digitsOnly.length <= 7) {
                                price = digitsOnly
                            }
                        },
                        label = { Text("Price (Optional)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                    val dateText = if (startDate != null) formatTimestamp(startDate!!) else "Select Start Date"

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
                            .focusRequester(dateFocusRequester)
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
                .padding(top = 16.dp, bottom = 24.dp),
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
                    val finalPassword = password
                    onSave(
                        entry.id, name, username, email, finalPassword,
                        website.ifBlank { null }, comments.ifBlank { null },
                        isFavorite, isSubscription, selectedPlanType,
                        price.ifBlank { null }, subscriptionEmail.ifBlank { null },
                        startDate, reminderEnabled,
                        selectedEntryType,
                        null,
                        if (selectedEntryType == EntryType.CryptoWallet) walletAddress else null,
                        if (selectedEntryType == EntryType.CryptoWallet) seedPhraseWords.take(selectedSeedWordCount).joinToString(" ") { it.trim() } else null,
                        if (selectedEntryType == EntryType.CryptoWallet) selectedNetwork else null,
                        if (selectedEntryType == EntryType.CryptoWallet) editExchange.ifBlank { null } else null
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
            onDismissRequest = {
                showDatePicker = false
                focusManager.clearFocus()
            },
            confirmButton = {
                TextButton(onClick = {
                    startDate = datePickerState.selectedDateMillis
                    showDatePicker = false
                    focusManager.clearFocus()
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDatePicker = false
                    focusManager.clearFocus()
                }) {
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

@Composable
private fun PassphraseGridSection(
    passphrase: String,
    visible: Boolean,
    onToggleVisibility: () -> Unit,
    onCopy: () -> Unit
) {
    val words = passphrase.trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }
    val columns = 3

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Seed Phrase",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${words.size}-word seed phrase",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onToggleVisibility) {
                Icon(
                    imageVector = if (visible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                    contentDescription = if (visible) "Hide seed phrase" else "Show seed phrase",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            IconButton(onClick = onCopy) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy seed phrase",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        val blurRadius by animateDpAsState(
            targetValue = if (visible) 0.dp else 12.dp,
            label = "passphrase-blur"
        )

        val rowCount = (words.size + columns - 1) / columns
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.blur(blurRadius)
        ) {
            for (row in 0 until rowCount) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (col in 0 until columns) {
                        val index = row * columns + col
                        if (index < words.size) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "${index + 1}.",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = words[index],
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}
