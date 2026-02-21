package com.lonley.dev.vault.views

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AttachEmail
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lonley.dev.vault.model.SettingsState
import com.lonley.dev.vault.util.HapticHelper
import androidx.compose.ui.platform.LocalView

@Composable
fun UserProfileScreen(
    username: String,
    email: String,
    encryptionType: String,
    lastUpdatedAt: Long = 0L,
    settingsState: SettingsState,
    onBackClick: () -> Unit,
    onLockClick: () -> Unit,
    onDownloadClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val view = LocalView.current

    val lastUpdatedText = remember(lastUpdatedAt) {
        if (lastUpdatedAt == 0L) "Never" else {
            val diff = System.currentTimeMillis() - lastUpdatedAt
            val minutes = diff / 60_000
            val hours = minutes / 60
            val days = hours / 24
            when {
                minutes < 1 -> "Just now"
                minutes < 60 -> "${minutes}m ago"
                hours < 24 -> "${hours}h ago"
                days < 30 -> "${days}d ago"
                days < 365 -> "${days / 30}mo ago"
                else -> "${days / 365}y ago"
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
                color = MaterialTheme.colorScheme.primary
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
                            icon = Icons.Outlined.Key,
                            title = "Change Master Password"
                        )
                        ProfileDivider()
                        ProfileSettingsRow(
                            icon = Icons.Outlined.UploadFile,
                            title = "Export Data"
                        )
                    }
                }

                // Bottom spacing for toolbar
                Spacer(modifier = Modifier.height(100.dp))
            }
        }

        // Bottom toolbar
        val iconTint = MaterialTheme.colorScheme.primary
        val iconSize = Modifier.size(24.dp)

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(28.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f))
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(28.dp)
                    )
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp)
                ) {
                    IconButton(onClick = {
                        HapticHelper.performClick(view, settingsState.hapticsEnabled)
                        onLockClick()
                    }, modifier = Modifier.size(48.dp)) {
                        Icon(
                            imageVector = Icons.Outlined.Lock,
                            contentDescription = "Lock vault",
                            tint = iconTint,
                            modifier = iconSize
                        )
                    }
                    IconButton(onClick = {
                        HapticHelper.performClick(view, settingsState.hapticsEnabled)
                        onDownloadClick()
                    }, modifier = Modifier.size(48.dp)) {
                        Icon(
                            imageVector = Icons.Outlined.Download,
                            contentDescription = "Download vault",
                            tint = iconTint,
                            modifier = iconSize
                        )
                    }
                    IconButton(onClick = { }, modifier = Modifier.size(48.dp)) {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = "Profile",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = iconSize
                        )
                    }
                    IconButton(onClick = {
                        HapticHelper.performClick(view, settingsState.hapticsEnabled)
                        onSettingsClick()
                    }, modifier = Modifier.size(48.dp)) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "Settings",
                            tint = iconTint,
                            modifier = iconSize
                        )
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                ),
                onClick = {
                    HapticHelper.performClick(view, settingsState.hapticsEnabled)
                    onBackClick()
                }
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Home,
                        contentDescription = "Back to vault",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = iconSize
                    )
                }
            }
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
    title: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
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
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun ProfileDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    )
}
