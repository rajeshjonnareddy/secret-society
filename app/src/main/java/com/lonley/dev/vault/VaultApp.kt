package com.lonley.dev.vault

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.AnnotatedString
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.lonley.dev.vault.model.EntryType
import com.lonley.dev.vault.model.FontScale
import com.lonley.dev.vault.model.Network
import com.lonley.dev.vault.model.PasswordEntry
import com.lonley.dev.vault.model.PlanType
import com.lonley.dev.vault.model.ChangePasswordState
import com.lonley.dev.vault.model.RecoveryState
import com.lonley.dev.vault.model.VaultUiState
import com.lonley.dev.vault.util.HapticHelper
import com.lonley.dev.vault.viewmodel.VaultViewModel
import com.lonley.dev.vault.views.AddPasswordContent
import com.lonley.dev.vault.views.ChangePasswordScreen
import com.lonley.dev.vault.views.HomeScreen
import com.lonley.dev.vault.views.PasswordTextField
import com.lonley.dev.vault.views.PasswordDetailScreen
import com.lonley.dev.vault.views.PasswordEditScreen
import com.lonley.dev.vault.views.SettingsScreen
import com.lonley.dev.vault.views.UserProfileScreen
import com.lonley.dev.vault.views.PasswordGeneratorDialog
import com.lonley.dev.vault.views.VaultBottomBar
import com.lonley.dev.vault.views.RecoveryEntryScreen
import com.lonley.dev.vault.views.RecoveryPhraseScreen
import com.lonley.dev.vault.views.VaultScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultApp(viewModel: VaultViewModel) {
    val context = LocalContext.current

    val uiState by viewModel.uiState.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val settingsState by viewModel.settingsState.collectAsState()

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            // Query the proper display name from the content resolver
            val displayName = context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (cursor.moveToFirst() && nameIndex >= 0) cursor.getString(nameIndex) else null
            } ?: uri.lastPathSegment?.substringAfterLast('/') ?: "imported.vlt"
            if (bytes != null) {
                viewModel.importVault(bytes, displayName)
            } else {
                viewModel.noVault()
            }
        } else {
            viewModel.noVault()
        }
    }

    var pendingExportFileName by remember { mutableStateOf<String?>(null) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        // Export regardless of whether permission was granted — notification is best-effort
        val fileName = pendingExportFileName
        if (fileName != null) {
            pendingExportFileName = null
            viewModel.exportToDownloads(context, fileName)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.initialize()
    }

    when (val state = uiState) {
        is VaultUiState.Initializing -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                LinearProgressIndicator(
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            }
        }

        is VaultUiState.Locked -> {
            HomeScreen(
                createNewVault = { vaultName, username, email, masterPassword, encryptionType ->
                    viewModel.createVault(
                        vaultName = vaultName,
                        username = username,
                        email = email,
                        password = masterPassword.toCharArray(),
                        encryptionType = encryptionType
                    )
                },
                onUploadClick = { viewModel.openExistingVault() },
                hapticsEnabled = settingsState.hapticsEnabled
            )
        }

        is VaultUiState.PickFile -> {
            HomeScreen(
                createNewVault = { vaultName, username, email, masterPassword, encryptionType ->
                    viewModel.createVault(
                        vaultName = vaultName,
                        username = username,
                        email = email,
                        password = masterPassword.toCharArray(),
                        encryptionType = encryptionType
                    )
                },
                onUploadClick = { viewModel.openExistingVault() },
                hapticsEnabled = settingsState.hapticsEnabled
            )
            LaunchedEffect(Unit) {
                filePickerLauncher.launch(arrayOf("*/*"))
            }
        }

        is VaultUiState.PromptUnlock -> {
            val recoveryState by viewModel.recoveryState.collectAsState()
            val hasRecovery by viewModel.hasRecovery.collectAsState()

            when (recoveryState) {
                is RecoveryState.PromptEntry, is RecoveryState.Error -> {
                    RecoveryEntryScreen(
                        errorMessage = (recoveryState as? RecoveryState.Error)?.message,
                        hapticsEnabled = settingsState.hapticsEnabled,
                        onRecover = { words, newPassword ->
                            viewModel.attemptRecovery(words, newPassword)
                        },
                        onCancel = { viewModel.cancelRecovery() }
                    )
                }
                else -> {
                    ExpressivePasswordDialog(
                        foundVaultFileName = state.fileName,
                        onDismiss = { viewModel.noVault() },
                        onConfirm = { password ->
                            viewModel.unlockVault(password.toCharArray())
                        },
                        hapticsEnabled = settingsState.hapticsEnabled,
                        showForgotPassword = hasRecovery,
                        onForgotPassword = { viewModel.startRecoveryFlow() }
                    )
                }
            }
        }

        is VaultUiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        is VaultUiState.Unlocked -> {
            var showAddSheet by remember { mutableStateOf(false) }
            var showSettings by remember { mutableStateOf(false) }
            var showProfile by remember { mutableStateOf(false) }
            var selectedEntry by remember { mutableStateOf<PasswordEntry?>(null) }
            var editingEntry by remember { mutableStateOf<PasswordEntry?>(null) }
            var showChangePassword by remember { mutableStateOf(false) }
            var showGeneratorDialog by remember { mutableStateOf(false) }
            var generatedPasswordForAdd by remember { mutableStateOf("") }
            val addSheetState = rememberModalBottomSheetState(
                skipPartiallyExpanded = true,
                confirmValueChange = { it != androidx.compose.material3.SheetValue.Hidden }
            )
            val scope = rememberCoroutineScope()
            val clipboardManager = LocalClipboardManager.current
            val isSuspended by viewModel.isSuspended.collectAsState()
            val suspendError by viewModel.suspendError.collectAsState()
            val recoveryState by viewModel.recoveryState.collectAsState()
            val hasRecovery by viewModel.hasRecovery.collectAsState()
            val changePasswordState by viewModel.changePasswordState.collectAsState()
            var showRecoveryPrompt by remember { mutableStateOf(false) }
            var recoveryFromCreation by remember { mutableStateOf(false) }
            var showExportDialog by remember { mutableStateOf(false) }
            val lastExportedAt by viewModel.lastExportedAt.collectAsState()

            val copyToClipboard: (String) -> Unit = { text ->
                viewModel.resetAutoLockTimer()
                clipboardManager.setText(AnnotatedString(text))
                Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
            }

            val launchDownload = {
                viewModel.resetAutoLockTimer()
                val exportName = viewModel.getVaultFileName()
                val needsPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                if (needsPermission) {
                    pendingExportFileName = exportName
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    viewModel.exportToDownloads(context, exportName)
                }
            }

            // Handle change password success
            LaunchedEffect(changePasswordState) {
                if (changePasswordState is ChangePasswordState.Success) {
                    Toast.makeText(context, "Master password changed", Toast.LENGTH_SHORT).show()
                    showChangePassword = false
                    showProfile = true
                    viewModel.resetChangePasswordState()
                }
            }

            // Post-creation recovery prompt
            LaunchedEffect(state.justCreated) {
                if (state.justCreated) {
                    showRecoveryPrompt = true
                    viewModel.clearJustCreated()
                }
            }

            if (showRecoveryPrompt) {
                AlertDialog(
                    onDismissRequest = { showRecoveryPrompt = false },
                    title = { Text("Set Up Recovery Phrase?") },
                    text = {
                        Text("Generate a 12-word recovery phrase so you can reset your master password if you forget it.")
                    },
                    confirmButton = {
                        Button(onClick = {
                            showRecoveryPrompt = false
                            recoveryFromCreation = true
                            viewModel.generateRecoveryPhrase()
                        }) {
                            Text("Generate")
                        }
                    },
                    dismissButton = {
                        OutlinedButton(onClick = { showRecoveryPrompt = false }) {
                            Text("Skip")
                        }
                    }
                )
            }

            if (isSuspended) {
                ExpressivePasswordDialog(
                    foundVaultFileName = "",
                    onDismiss = { viewModel.lockVault() },
                    onConfirm = { password ->
                        viewModel.verifyMasterPassword(password.toCharArray())
                    },
                    errorMessage = suspendError,
                    hapticsEnabled = settingsState.hapticsEnabled
                )
            } else Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                awaitPointerEvent(PointerEventPass.Initial)
                                if (!viewModel.isSuspended.value) {
                                    viewModel.resetAutoLockTimer()
                                }
                            }
                        }
                    }
            ) {

            // Back button handling: navigate back through screen stack
            BackHandler(enabled = recoveryState is RecoveryState.ShowPhrase) {
                // Block back navigation — user must tap "Secure Your Vault"
            }
            BackHandler(enabled = editingEntry != null) {
                editingEntry = null
            }
            BackHandler(enabled = editingEntry == null && selectedEntry != null) {
                selectedEntry = null
            }
            BackHandler(enabled = showSettings) {
                showSettings = false
            }
            BackHandler(enabled = showChangePassword) {
                showChangePassword = false
                showProfile = true
                viewModel.resetChangePasswordState()
            }
            BackHandler(enabled = showProfile) {
                showProfile = false
            }
            BackHandler(enabled = showAddSheet) {
                showAddSheet = false
            }

            // Snapshot entries so exit animations don't crash on null
            var lastSelectedEntry by remember { mutableStateOf(selectedEntry) }
            var lastEditingEntry by remember { mutableStateOf(editingEntry) }
            if (selectedEntry != null) lastSelectedEntry = selectedEntry
            if (editingEntry != null) lastEditingEntry = editingEntry

            // Derive a screen key for animated transitions
            val showRecoveryPhrase = recoveryState is RecoveryState.ShowPhrase
            val screenKey = when {
                showRecoveryPhrase -> "recovery"
                showChangePassword -> "changePassword"
                editingEntry != null -> "edit"
                selectedEntry != null -> "detail"
                showProfile -> "profile"
                showSettings -> "settings"
                else -> "vault"
            }

            // Screen depth for determining slide direction
            val screenDepth = mapOf(
                "vault" to 0, "profile" to 1, "settings" to 1, "recovery" to 1,
                "changePassword" to 2, "detail" to 2, "edit" to 3
            )

            Box(modifier = Modifier.fillMaxSize()) {
            AnimatedContent(
                targetState = screenKey,
                transitionSpec = {
                    val fromDepth = screenDepth[initialState] ?: 0
                    val toDepth = screenDepth[targetState] ?: 0
                    val slideOffset = { width: Int -> width / 4 }
                    if (toDepth >= fromDepth) {
                        (slideInHorizontally(initialOffsetX = slideOffset) + fadeIn()) togetherWith
                            (slideOutHorizontally(targetOffsetX = { -it / 4 }) + fadeOut())
                    } else {
                        (slideInHorizontally(initialOffsetX = { -it / 4 }) + fadeIn()) togetherWith
                            (slideOutHorizontally(targetOffsetX = slideOffset) + fadeOut())
                    }
                },
                label = "screenTransition"
            ) { targetScreen ->
                when (targetScreen) {
                    "edit" -> {
                        val entry = editingEntry ?: lastEditingEntry ?: return@AnimatedContent
                        PasswordEditScreen(
                            entry = entry,
                            settingsState = settingsState,
                            onCancel = {
                                editingEntry = null
                            },
                            onDelete = { id ->
                                viewModel.deletePassword(id)
                                editingEntry = null
                                selectedEntry = null
                            },
                            onToggleFavorite = {
                                val current = editingEntry ?: return@PasswordEditScreen
                                viewModel.toggleFavorite(current.id)
                                editingEntry = current.copy(isFavorite = !current.isFavorite)
                                selectedEntry = current.copy(isFavorite = !current.isFavorite)
                            },
                            onSave = { id, name, username, email, password, website, comments,
                                       isFavorite, isSubscription, planType, price,
                                       subscriptionEmail, startDate, reminderEnabled,
                                       entryType, phraseWordCount,
                                       walletAddress, seedPhrase, network, exchange ->
                                viewModel.resetAutoLockTimer()
                                if (reminderEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                                viewModel.updatePassword(
                                    id, name,
                                    username = username.ifBlank { null },
                                    password = password,
                                    website = website,
                                    comments = comments,
                                    email = email.ifBlank { null },
                                    isFavorite = isFavorite,
                                    isSubscription = isSubscription,
                                    planType = planType,
                                    price = price,
                                    subscriptionEmail = subscriptionEmail,
                                    startDate = startDate,
                                    reminderEnabled = reminderEnabled,
                                    entryType = entryType,
                                    phraseWordCount = phraseWordCount,
                                    walletAddress = walletAddress,
                                    seedPhrase = seedPhrase,
                                    network = network,
                                    exchange = exchange
                                )
                                val updated = entry.copy(
                                    name = name,
                                    username = username.ifBlank { null },
                                    email = email.ifBlank { null },
                                    password = password,
                                    website = website,
                                    comments = comments,
                                    isFavorite = isFavorite,
                                    isSubscription = isSubscription,
                                    planType = planType,
                                    price = price,
                                    subscriptionEmail = subscriptionEmail,
                                    startDate = startDate,
                                    reminderEnabled = reminderEnabled,
                                    entryType = entryType,
                                    phraseWordCount = phraseWordCount,
                                    walletAddress = walletAddress,
                                    seedPhrase = seedPhrase,
                                    network = network,
                                    exchange = exchange
                                )
                                selectedEntry = updated
                                editingEntry = null
                            }
                        )
                    }
                    "detail" -> {
                        val entry = selectedEntry ?: lastSelectedEntry ?: return@AnimatedContent
                        PasswordDetailScreen(
                            entry = entry,
                            settingsState = settingsState,
                            onDismiss = { selectedEntry = null },
                            onEditClick = { editingEntry = selectedEntry },
                            onDelete = { id ->
                                viewModel.deletePassword(id)
                                selectedEntry = null
                            },
                            onCopyToClipboard = copyToClipboard,
                            onToggleFavorite = {
                                val current = selectedEntry ?: return@PasswordDetailScreen
                                viewModel.toggleFavorite(current.id)
                                selectedEntry = current.copy(isFavorite = !current.isFavorite)
                            },
                            onToggleReminder = {
                                val current = selectedEntry ?: return@PasswordDetailScreen
                                if (!current.reminderEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                                viewModel.toggleReminder(current.id)
                                selectedEntry = current.copy(reminderEnabled = !current.reminderEnabled)
                            }
                        )
                    }
                    "recovery" -> {
                        // Snapshot words so they survive the exit animation after recoveryState resets to None
                        val phraseWords = remember { (recoveryState as? RecoveryState.ShowPhrase)?.words ?: emptyList() }
                        RecoveryPhraseScreen(
                            words = phraseWords,
                            onConfirm = {
                                viewModel.confirmRecoveryPhraseSeen()
                                if (!recoveryFromCreation) {
                                    showProfile = true
                                } else {
                                    showProfile = false
                                    showSettings = false
                                    selectedEntry = null
                                    editingEntry = null
                                }
                                recoveryFromCreation = false
                            }
                        )
                    }
                    "changePassword" -> {
                        viewModel.resetAutoLockTimer()
                        ChangePasswordScreen(
                            errorMessage = (changePasswordState as? ChangePasswordState.Error)?.message,
                            onChangePassword = { oldPw, newPw ->
                                viewModel.resetAutoLockTimer()
                                viewModel.changeMasterPassword(oldPw, newPw)
                            },
                            onCancel = {
                                showChangePassword = false
                                showProfile = true
                                viewModel.resetChangePasswordState()
                            }
                        )
                    }
                    "profile" -> {
                        viewModel.resetAutoLockTimer()
                        UserProfileScreen(
                            username = state.username,
                            email = state.email,
                            encryptionType = state.encryptionType,
                            lastUpdatedAt = state.lastUpdatedAt,
                            lastExportedAt = lastExportedAt,
                            settingsState = settingsState,
                            hasRecovery = hasRecovery,
                            onSetupRecovery = {
                                recoveryFromCreation = false
                                viewModel.generateRecoveryPhrase()
                            },
                            onChangePassword = {
                                viewModel.resetAutoLockTimer()
                                showChangePassword = true
                                showProfile = false
                            },
                            onExportData = {
                                viewModel.resetAutoLockTimer()
                                showExportDialog = true
                            }
                        )
                    }
                    "settings" -> {
                        viewModel.resetAutoLockTimer()
                        SettingsScreen(
                            settingsState = settingsState,
                            onThemeModeChange = { viewModel.setThemeMode(it) },
                            onAccentColorChange = { viewModel.setAccentColor(it) },
                            onHapticsToggle = { viewModel.setHapticsEnabled(it) },
                            onFontScaleChange = { viewModel.setFontScale(it) },
                            onAutoLockTimeoutChange = { viewModel.setAutoLockTimeout(it) },
                            onScrollVibrationToggle = { screen, enabled -> viewModel.setScrollVibration(screen, enabled) },
                            onSwipeActionsToggle = { viewModel.setSwipeActionsEnabled(it) },
                            onSwipeLeftActionChange = { viewModel.setSwipeLeftAction(it) },
                            onSwipeRightActionChange = { viewModel.setSwipeRightAction(it) }
                        )
                    }
                    else -> {
                        VaultScreen(
                            vaultName = state.vaultName,
                            passwordEntries = state.entries,
                            settingsState = settingsState,
                            isLoading = state.isLoading,
                            lastUpdatedAt = state.lastUpdatedAt,
                            onEntryClick = { entry ->
                                viewModel.resetAutoLockTimer()
                                selectedEntry = entry
                            },
                            onCopyPassword = { entry ->
                                copyToClipboard(entry.password)
                            },
                            onEditEntry = { entry ->
                                viewModel.resetAutoLockTimer()
                                selectedEntry = entry
                                editingEntry = entry
                            },
                            onToggleFavorite = { entry ->
                                viewModel.resetAutoLockTimer()
                                viewModel.toggleFavorite(entry.id)
                            },
                            onDeleteEntry = { entry ->
                                viewModel.resetAutoLockTimer()
                                viewModel.deletePassword(entry.id)
                            }
                        )
                    }
                }
            }

            // Fixed FAB — only on vault/settings/profile screens
            if (screenKey in listOf("vault", "settings", "profile")) {
                VaultBottomBar(
                    currentScreen = screenKey,
                    onLockClick = { viewModel.lockVault() },
                    onDownloadClick = launchDownload,
                    onProfileClick = {
                        showProfile = true
                        showSettings = false
                    },
                    onGeneratePasswordClick = {
                        viewModel.resetAutoLockTimer()
                        showGeneratorDialog = true
                    },
                    onSettingsClick = {
                        showSettings = true
                        showProfile = false
                    },
                    onPrimaryAction = {
                        if (screenKey == "vault") {
                            viewModel.resetAutoLockTimer()
                            showAddSheet = true
                        } else {
                            showSettings = false
                            showProfile = false
                        }
                    },
                    hapticsEnabled = settingsState.hapticsEnabled,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
            } // end Box

            if (showExportDialog) {
                var exportFileName by remember { mutableStateOf(viewModel.getUsername().ifEmpty { "vault" }) }
                AlertDialog(
                    onDismissRequest = { showExportDialog = false },
                    title = { Text("Export Vault") },
                    text = {
                        Column {
                            Text(
                                text = "Choose a file name for your vault backup.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(
                                value = exportFileName,
                                onValueChange = { exportFileName = it },
                                label = { Text("File name") },
                                suffix = { Text(".vlt") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.medium
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showExportDialog = false
                                val fileName = "${exportFileName.trim().ifEmpty { "vault" }}.vlt"
                                val needsPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                    ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                                if (needsPermission) {
                                    pendingExportFileName = fileName
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    viewModel.exportToDownloads(context, fileName)
                                }
                            },
                            enabled = exportFileName.isNotBlank()
                        ) {
                            Text("Export")
                        }
                    },
                    dismissButton = {
                        OutlinedButton(onClick = { showExportDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            if (showGeneratorDialog) {
                PasswordGeneratorDialog(
                    onDismiss = { showGeneratorDialog = false },
                    onAddPassword = { password ->
                        clipboardManager.setText(AnnotatedString(password))
                        Toast.makeText(context, "Password copied", Toast.LENGTH_SHORT).show()
                        generatedPasswordForAdd = password
                        showGeneratorDialog = false
                        showAddSheet = true
                    },
                    hapticsEnabled = settingsState.hapticsEnabled,
                    onInteraction = { viewModel.resetAutoLockTimer() }
                )
            }

            if (showAddSheet) {
                ModalBottomSheet(
                    onDismissRequest = { /* Dismiss only via Cancel/Save buttons */ },
                    sheetState = addSheetState
                ) {
                    Box(modifier = Modifier.fillMaxHeight(0.85f).pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                awaitPointerEvent(PointerEventPass.Initial)
                                viewModel.resetAutoLockTimer()
                            }
                        }
                    }) {
                    AddPasswordContent(
                        onConfirm = { name, username, email, password, website, comments,
                                      isSubscription, planType, price, subscriptionEmail,
                                      startDate, reminderEnabled, entryType, phraseWordCount,
                                      walletAddress, seedPhrase, network, exchange ->
                            viewModel.resetAutoLockTimer()
                            if (reminderEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                            viewModel.addPassword(
                                name = name,
                                username = username.ifBlank { null },
                                password = password,
                                website = website.ifBlank { null },
                                comments = comments.ifBlank { null },
                                email = email.ifBlank { null },
                                isSubscription = isSubscription,
                                planType = planType,
                                price = price,
                                subscriptionEmail = subscriptionEmail,
                                startDate = startDate,
                                reminderEnabled = reminderEnabled,
                                entryType = entryType,
                                phraseWordCount = phraseWordCount,
                                walletAddress = walletAddress,
                                seedPhrase = seedPhrase,
                                network = network,
                                exchange = exchange
                            )
                            generatedPasswordForAdd = ""
                            showAddSheet = false
                        },
                        onCancel = {
                            generatedPasswordForAdd = ""
                            showAddSheet = false
                        },
                        hapticsEnabled = settingsState.hapticsEnabled,
                        initialPassword = generatedPasswordForAdd,
                        onInteraction = { viewModel.resetAutoLockTimer() }
                    )
                    }
                }
            }

            } // end Box (touch interceptor)
        }

        is VaultUiState.Error -> {
            val isVaultDeleted = state.previous is VaultUiState.Locked
            val recoveryState by viewModel.recoveryState.collectAsState()
            val hasRecovery by viewModel.hasRecovery.collectAsState()

            if (isVaultDeleted) {
                AlertDialog(
                    onDismissRequest = { viewModel.dismissError() },
                    title = { Text("Vault Deleted") },
                    text = { Text(state.message) },
                    confirmButton = {
                        TextButton(onClick = { viewModel.dismissError() }) {
                            Text("OK")
                        }
                    }
                )
            } else {
                when (recoveryState) {
                    is RecoveryState.PromptEntry, is RecoveryState.Error -> {
                        RecoveryEntryScreen(
                            errorMessage = (recoveryState as? RecoveryState.Error)?.message,
                            hapticsEnabled = settingsState.hapticsEnabled,
                            onRecover = { words, newPassword ->
                                viewModel.attemptRecovery(words, newPassword)
                            },
                            onCancel = { viewModel.cancelRecovery() }
                        )
                    }
                    else -> {
                        ExpressivePasswordDialog(
                            foundVaultFileName = (state.previous as? VaultUiState.PromptUnlock)?.fileName ?: "",
                            onDismiss = { viewModel.dismissError() },
                            onConfirm = { password ->
                                viewModel.unlockVault(password.toCharArray())
                            },
                            errorMessage = state.message,
                            hapticsEnabled = settingsState.hapticsEnabled,
                            showForgotPassword = hasRecovery,
                            onForgotPassword = { viewModel.startRecoveryFlow() }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpressivePasswordDialog(
    foundVaultFileName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    errorMessage: String? = null,
    hapticsEnabled: Boolean = false,
    showForgotPassword: Boolean = false,
    onForgotPassword: () -> Unit = {}
) {
    val view = LocalView.current
    var masterPasswordInput by remember { mutableStateOf("") }
    var emptyError by remember { mutableStateOf(false) }

    BasicAlertDialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp,
            modifier = Modifier.widthIn(max = 320.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Unlock Vault",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (foundVaultFileName.isNotEmpty()) {
                    Text(
                        text = if (foundVaultFileName.startsWith(".")) foundVaultFileName.substring(1) else foundVaultFileName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = if (errorMessage != null) 8.dp else 24.dp)
                    )
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                if (emptyError) {
                    Text(
                        text = "Password cannot be empty",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                PasswordTextField(
                    value = masterPasswordInput,
                    onValueChange = {
                        masterPasswordInput = it
                        if (it.isNotEmpty()) emptyError = false
                    },
                    label = "Master Password",
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    isError = errorMessage != null || emptyError
                )

                if (showForgotPassword) {
                    TextButton(
                        onClick = {
                            HapticHelper.performClick(view, hapticsEnabled)
                            onForgotPassword()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Text(
                            text = "Forgot Password?",
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(if (showForgotPassword) 8.dp else 24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(onClick = {
                        HapticHelper.performClick(view, hapticsEnabled)
                        onDismiss()
                    }) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            HapticHelper.performClick(view, hapticsEnabled)
                            if (masterPasswordInput.isEmpty()) {
                                emptyError = true
                            } else {
                                emptyError = false
                                onConfirm(masterPasswordInput)
                            }
                        },
                        contentPadding = ButtonDefaults.ButtonWithIconContentPadding
                    ) {
                        Icon(Icons.Default.LockOpen, contentDescription = null)
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text("Unlock")
                    }
                }
            }
        }
    }
}
