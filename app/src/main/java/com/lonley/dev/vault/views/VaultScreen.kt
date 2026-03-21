package com.lonley.dev.vault.views

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.Subscriptions
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.LocalActivity
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Password
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.lonley.dev.vault.ui.theme.LocalGlassColors
import com.lonley.dev.vault.ui.theme.LocalVaultColors
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import com.lonley.dev.vault.model.EntryType
import com.lonley.dev.vault.model.formatPrice
import com.lonley.dev.vault.model.PasswordEntry
import com.lonley.dev.vault.model.SettingsState
import com.lonley.dev.vault.model.SwipeAction
import com.lonley.dev.vault.ui.theme.VaultTheme
import com.lonley.dev.vault.util.HapticHelper
import com.lonley.dev.vault.util.formatRelativeTime

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
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape)
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

// ── Password entry card using glass style ──

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PasswordEntryItem(
    entry: PasswordEntry,
    onClick: () -> Unit = {},
    onLongPress: () -> Unit = {},
    onCopyClick: () -> Unit = {},
    onEditClick: () -> Unit = {},
    onFavoriteClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {}
) {
    val relativeTime = remember(entry.createdAt) { formatRelativeTime(entry.createdAt, justNowText = "now") }

    val primaryColor = MaterialTheme.colorScheme.primary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary

    val cardGradient = Brush.linearGradient(
        colors = listOf(
            primaryColor.copy(alpha = 0.12f),
            tertiaryColor.copy(alpha = 0.08f),
            Color.Transparent
        ),
        start = Offset(0f, Float.POSITIVE_INFINITY),
        end = Offset(Float.POSITIVE_INFINITY, 0f)
    )
    val shineGradient = Brush.radialGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.10f),
            Color.Transparent
        ),
        center = Offset(Float.POSITIVE_INFINITY, 0f),
        radius = 200f
    )

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongPress
            )
    ) {
                // Credit card background layers
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(cardGradient)
                        .background(shineGradient)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    // Text content
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        // Primary zone: name + username with subscription info
                        Column {
                            Text(
                                text = entry.name.replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = LocalVaultColors.current.cardHeading,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (entry.entryType == EntryType.CryptoWallet) {
                                Text(
                                    text = entry.network?.label ?: "Digital Wallet",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val displayIdentifier = entry.username?.takeIf { it.isNotBlank() }
                                    ?: entry.email?.takeIf { it.isNotBlank() }
                                Text(
                                    text = if (displayIdentifier != null) {
                                        if (entry.username?.isNotBlank() == true) "@$displayIdentifier" else displayIdentifier
                                    } else "",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.secondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                if (entry.isSubscription && (!entry.price.isNullOrBlank() || entry.planType != null)) {
                                    Column(
                                        horizontalAlignment = Alignment.End
                                    ) {
                                        if (!entry.price.isNullOrBlank()) {
                                            Text(
                                                text = "$${formatPrice(entry.price)}",
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.tertiary
                                            )
                                        }
                                        if (entry.planType != null) {
                                            Text(
                                                text = entry.planType.label,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.7f)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // Subtle divider + Secondary info row (pinned to bottom)
                        Column {
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
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.widthIn(max = 120.dp)
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
                                        modifier = Modifier.basicMarquee(
                                            iterations = Int.MAX_VALUE,
                                            initialDelayMillis = 1000,
                                            repeatDelayMillis = 2000
                                        )
                                    )
                                }
                            }

                            Text(
                                text = "\u2022".repeat(8),
                                style = MaterialTheme.typography.bodySmall.copy(letterSpacing = 2.sp),
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.30f)
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            Text(
                                text = relativeTime,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                        } // end bottom-pinned Column
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Vertically stacked action buttons
                    Column(
                        verticalArrangement = Arrangement.spacedBy(0.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        IconButton(
                            onClick = onFavoriteClick,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (entry.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                contentDescription = if (entry.isFavorite) "Remove from favorites" else "Add to favorites",
                                tint = if (entry.isFavorite) Color(0xFFFFC107) else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
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
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
    }
}

// ── Dashboard stat cards ──

@Composable
private fun DashboardStatsRow(
    passwordEntries: List<PasswordEntry>,
    totalPasswords: Int,
    lastUpdatedAt: Long
) {
    val relativeTime = remember(lastUpdatedAt) { formatRelativeTime(lastUpdatedAt) }

    data class StatCard(
        val icon: androidx.compose.ui.graphics.vector.ImageVector,
        val value: String,
        val label: String,
        val isCount: Boolean = false
    )

    val stats = listOf(
        StatCard(Icons.Outlined.Password, totalPasswords.toString(), "Passwords", isCount = true),
        StatCard(Icons.Outlined.Subscriptions, passwordEntries.count { it.isSubscription }.toString(), "Subscriptions", isCount = true),
        StatCard(Icons.Outlined.Favorite, passwordEntries.count { it.isFavorite }.toString(), "Favorites", isCount = true),
        StatCard(Icons.Outlined.LocalActivity, relativeTime, "Last Updated")
    )

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(stats.size) { index ->
            val stat = stats[index]
            GlassCard(
                modifier = Modifier
                    .width(if (stat.isCount) 120.dp else 100.dp)
                    .height(90.dp),
                contentPadding = PaddingValues(14.dp)
            ) {
                if (stat.isCount) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            modifier = Modifier.fillMaxHeight(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Icon(
                                imageVector = stat.icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = stat.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .widthIn(min = 36.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stat.value,
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Icon(
                            imageVector = stat.icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = stat.value,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            modifier = Modifier
                                .fillMaxWidth()
                                .basicMarquee(
                                    iterations = Int.MAX_VALUE,
                                    initialDelayMillis = 1000,
                                    repeatDelayMillis = 2000
                                )
                        )
                        Text(
                            text = stat.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            modifier = Modifier
                                .fillMaxWidth()
                                .basicMarquee(
                                    iterations = Int.MAX_VALUE,
                                    initialDelayMillis = 500,
                                    repeatDelayMillis = 2500
                                )
                        )
                    }
                }
            }
        }
    }
}

// ── Swipe action helpers ──

private data class SwipeActionVisuals(
    val color: Color,
    val icon: ImageVector,
    val description: String
)

private fun swipeActionVisuals(action: SwipeAction): SwipeActionVisuals = when (action) {
    SwipeAction.Delete -> SwipeActionVisuals(Color(0xFFE53935), Icons.Default.Delete, "Delete")
    SwipeAction.Edit -> SwipeActionVisuals(Color(0xFF1E88E5), Icons.Default.Edit, "Edit")
    SwipeAction.CopyPassword -> SwipeActionVisuals(Color(0xFF43A047), Icons.Default.ContentCopy, "Copy Password")
    SwipeAction.ToggleFavorite -> SwipeActionVisuals(Color(0xFFFFC107), Icons.Default.Star, "Toggle Favorite")
}

@Composable
private fun SwipeBackground(
    action: SwipeAction,
    direction: SwipeToDismissBoxValue,
    progress: Float
) {
    val visuals = swipeActionVisuals(action)
    val parentAlignment = when (direction) {
        SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
        SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
        else -> return
    }

    // Clamp progress so the background only covers the revealed portion
    val fraction = progress.coerceIn(0f, 1f)
    if (fraction == 0f) return

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = parentAlignment
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction)
                .fillMaxHeight()
                .clip(RoundedCornerShape(16.dp))
                .background(visuals.color)
                .padding(horizontal = 24.dp),
            contentAlignment = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                else -> Alignment.Center
            }
        ) {
            Icon(
                imageVector = visuals.icon,
                contentDescription = visuals.description,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeablePasswordCard(
    entry: PasswordEntry,
    swipeLeftAction: SwipeAction,
    swipeRightAction: SwipeAction,
    onSwipeAction: (SwipeAction) -> Unit,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    onCopyClick: () -> Unit,
    onEditClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            when (dismissValue) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    onSwipeAction(swipeRightAction)
                    false // snap back
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    onSwipeAction(swipeLeftAction)
                    false // snap back
                }
                else -> false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val rawProgress = dismissState.progress
            val current = dismissState.currentValue
            val target = dismissState.targetValue
            val isSettled = current == SwipeToDismissBoxValue.Settled &&
                    target == SwipeToDismissBoxValue.Settled
            val isSnappingBack = current != SwipeToDismissBoxValue.Settled &&
                    target == SwipeToDismissBoxValue.Settled

            val fraction = when {
                isSettled -> 0f
                isSnappingBack -> (1f - rawProgress).coerceIn(0f, 1f)
                else -> rawProgress.coerceIn(0f, 1f)
            }

            when (dismissState.dismissDirection) {
                SwipeToDismissBoxValue.StartToEnd -> SwipeBackground(swipeRightAction, SwipeToDismissBoxValue.StartToEnd, fraction)
                SwipeToDismissBoxValue.EndToStart -> SwipeBackground(swipeLeftAction, SwipeToDismissBoxValue.EndToStart, fraction)
                else -> Box(Modifier.fillMaxSize())
            }
        }
    ) {
        PasswordEntryItem(
            entry = entry,
            onClick = onClick,
            onLongPress = onLongPress,
            onCopyClick = onCopyClick,
            onEditClick = onEditClick,
            onFavoriteClick = onFavoriteClick,
            onDeleteClick = onDeleteClick
        )
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
    onEntryClick: (PasswordEntry) -> Unit = {},
    onCopyPassword: (PasswordEntry) -> Unit = {},
    onEditEntry: (PasswordEntry) -> Unit = {},
    onToggleFavorite: (PasswordEntry) -> Unit = {},
    onDeleteEntry: (PasswordEntry) -> Unit = {}
) {
    val view = LocalView.current
    val listState = rememberLazyListState()
    val isCollapsed by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 50
        }
    }
    var searchQuery by remember { mutableStateOf("") }
    var searchExpanded by remember { mutableStateOf(false) }
    val showSearchBar = !isCollapsed || searchExpanded
    val scope = rememberCoroutineScope()

    // Reset search override when user scrolls back to top, or scrolls further while expanded
    LaunchedEffect(Unit) {
        snapshotFlow { listState.firstVisibleItemScrollOffset }
            .drop(1)
            .collect { searchExpanded = false }
    }

    LaunchedEffect(settingsState?.hapticsEnabled, settingsState?.scrollVibrations) {
        if (settingsState != null && settingsState.hapticsEnabled && settingsState.scrollVibrations["vault"] == true) {
            snapshotFlow {
                listState.firstVisibleItemIndex to (listState.firstVisibleItemScrollOffset / 50)
            }.drop(1).collect { HapticHelper.performScrollTick(view, true) }
        }
    }

    var selectedFilter by remember { mutableStateOf("All") }
    val relativeTime = remember(lastUpdatedAt) { formatRelativeTime(lastUpdatedAt) }

    data class FilterPill(
        val label: String,
        val icon: androidx.compose.ui.graphics.vector.ImageVector,
        val count: Int? = null,
        val displayValue: String? = null,
        val isFilter: Boolean = true
    )

    val filterPills = listOf(
        FilterPill("All", Icons.Outlined.Password, count = passwordEntries.size),
        FilterPill("Favorites", Icons.Outlined.Favorite, count = passwordEntries.count { it.isFavorite }),
        FilterPill("Subscriptions", Icons.Outlined.Subscriptions, count = passwordEntries.count { it.isSubscription }),
        FilterPill("Wallets", Icons.Outlined.AccountBalanceWallet, count = passwordEntries.count { it.entryType == EntryType.CryptoWallet }),
        FilterPill("Websites", Icons.Outlined.Language, count = passwordEntries.count { !it.website.isNullOrBlank() }),
        FilterPill("Apps", Icons.Outlined.Apps),
        FilterPill("Social", Icons.Outlined.People),
        FilterPill("Other", Icons.Outlined.MoreHoriz),
        FilterPill("Last Updated", Icons.Outlined.LocalActivity, displayValue = relativeTime, isFilter = false)
    )

    val filteredEntries = passwordEntries.filter { entry ->
        val matchesSearch = searchQuery.isBlank() ||
                entry.name.contains(searchQuery, ignoreCase = true) ||
                (entry.username?.contains(searchQuery, ignoreCase = true) == true) ||
                (entry.email?.contains(searchQuery, ignoreCase = true) == true) ||
                (entry.walletAddress?.contains(searchQuery, ignoreCase = true) == true) ||
                (entry.exchange?.contains(searchQuery, ignoreCase = true) == true)
        val matchesFilter = selectedFilter == "All" || when (selectedFilter) {
            "Favorites" -> entry.isFavorite
            "Subscriptions" -> entry.isSubscription
            "Wallets" -> entry.entryType == EntryType.CryptoWallet
            "Websites" -> !entry.website.isNullOrBlank()
            else -> true
        }
        matchesSearch && matchesFilter
    }.sortedBy { it.name.lowercase() }

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
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                modifier = Modifier.basicMarquee(
                    iterations = Int.MAX_VALUE,
                    initialDelayMillis = 1000,
                    repeatDelayMillis = 2000
                )
            )

            Text(
                text = "Your passwords, secured.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            val topSpacing by animateDpAsState(
                targetValue = if (isCollapsed && !searchExpanded) 8.dp else 24.dp,
                label = "top-spacing"
            )
            val postSearchSpacing by animateDpAsState(
                targetValue = if (isCollapsed && !searchExpanded) 4.dp else 12.dp,
                label = "post-search-spacing"
            )

            Spacer(modifier = Modifier.height(topSpacing))

            AnimatedContent(
                targetState = showSearchBar,
                transitionSpec = {
                    (fadeIn() + expandVertically()) togetherWith (fadeOut() + shrinkVertically())
                },
                label = "search-collapse"
            ) { visible ->
                if (visible) {
                    SearchGlassBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(postSearchSpacing))

            // ── Filter pills (credit card style, replaces both chips + stats row) ──
            val primaryColor = MaterialTheme.colorScheme.primary
            val tertiaryColor = MaterialTheme.colorScheme.tertiary

            // Diagonal gradient wash (bottom-left → top-right)
            val cardGradient = Brush.linearGradient(
                colors = listOf(
                    primaryColor.copy(alpha = 0.12f),
                    tertiaryColor.copy(alpha = 0.08f),
                    Color.Transparent
                ),
                start = Offset(0f, Float.POSITIVE_INFINITY),
                end = Offset(Float.POSITIVE_INFINITY, 0f)
            )
            // Radial shine in top-right corner
            val shineGradient = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.10f),
                    Color.Transparent
                ),
                center = Offset(Float.POSITIVE_INFINITY, 0f),
                radius = 200f
            )

            AnimatedContent(
                targetState = isCollapsed,
                transitionSpec = {
                    (fadeIn() + expandVertically()) togetherWith (fadeOut() + shrinkVertically())
                },
                label = "dashboard-collapse"
            ) { collapsed ->
                if (collapsed) {
                    // ── Compact filter chips with search pill first ──
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (!searchExpanded) item {
                            FilterChip(
                                selected = searchQuery.isNotBlank(),
                                onClick = {
                                    HapticHelper.performClick(view, settingsState?.hapticsEnabled == true)
                                    searchExpanded = true
                                },
                                label = {
                                    Text(
                                        text = searchQuery.ifBlank { "Search..." },
                                        style = MaterialTheme.typography.labelMedium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.widthIn(max = 120.dp)
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Search",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            )
                        }
                        items(filterPills.size) { index ->
                            val pill = filterPills[index]
                            val isSelected = pill.isFilter && selectedFilter == pill.label
                            val chipLabel = when {
                                pill.count != null -> "${pill.label} (${pill.count})"
                                pill.displayValue != null -> "${pill.label}: ${pill.displayValue}"
                                else -> pill.label
                            }

                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    if (pill.isFilter) {
                                        HapticHelper.performClick(view, settingsState?.hapticsEnabled == true)
                                        selectedFilter = pill.label
                                    }
                                },
                                label = {
                                    Text(
                                        text = chipLabel,
                                        style = MaterialTheme.typography.labelMedium,
                                        maxLines = 1
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = pill.icon,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                border = if (isSelected) FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = true,
                                    borderColor = primaryColor,
                                    borderWidth = 1.5.dp
                                ) else FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = false
                                )
                            )
                        }
                    }
                } else {
                    // ── Expanded glass cards ──
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filterPills.size) { index ->
                            val pill = filterPills[index]
                            val isSelected = pill.isFilter && selectedFilter == pill.label

                            GlassCard(
                                modifier = Modifier
                                    .width(if (pill.count != null) 120.dp else 100.dp)
                                    .height(90.dp)
                                    .then(
                                        if (isSelected) Modifier.border(
                                            1.5.dp,
                                            Brush.linearGradient(listOf(primaryColor, tertiaryColor)),
                                            RoundedCornerShape(16.dp)
                                        ) else Modifier
                                    )
                                    .then(
                                        if (pill.isFilter) Modifier.clickable {
                                            HapticHelper.performClick(view, settingsState?.hapticsEnabled == true)
                                            selectedFilter = pill.label
                                        } else Modifier
                                    )
                            ) {
                                // Credit card background layers
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(cardGradient)
                                        .background(shineGradient)
                                )

                                // Content
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(14.dp)
                                ) {
                                    if (pill.count != null) {
                                        Row(
                                            modifier = Modifier.fillMaxSize(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(
                                                modifier = Modifier.fillMaxHeight(),
                                                verticalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Icon(
                                                    imageVector = pill.icon,
                                                    contentDescription = null,
                                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Text(
                                                    text = pill.label,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                                    maxLines = 1
                                                )
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxHeight()
                                                    .widthIn(min = 36.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = pill.count.toString(),
                                                    style = MaterialTheme.typography.displayMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                    } else if (pill.displayValue != null) {
                                        Column(
                                            modifier = Modifier.fillMaxSize(),
                                            verticalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Icon(
                                                imageVector = pill.icon,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Text(
                                                text = pill.displayValue,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .basicMarquee(
                                                        iterations = Int.MAX_VALUE,
                                                        initialDelayMillis = 1000,
                                                        repeatDelayMillis = 2000
                                                    )
                                            )
                                            Text(
                                                text = pill.label,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1
                                            )
                                        }
                                    } else {
                                        Column(
                                            modifier = Modifier.fillMaxSize(),
                                            verticalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Icon(
                                                imageVector = pill.icon,
                                                contentDescription = null,
                                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Text(
                                                text = pill.label,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            val postPillsSpacing by animateDpAsState(
                targetValue = if (isCollapsed && !searchExpanded) 4.dp else 10.dp,
                label = "post-pills-spacing"
            )
            Spacer(modifier = Modifier.height(postPillsSpacing))

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
                        var showContextMenu by remember { mutableStateOf(false) }
                        var showDeleteDialog by remember { mutableStateOf(false) }

                        val executeSwipeAction: (SwipeAction) -> Unit = { action ->
                            HapticHelper.performClick(view, settingsState?.hapticsEnabled == true)
                            when (action) {
                                SwipeAction.Delete -> showDeleteDialog = true
                                SwipeAction.Edit -> onEditEntry(entry)
                                SwipeAction.CopyPassword -> onCopyPassword(entry)
                                SwipeAction.ToggleFavorite -> onToggleFavorite(entry)
                            }
                        }

                        Box {
                            if (settingsState?.swipeActionsEnabled != false) {
                                SwipeablePasswordCard(
                                    entry = entry,
                                    swipeLeftAction = settingsState?.swipeLeftAction ?: SwipeAction.Edit,
                                    swipeRightAction = settingsState?.swipeRightAction ?: SwipeAction.CopyPassword,
                                    onSwipeAction = executeSwipeAction,
                                    onClick = {
                                        HapticHelper.performClick(view, settingsState?.hapticsEnabled == true)
                                        onEntryClick(entry)
                                    },
                                    onLongPress = {
                                        HapticHelper.performLongPress(view, settingsState?.hapticsEnabled == true)
                                        showContextMenu = true
                                    },
                                    onCopyClick = {
                                        HapticHelper.performClick(view, settingsState?.hapticsEnabled == true)
                                        onCopyPassword(entry)
                                    },
                                    onEditClick = {
                                        HapticHelper.performClick(view, settingsState?.hapticsEnabled == true)
                                        onEditEntry(entry)
                                    },
                                    onFavoriteClick = {
                                        HapticHelper.performClick(view, settingsState?.hapticsEnabled == true)
                                        onToggleFavorite(entry)
                                    },
                                    onDeleteClick = {
                                        showDeleteDialog = true
                                    }
                                )
                            } else {
                                PasswordEntryItem(
                                    entry = entry,
                                    onClick = {
                                        HapticHelper.performClick(view, settingsState.hapticsEnabled)
                                        onEntryClick(entry)
                                    },
                                    onLongPress = {
                                        HapticHelper.performLongPress(view, settingsState.hapticsEnabled)
                                        showContextMenu = true
                                    },
                                    onCopyClick = {
                                        HapticHelper.performClick(view, settingsState.hapticsEnabled)
                                        onCopyPassword(entry)
                                    },
                                    onEditClick = {
                                        HapticHelper.performClick(view, settingsState.hapticsEnabled)
                                        onEditEntry(entry)
                                    },
                                    onFavoriteClick = {
                                        HapticHelper.performClick(view, settingsState.hapticsEnabled)
                                        onToggleFavorite(entry)
                                    },
                                    onDeleteClick = {
                                        showDeleteDialog = true
                                    }
                                )
                            }

                            DropdownMenu(
                                expanded = showContextMenu,
                                onDismissRequest = { showContextMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Copy Password") },
                                    onClick = {
                                        showContextMenu = false
                                        HapticHelper.performClick(view, settingsState?.hapticsEnabled == true)
                                        onCopyPassword(entry)
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.ContentCopy, contentDescription = null)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Edit") },
                                    onClick = {
                                        showContextMenu = false
                                        HapticHelper.performClick(view, settingsState?.hapticsEnabled == true)
                                        onEditEntry(entry)
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Edit, contentDescription = null)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Delete") },
                                    onClick = {
                                        showContextMenu = false
                                        showDeleteDialog = true
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Delete, contentDescription = null)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(if (entry.isFavorite) "Remove Favorite" else "Add Favorite") },
                                    onClick = {
                                        showContextMenu = false
                                        HapticHelper.performClick(view, settingsState?.hapticsEnabled == true)
                                        onToggleFavorite(entry)
                                    },
                                    leadingIcon = {
                                        Icon(
                                            if (entry.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                            contentDescription = null
                                        )
                                    }
                                )
                            }
                        }

                        if (showDeleteDialog) {
                            AlertDialog(
                                onDismissRequest = { showDeleteDialog = false },
                                title = { Text("Delete Entry") },
                                text = { Text("Are you sure you want to delete \"${entry.name}\"? This cannot be undone.") },
                                confirmButton = {
                                    Button(
                                        onClick = {
                                            showDeleteDialog = false
                                            onDeleteEntry(entry)
                                        }
                                    ) {
                                        Text("Delete")
                                    }
                                },
                                dismissButton = {
                                    OutlinedButton(onClick = { showDeleteDialog = false }) {
                                        Text("Cancel")
                                    }
                                }
                            )
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(120.dp))
                    }
                }
            }
        }
    }
}

// ── Shared bottom bar (rendered outside AnimatedContent in VaultApp) ──

@Composable
fun VaultBottomBar(
    currentScreen: String,
    onLockClick: () -> Unit,
    onDownloadClick: () -> Unit,
    onProfileClick: () -> Unit,
    onGeneratePasswordClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onPrimaryAction: () -> Unit,
    hapticsEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val iconTint = MaterialTheme.colorScheme.primary
    val activeIconTint = MaterialTheme.colorScheme.onPrimaryContainer
    val iconSize = Modifier.size(24.dp)

    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        Spacer(modifier = Modifier.weight(1f))

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
                    HapticHelper.performClick(view, hapticsEnabled)
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
                    HapticHelper.performClick(view, hapticsEnabled)
                    onDownloadClick()
                }, modifier = Modifier.size(48.dp)) {
                    Icon(
                        imageVector = Icons.Outlined.Download,
                        contentDescription = "Download vault",
                        tint = iconTint,
                        modifier = iconSize
                    )
                }
                // Profile
                IconButton(onClick = {
                    HapticHelper.performClick(view, hapticsEnabled)
                    onProfileClick()
                }, modifier = Modifier
                    .size(48.dp)
                    .then(
                        if (currentScreen == "profile") Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                        else Modifier
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = "Profile",
                        tint = if (currentScreen == "profile") activeIconTint else iconTint,
                        modifier = iconSize
                    )
                }
                // Password generator (before Settings per fix #2)
                IconButton(onClick = {
                    HapticHelper.performClick(view, hapticsEnabled)
                    onGeneratePasswordClick()
                }, modifier = Modifier.size(48.dp)) {
                    Icon(
                        imageVector = Icons.Outlined.Password,
                        contentDescription = "Generate Password",
                        tint = iconTint,
                        modifier = iconSize
                    )
                }
                // Settings
                IconButton(onClick = {
                    HapticHelper.performClick(view, hapticsEnabled)
                    onSettingsClick()
                }, modifier = Modifier
                    .size(48.dp)
                    .then(
                        if (currentScreen == "settings") Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                        else Modifier
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = "Settings",
                        tint = if (currentScreen == "settings") activeIconTint else iconTint,
                        modifier = iconSize
                    )
                }
            }
        }

        // Primary action button: Add on vault, Home on settings/profile
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
            ),
            onClick = {
                HapticHelper.performClick(view, hapticsEnabled)
                onPrimaryAction()
            }
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = if (currentScreen == "vault") Icons.Default.Add else Icons.Outlined.Home,
                    contentDescription = if (currentScreen == "vault") "Add New Password" else "Back to vault",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = iconSize
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_NO)
@Composable
private fun VaultScreenEmptyPreview() {
    VaultTheme {
        VaultScreen(
            vaultName = "My Vault",
            passwordEntries = emptyList()
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
                PasswordEntry(id = "1", name = "Google Account", username = "me@gmail.com", password = "secret", website = "google.com"),
                PasswordEntry(id = "2", name = "My Bank", username = "user123", password = "pass", website = "mybank.com"),
                PasswordEntry(id = "3", name = "Social Media", username = "social_user", password = "pwd123"),
            )
        )
    }
}
