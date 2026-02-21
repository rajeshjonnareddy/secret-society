package com.lonley.dev.vault.views

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Downloading
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.VideocamOff
import androidx.compose.material.icons.outlined.FrontHand
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.lonley.dev.vault.ui.theme.LocalGlassColors
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.drop
import com.lonley.dev.vault.model.PasswordEntry
import com.lonley.dev.vault.model.SettingsState
import com.lonley.dev.vault.ui.theme.VaultTheme
import com.lonley.dev.vault.util.HapticHelper

// ── Reusable glass card ──

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content: @Composable BoxScope.() -> Unit
) {
    val glass = LocalGlassColors.current

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(glass.background)
            .border(1.dp, glass.border, RoundedCornerShape(16.dp))
            .padding(contentPadding),
        content = content
    )
}

// ── Glass search bar ──

@Composable
private fun SearchGlassBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(16.dp)
    val glass = LocalGlassColors.current

    Box(
        modifier = modifier
            .height(56.dp)
            .clip(shape)
            .background(glass.background)
            .border(1.dp, glass.border, shape)
            .padding(horizontal = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.width(8.dp))

            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = {
                    Text(
                        text = "Search passwords...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(0.dp),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onBackground
                ),
                singleLine = true
            )
        }
    }
}

@Composable
fun SearchInputGlassCard(
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    val roundedShape = RoundedCornerShape(16.dp)
    val glass = LocalGlassColors.current

    Box(
        modifier = modifier
            .height(50.dp)
            .clip(roundedShape)
            .background(glass.background)
            .border(1.dp, glass.border, roundedShape)
            .padding(horizontal = 16.dp, vertical = 0.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search Icon",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.width(16.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { newValue ->
                    searchQuery = newValue
                },
                placeholder = {
                    Text(
                        text = "Search for a disease...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(0.dp),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onBackground
                ),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search,
                    keyboardType = KeyboardType.Text
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {

                    }
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {

                },
                enabled = searchQuery.isNotBlank()
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Submit Search",
                    tint = if (searchQuery.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
            }
        }
    }
}

// ── Password entry card using glass style ──

