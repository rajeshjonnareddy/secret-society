package com.lonley.dev.vault.views

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AttachEmail
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DriveFileRenameOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lonley.dev.vault.model.SettingsState
import com.lonley.dev.vault.util.formatRelativeTime
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun UserProfileScreen(
    username: String,
    email: String,
    encryptionType: String,
    lastUpdatedAt: Long = 0L,
    lastExportedAt: Long = 0L,
    settingsState: SettingsState,
    hasRecovery: Boolean = false,
    vaultName: String = "Vault",
    onVaultNameChange: (String) -> Unit = {},
    onSetupRecovery: () -> Unit = {},
    onChangePassword: () -> Unit = {},
    onExportData: () -> Unit = {}
) {
    var isEditingName by remember { mutableStateOf(false) }
    var editedName by remember(vaultName) { mutableStateOf(vaultName) }
    val lastUpdatedText = remember(lastUpdatedAt) { formatRelativeTime(lastUpdatedAt) }
    val lastExportedText = remember(lastExportedAt) { formatExportTimestamp(lastExportedAt) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Profile",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                modifier = Modifier.basicMarquee(
                    iterations = Int.MAX_VALUE,
                    initialDelayMillis = 1000,
                    repeatDelayMillis = 2000
                )
            )
            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                // ── Vault Info ──
                ProfileSectionHeader("Vault Info")
                Spacer(modifier = Modifier.height(8.dp))

                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        if (isEditingName) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.DriveFileRenameOutline,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                OutlinedTextField(
                                    value = editedName,
                                    onValueChange = { editedName = it },
                                    label = { Text("Vault Name") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = {
                                    if (editedName.isNotBlank()) {
                                        onVaultNameChange(editedName.trim())
                                        isEditingName = false
                                    }
                                }) {
                                    Icon(
                                        imageVector = Icons.Outlined.Check,
                                        contentDescription = "Save",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                IconButton(onClick = {
                                    editedName = vaultName
                                    isEditingName = false
                                }) {
                                    Icon(
                                        imageVector = Icons.Outlined.Close,
                                        contentDescription = "Cancel",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.DriveFileRenameOutline,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Vault Name",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = vaultName,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                IconButton(onClick = {
                                    editedName = vaultName
                                    isEditingName = true
                                }) {
                                    Icon(
                                        imageVector = Icons.Outlined.Edit,
                                        contentDescription = "Edit vault name",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                        ProfileDivider()
                        ProfileInfoRow(label = "Username", value = username.ifEmpty { "Not set" }, icon = Icons.Outlined.Person)
                        ProfileDivider()
                        ProfileInfoRow(label = "Email", value = email.ifEmpty { "Not set" }, icon = Icons.Outlined.AttachEmail)
                        ProfileDivider()
                        ProfileInfoRow(label = "Encryption", value = encryptionType, icon = Icons.Outlined.Lock)
                        ProfileDivider()
                        ProfileInfoRow(label = "Last Updated", value = lastUpdatedText, icon = Icons.Outlined.Update)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ── Account ──
                ProfileSectionHeader("Account")
                Spacer(modifier = Modifier.height(8.dp))

                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        ProfileSettingsRow(
                            icon = Icons.Outlined.Shield,
                            title = "Recovery Phrase",
                            badge = if (hasRecovery) "Secured" else "Not Secured",
                            badgeColor = if (hasRecovery) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            onClick = if (hasRecovery) null else onSetupRecovery
                        )
                        ProfileDivider()
                        ProfileSettingsRow(
                            icon = Icons.Outlined.Key,
                            title = "Change Master Password",
                            onClick = onChangePassword
                        )
                        ProfileDivider()
                        ProfileSettingsRow(
                            icon = Icons.Outlined.UploadFile,
                            title = "Export Data",
                            badge = lastExportedText,
                            badgeColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            onClick = onExportData
                        )
                    }
                }

                // Bottom spacing for toolbar
                Spacer(modifier = Modifier.height(120.dp))
            }
        }
}

// ── Private composables ──

@Composable
private fun ProfileSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun ProfileInfoRow(label: String, value: String, icon: ImageVector? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ProfileSettingsRow(
    icon: ImageVector,
    title: String,
    badge: String? = null,
    badgeColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        if (badge != null) {
            Text(
                text = badge,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = badgeColor
            )
        }
    }
}

@Composable
private fun ProfileDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    )
}

private fun formatExportTimestamp(timestampMs: Long): String {
    if (timestampMs == 0L) return "Never"
    val sdf = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
    return sdf.format(Date(timestampMs))
}
