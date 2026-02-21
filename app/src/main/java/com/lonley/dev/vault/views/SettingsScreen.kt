package com.lonley.dev.vault.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Coffee
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SwipeDown
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.drop
import com.lonley.dev.vault.model.AccentColor
import com.lonley.dev.vault.model.FontScale
import com.lonley.dev.vault.model.SettingsState
import com.lonley.dev.vault.ui.theme.AccentBlue
import com.lonley.dev.vault.ui.theme.AccentGray
import com.lonley.dev.vault.ui.theme.AccentGreen
import com.lonley.dev.vault.ui.theme.AccentOrange
import com.lonley.dev.vault.ui.theme.AccentPurple
import com.lonley.dev.vault.ui.theme.AccentTeal
import com.lonley.dev.vault.ui.theme.AccentYellow
import com.lonley.dev.vault.ui.theme.AccentRed
import com.lonley.dev.vault.ui.theme.AccentPink
import com.lonley.dev.vault.ui.theme.AccentIndigo
import com.lonley.dev.vault.ui.theme.AccentBrown
import com.lonley.dev.vault.ui.theme.ThemeMode
import com.lonley.dev.vault.util.HapticHelper

@Composable
fun SettingsScreen(
    settingsState: SettingsState,
    onThemeModeChange: (ThemeMode) -> Unit,
    onAccentColorChange: (AccentColor) -> Unit,
    onHapticsToggle: (Boolean) -> Unit,
    onFontScaleChange: (FontScale) -> Unit = {},
    onScrollVibrationToggle: (String, Boolean) -> Unit,
    onBackClick: () -> Unit,
    onLockClick: () -> Unit,
    onDownloadClick: () -> Unit,
    onProfileClick: () -> Unit = {}
) {
    val view = LocalView.current

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            // ── Sticky header ──
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Settings",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(24.dp))

            // ── Scrollable content ──
            val scrollState = rememberScrollState()

            LaunchedEffect(settingsState.hapticsEnabled, settingsState.scrollVibrations) {
                if (settingsState.hapticsEnabled && settingsState.scrollVibrations["settings"] == true) {
                    snapshotFlow { scrollState.value / 50 }
                        .drop(1)
                        .collect { HapticHelper.performScrollTick(view, true) }
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
            ) {
                // ── Appearance ──
                SectionHeader("Appearance")
                Spacer(modifier = Modifier.height(8.dp))

                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Theme Mode (expandable, like scroll vibrations)
                        var themeExpanded by remember { mutableStateOf(false) }
                        val themeChevronRotation by animateFloatAsState(
                            targetValue = if (themeExpanded) 180f else 0f,
                            label = "themeChevron"
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    HapticHelper.performClick(view, settingsState.hapticsEnabled)
                                    themeExpanded = !themeExpanded
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.DarkMode,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Theme Mode",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = settingsState.themeMode.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                imageVector = Icons.Outlined.ExpandMore,
                                contentDescription = if (themeExpanded) "Collapse" else "Expand",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.rotate(themeChevronRotation)
                            )
                        }

                        AnimatedVisibility(
                            visible = themeExpanded,
                            enter = expandVertically(),
                            exit = shrinkVertically()
                        ) {
                            Column(
                                modifier = Modifier.padding(start = 56.dp, end = 16.dp, bottom = 8.dp)
                            ) {
                                ThemeMode.entries.forEach { mode ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                HapticHelper.performClick(view, settingsState.hapticsEnabled)
                                                onThemeModeChange(mode)
                                            }
                                    ) {
                                        RadioButton(
                                            selected = settingsState.themeMode == mode,
                                            onClick = {
                                                HapticHelper.performClick(view, settingsState.hapticsEnabled)
                                                onThemeModeChange(mode)
                                            }
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = mode.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }

                        SettingsDivider()

                        // Accent Color
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Palette,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Accent Color",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                AccentColorPicker(
                                    selected = settingsState.accentColor,
                                    onSelect = {
                                        HapticHelper.performClick(view, settingsState.hapticsEnabled)
                                        onAccentColorChange(it)
                                    }
                                )
                            }
                        }

                        SettingsDivider()

                        // Language
                        SettingsRow(
                            icon = Icons.Outlined.Language,
                            title = "Language",
                            subtitle = settingsState.language
                        )

                        SettingsDivider()

                        // Font Size (expandable)
                        var fontSizeExpanded by remember { mutableStateOf(false) }
                        val fontSizeChevronRotation by animateFloatAsState(
                            targetValue = if (fontSizeExpanded) 180f else 0f,
                            label = "fontSizeChevron"
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    HapticHelper.performClick(view, settingsState.hapticsEnabled)
                                    fontSizeExpanded = !fontSizeExpanded
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Settings,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Font Size",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = settingsState.fontScale.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                imageVector = Icons.Outlined.ExpandMore,
                                contentDescription = if (fontSizeExpanded) "Collapse" else "Expand",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.rotate(fontSizeChevronRotation)
                            )
                        }

                        AnimatedVisibility(
                            visible = fontSizeExpanded,
                            enter = expandVertically(),
                            exit = shrinkVertically()
                        ) {
                            Column(
                                modifier = Modifier.padding(start = 56.dp, end = 16.dp, bottom = 8.dp)
                            ) {
                                FontScale.entries.forEach { scale ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                HapticHelper.performClick(view, settingsState.hapticsEnabled)
                                                onFontScaleChange(scale)
                                            }
                                    ) {
                                        RadioButton(
                                            selected = settingsState.fontScale == scale,
                                            onClick = {
                                                HapticHelper.performClick(view, settingsState.hapticsEnabled)
                                                onFontScaleChange(scale)
                                            }
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = scale.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ── Experience ──
                SectionHeader("Experience")
                Spacer(modifier = Modifier.height(8.dp))

                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Haptics
                        SettingsRow(
                            icon = Icons.Outlined.Vibration,
                            title = "Haptics",
                            subtitle = "System-wide vibrations",
                            trailing = {
                                Switch(
                                    checked = settingsState.hapticsEnabled,
                                    onCheckedChange = {
                                        onHapticsToggle(it)
                                        HapticHelper.performClick(view, it)
                                    }
                                )
                            }
                        )

                        SettingsDivider()

                        // Scroll Vibrations (expandable, disabled when haptics off)
                        val hapticsOn = settingsState.hapticsEnabled
                        var scrollExpanded by remember { mutableStateOf(false) }
                        val chevronRotation by animateFloatAsState(
                            targetValue = if (scrollExpanded && hapticsOn) 180f else 0f,
                            label = "chevron"
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .alpha(if (hapticsOn) 1f else 0.4f)
                                .then(
                                    if (hapticsOn) Modifier.clickable {
                                        HapticHelper.performClick(view, true)
                                        scrollExpanded = !scrollExpanded
                                    } else Modifier
                                )
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.SwipeDown,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Scroll Vibrations",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Per-screen scroll feedback",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                imageVector = Icons.Outlined.ExpandMore,
                                contentDescription = if (scrollExpanded) "Collapse" else "Expand",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.rotate(chevronRotation)
                            )
                        }

                        AnimatedVisibility(
                            visible = scrollExpanded && hapticsOn,
                            enter = expandVertically(),
                            exit = shrinkVertically()
                        ) {
                            Column(
                                modifier = Modifier.padding(start = 56.dp, end = 16.dp, bottom = 8.dp)
                            ) {
                                ScrollVibrationCheckbox("Home", "home", settingsState, onScrollVibrationToggle)
                                ScrollVibrationCheckbox("Vault", "vault", settingsState, onScrollVibrationToggle)
                                ScrollVibrationCheckbox("Settings", "settings", settingsState, onScrollVibrationToggle)
                                ScrollVibrationCheckbox("Password Detail", "passwordDetail", settingsState, onScrollVibrationToggle)
                                ScrollVibrationCheckbox("Edit Entry", "editEntry", settingsState, onScrollVibrationToggle)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ── About ──
                SectionHeader("About")
                Spacer(modifier = Modifier.height(8.dp))

                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        SettingsRow(
                            icon = Icons.Outlined.Info,
                            title = "About App",
                            subtitle = "What we do"
                        )
                        SettingsDivider()
                        SettingsRow(
                            icon = Icons.Outlined.PrivacyTip,
                            title = "Privacy Policy",
                            trailing = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                                    contentDescription = "Open link",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        )
                        SettingsDivider()
                        SettingsRow(
                            icon = Icons.Outlined.Person,
                            title = "Author",
                            trailing = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                                    contentDescription = "Open link",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        )
                        SettingsDivider()
                        SettingsRow(
                            icon = Icons.Outlined.AutoAwesome,
                            title = "More From Me",
                            trailing = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                                    contentDescription = "Open link",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        )
                        SettingsDivider()
                        SettingsRow(
                            icon = Icons.Outlined.Coffee,
                            title = "Buy Me a Coffee",
                            trailing = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                                    contentDescription = "Open link",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
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
                    IconButton(onClick = onLockClick, modifier = Modifier.size(48.dp)) {
                        Icon(
                            imageVector = Icons.Outlined.Lock,
                            contentDescription = "Lock vault",
                            tint = iconTint,
                            modifier = iconSize
                        )
                    }
                    IconButton(onClick = onDownloadClick, modifier = Modifier.size(48.dp)) {
                        Icon(
                            imageVector = Icons.Outlined.Download,
                            contentDescription = "Download vault",
                            tint = iconTint,
                            modifier = iconSize
                        )
                    }
                    IconButton(onClick = {
                        HapticHelper.performClick(view, settingsState.hapticsEnabled)
                        onProfileClick()
                    }, modifier = Modifier.size(48.dp)) {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = "Profile",
                            tint = iconTint,
                            modifier = iconSize
                        )
                    }
                    IconButton(onClick = onBackClick, modifier = Modifier.size(48.dp)) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "Back to vault",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = iconSize
                        )
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                ),
                onClick = onBackClick
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
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    trailing: @Composable (() -> Unit)? = null
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
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (trailing != null) {
            Spacer(modifier = Modifier.width(8.dp))
            trailing()
        }
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    )
}