@Composable
private fun PasswordEntryItem(
    entry: PasswordEntry,
    onClick: () -> Unit = {},
    onCopyClick: () -> Unit = {},
    onEditClick: () -> Unit = {}
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val accentGradient = Brush.verticalGradient(listOf(primaryColor, tertiaryColor))
    val bandGradient = Brush.horizontalGradient(
        listOf(primaryColor.copy(alpha = 0.15f), tertiaryColor.copy(alpha = 0.05f), Color.Transparent)
    )

    val relativeTime = remember(entry.createdAt) {
        val diff = System.currentTimeMillis() - entry.createdAt
        val minutes = diff / 60_000
        val hours = minutes / 60
        val days = hours / 24
        when {
            minutes < 1 -> "now"
            minutes < 60 -> "${minutes}m ago"
            hours < 24 -> "${hours}h ago"
            days < 30 -> "${days}d ago"
            days < 365 -> "${days / 30}mo ago"
            else -> "${days / 365}y ago"
        }
    }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            // Left accent gradient strip
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(accentGradient)
            )

            // Main content with gradient band background
            Box(modifier = Modifier.weight(1f)) {
                // Gradient accent band spanning left portion
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .fillMaxHeight()
                        .background(bandGradient)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    // Initial letter overlaid on gradient band
                    Box(
                        modifier = Modifier
                            .size(44.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = entry.name.firstOrNull()?.uppercase() ?: "?",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Text content
                    Column(modifier = Modifier.weight(1f)) {
                        // Primary zone: name + username
                        Text(
                            text = entry.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "@${entry.username}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        // Subtle divider
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(0.5.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Secondary info row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (!entry.website.isNullOrBlank()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Language,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = entry.website,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Text(
                                text = "\u2022".repeat(8),
                                style = MaterialTheme.typography.bodySmall.copy(letterSpacing = 2.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            Text(
                                text = relativeTime,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Vertically stacked action buttons
                    Column(
                        verticalArrangement = Arrangement.spacedBy(0.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        IconButton(
                            onClick = onCopyClick,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy password",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(
                            onClick = onEditClick,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit entry",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Dashboard stat cards ──

@Composable
private fun DashboardStatsRow(
    totalPasswords: Int,
    lastUpdatedAt: Long
) {
    val relativeTime = remember(lastUpdatedAt) {
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

    data class StatCard(val icon: androidx.compose.ui.graphics.vector.ImageVector, val value: String, val label: String)

    val stats = listOf(
        StatCard(Icons.Outlined.Lock, totalPasswords.toString(), "Passwords"),
        StatCard(Icons.Outlined.Download, relativeTime, "Last Updated"),
        StatCard(Icons.Outlined.Settings, "4", "Categories"),
        StatCard(Icons.Outlined.Person, "0", "Favorites")
    )

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(stats.size) { index ->
            val stat = stats[index]
            GlassCard(
                modifier = Modifier.width(150.dp),
                contentPadding = PaddingValues(14.dp)
            ) {
                Column {
                    Icon(
                        imageVector = stat.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stat.value,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stat.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ── Main screen ──

@Composable
fun VaultScreen(
    vaultName: String,
    passwordEntries: List<PasswordEntry>,
    settingsState: SettingsState? = null,
    isLoading: Boolean = false,
    lastUpdatedAt: Long = 0L,
    onAddPasswordClick: () -> Unit,
    onBackClick: () -> Unit = {},
    onDownloadClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onEntryClick: (PasswordEntry) -> Unit = {},
    onCopyPassword: (PasswordEntry) -> Unit = {},
    onEditEntry: (PasswordEntry) -> Unit = {}
) {
    val view = LocalView.current
    val listState = rememberLazyListState()
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(settingsState?.hapticsEnabled, settingsState?.scrollVibrations) {
        if (settingsState != null && settingsState.hapticsEnabled && settingsState.scrollVibrations["vault"] == true) {
            snapshotFlow {
                listState.firstVisibleItemIndex to (listState.firstVisibleItemScrollOffset / 50)
            }.drop(1).collect { HapticHelper.performScrollTick(view, true) }
        }
    }

    val filterOptions = listOf("All", "Websites", "Apps", "Social", "Other")
    var selectedFilter by remember { mutableStateOf(filterOptions.first()) }

    val filteredEntries = passwordEntries.filter { entry ->
        val matchesSearch = searchQuery.isBlank() ||
                entry.name.contains(searchQuery, ignoreCase = true) ||
                entry.username.contains(searchQuery, ignoreCase = true)
        val matchesFilter = selectedFilter == "All" || when (selectedFilter) {
            "Websites" -> !entry.website.isNullOrBlank()
            else -> true
        }
        matchesSearch && matchesFilter
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            // Brand-style vault name
            Text(
                text = vaultName.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Your passwords, secured.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            SearchGlassBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(0.dp))

            // ── Filter chips (horizontally scrollable) ──
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(filterOptions) { _, filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = {
                            HapticHelper.performClick(view, settingsState?.hapticsEnabled == true)
                            selectedFilter = filter
                        },
                        label = {
                            Text(
                                text = filter,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (selectedFilter == filter) FontWeight.SemiBold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedFilter == filter,
                            borderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── Dashboard stats ──
            DashboardStatsRow(
                totalPasswords = passwordEntries.size,
                lastUpdatedAt = lastUpdatedAt
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── Content area ──
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (passwordEntries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No passwords yet",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap + to add your first one",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else if (filteredEntries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No matches found",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = filteredEntries,
                        key = { it.id }
                    ) { entry ->
                        PasswordEntryItem(
                            entry = entry,
                            onClick = {
                                HapticHelper.performClick(view, settingsState?.hapticsEnabled == true)
                                onEntryClick(entry)
                            },
                            onCopyClick = {
                                HapticHelper.performClick(view, settingsState?.hapticsEnabled == true)
                                onCopyPassword(entry)
                            },
                            onEditClick = {
                                HapticHelper.performClick(view, settingsState?.hapticsEnabled == true)
                                onEditEntry(entry)
                            }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }

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
                        HapticHelper.performClick(view, settingsState?.hapticsEnabled == true)
                        onBackClick()
                    }, modifier = Modifier.size(48.dp)) {
                        Icon(
                            imageVector = Icons.Outlined.Lock,
                            contentDescription = "Lock vault",
                            tint = iconTint,
                            modifier = iconSize
                        )
                    }
                    IconButton(onClick = {
                        HapticHelper.performClick(view, settingsState?.hapticsEnabled == true)
                        onDownloadClick()
                    }, modifier = Modifier.size(48.dp)) {
                        Icon(
                            imageVector = Icons.Outlined.Download,
                            contentDescription = "Download vault",
                            tint = iconTint,
                            modifier = iconSize
                        )
                    }
                    IconButton(onClick = {
                        HapticHelper.performClick(view, settingsState?.hapticsEnabled == true)
                        onProfileClick()
                    }, modifier = Modifier.size(48.dp)) {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = "Profile",
                            tint = iconTint,
                            modifier = iconSize
                        )
                    }
                    IconButton(onClick = {
                        HapticHelper.performClick(view, settingsState?.hapticsEnabled == true)
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
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                ),
                onClick = {
                    HapticHelper.performClick(view, settingsState?.hapticsEnabled == true)
                    onAddPasswordClick()
                }
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add New Password",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = iconSize
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_NO)
@Composable
private fun VaultScreenEmptyPreview() {
    VaultTheme {
        VaultScreen(
            vaultName = "My Vault",
            passwordEntries = emptyList(),
            onAddPasswordClick = {}
        )
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun VaultScreenPopulatedPreview() {
    VaultTheme {
        VaultScreen(
            vaultName = "My Vault",
            passwordEntries = listOf(
                PasswordEntry("1", "Google Account", "me@gmail.com", "secret", "google.com"),
                PasswordEntry("2", "My Bank", "user123", "pass", "mybank.com"),
                PasswordEntry("3", "Social Media", "social_user", "pwd123"),
            ),
            onAddPasswordClick = {}
        )
    }
}