@Composable
private fun AccentColorPicker(
    selected: AccentColor,
    onSelect: (AccentColor) -> Unit
) {
    val colorMap = mapOf(
        AccentColor.Auto to null,
        AccentColor.Blue to AccentBlue,
        AccentColor.Teal to AccentTeal,
        AccentColor.Green to AccentGreen,
        AccentColor.Yellow to AccentYellow,
        AccentColor.Orange to AccentOrange,
        AccentColor.Purple to AccentPurple,
        AccentColor.Gray to AccentGray,
        AccentColor.Red to AccentRed,
        AccentColor.Pink to AccentPink,
        AccentColor.Indigo to AccentIndigo,
        AccentColor.Brown to AccentBrown
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.horizontalScroll(rememberScrollState())
    ) {
        colorMap.forEach { (accent, color) ->
            val isSelected = accent == selected
            val borderModifier = if (isSelected) {
                Modifier.border(2.5.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
            } else {
                Modifier
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .then(borderModifier)
                    .padding(3.dp)
                    .clip(CircleShape)
                    .background(
                        color ?: MaterialTheme.colorScheme.surfaceVariant,
                        CircleShape
                    )
                    .clickable { onSelect(accent) }
            ) {
                if (accent == AccentColor.Auto) {
                    Icon(
                        imageVector = Icons.Outlined.AutoAwesome,
                        contentDescription = "Auto",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                } else if (isSelected) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ScrollVibrationCheckbox(
    label: String,
    key: String,
    settingsState: SettingsState,
    onToggle: (String, Boolean) -> Unit
) {
    val view = LocalView.current
    val checked = settingsState.scrollVibrations[key] ?: false
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                HapticHelper.performClick(view, settingsState.hapticsEnabled)
                onToggle(key, !checked)
            }
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = {
                HapticHelper.performClick(view, settingsState.hapticsEnabled)
                onToggle(key, it)
            }
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String, icon: ImageVector? = null) {
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
