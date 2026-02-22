package com.lonley.dev.vault

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.text.input.VisualTransformation
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.IconButton
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.AnnotatedString
import android.Manifest
import android.os.Build
import com.lonley.dev.vault.model.FontScale
import com.lonley.dev.vault.model.PasswordEntry
import com.lonley.dev.vault.model.PlanType
import com.lonley.dev.vault.model.VaultUiState
import com.lonley.dev.vault.util.HapticHelper
import com.lonley.dev.vault.viewmodel.VaultViewModel
import com.lonley.dev.vault.views.AddPasswordContent
import com.lonley.dev.vault.views.HomeScreen
import com.lonley.dev.vault.views.PasswordDetailScreen
import com.lonley.dev.vault.views.PasswordEditScreen
import com.lonley.dev.vault.views.SettingsScreen
import com.lonley.dev.vault.views.UserProfileScreen
import com.lonley.dev.vault.views.VaultScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultApp(viewModel: VaultViewModel) {
    val context = LocalContext.current

    val uiState by viewModel.uiState.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val settingsState by viewModel.settingsState.collectAsState()

    val downloadLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        if (uri != null) {
            val bytes = viewModel.getVaultBytes()
            if (bytes != null) {
                try {
                    context.contentResolver.openOutputStream(uri)?.use { it.write(bytes) }
                    Toast.makeText(context, "Vault saved", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Failed to save: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "No vault data to save", Toast.LENGTH_SHORT).show()
            }
        }
    }

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

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { /* Permission result - no action needed, best-effort */ }

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
            ExpressivePasswordDialog(
                foundVaultFileName = state.fileName,
                onDismiss = { viewModel.noVault() },
                onConfirm = { password ->
                    viewModel.unlockVault(password.toCharArray())
                },
                hapticsEnabled = settingsState.hapticsEnabled
            )
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
            val addSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            val scope = rememberCoroutineScope()
            val clipboardManager = LocalClipboardManager.current
            val isSuspended by viewModel.isSuspended.collectAsState()
            val suspendError by viewModel.suspendError.collectAsState()

            val copyToClipboard: (String) -> Unit = { text ->
                viewModel.resetAutoLockTimer()
                clipboardManager.setText(AnnotatedString(text))
                Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
            }

            val launchDownload = {
                viewModel.resetAutoLockTimer()
                downloadLauncher.launch(viewModel.getVaultFileName())
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
            BackHandler(enabled = editingEntry != null) {
                editingEntry = null
            }
            BackHandler(enabled = editingEntry == null && selectedEntry != null) {
                selectedEntry = null
            }
            BackHandler(enabled = showSettings) {
                showSettings = false
            }
            BackHandler(enabled = showProfile) {
                showProfile = false
            }
            BackHandler(enabled = showAddSheet) {
                scope.launch { addSheetState.hide() }.invokeOnCompletion {
                    if (!addSheetState.isVisible) showAddSheet = false
                }
            }

            if (editingEntry != null) {
                PasswordEditScreen(
                    entry = editingEntry!!,
                    settingsState = settingsState,
                    onCancel = {
                        editingEntry = null
                    },
                    onSave = { id, name, username, password, website, comments,
                               isFavorite, isSubscription, planType, price,
                               subscriptionEmail, startDate, reminderEnabled ->
                        viewModel.resetAutoLockTimer()
                        if (reminderEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        viewModel.updatePassword(
                            id, name, username, password, website, comments,
                            isFavorite, isSubscription, planType, price,
                            subscriptionEmail, startDate, reminderEnabled
                        )
                        val updated = editingEntry!!.copy(
                            name = name,
                            username = username,
                            password = password,
                            website = website,
                            comments = comments,
                            isFavorite = isFavorite,
                            isSubscription = isSubscription,
                            planType = planType,
                            price = price,
                            subscriptionEmail = subscriptionEmail,
                            startDate = startDate,
                            reminderEnabled = reminderEnabled
                        )
                        selectedEntry = updated
                        editingEntry = null
                    }
                )
            } else if (selectedEntry != null) {
                PasswordDetailScreen(
                    entry = selectedEntry!!,
                    settingsState = settingsState,
                    onDismiss = { selectedEntry = null },
                    onEditClick = { editingEntry = selectedEntry },
                    onDelete = { id ->
                        viewModel.deletePassword(id)
                        selectedEntry = null
                    },
                    onCopyToClipboard = copyToClipboard,
                    onToggleFavorite = {
                        viewModel.toggleFavorite(selectedEntry!!.id)
                        selectedEntry = selectedEntry!!.copy(isFavorite = !selectedEntry!!.isFavorite)
                    },
                    onToggleReminder = {
                        if (!selectedEntry!!.reminderEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        viewModel.toggleReminder(selectedEntry!!.id)
                        selectedEntry = selectedEntry!!.copy(reminderEnabled = !selectedEntry!!.reminderEnabled)
                    }
                )
            } else if (showProfile) {
                viewModel.resetAutoLockTimer()
                UserProfileScreen(
                    username = state.username,
                    email = state.email,
                    encryptionType = state.encryptionType,
                    lastUpdatedAt = state.lastUpdatedAt,
                    settingsState = settingsState,
                    onBackClick = { showProfile = false },
                    onLockClick = { viewModel.lockVault() },
                    onDownloadClick = launchDownload,
                    onSettingsClick = {
                        showProfile = false
                        showSettings = true
                    }
                )
            } else if (showSettings) {
                viewModel.resetAutoLockTimer()
                SettingsScreen(
                    settingsState = settingsState,
                    onThemeModeChange = { viewModel.setThemeMode(it) },
                    onAccentColorChange = { viewModel.setAccentColor(it) },
                    onHapticsToggle = { viewModel.setHapticsEnabled(it) },
                    onFontScaleChange = { viewModel.setFontScale(it) },
                    onScrollVibrationToggle = { screen, enabled -> viewModel.setScrollVibration(screen, enabled) },
                    onBackClick = { showSettings = false },
                    onLockClick = { viewModel.lockVault() },
                    onDownloadClick = launchDownload,
                    onProfileClick = {
                        showSettings = false
                        showProfile = true
                    }
                )
            } else {
                VaultScreen(
                    vaultName = state.vaultName,
                    passwordEntries = state.entries,
                    settingsState = settingsState,
                    isLoading = state.isLoading,
                    lastUpdatedAt = state.lastUpdatedAt,
                    onAddPasswordClick = {
                        viewModel.resetAutoLockTimer()
                        showAddSheet = true
                    },
                    onBackClick = { viewModel.lockVault() },
                    onDownloadClick = launchDownload,
                    onProfileClick = {
                        showProfile = true
                    },
                    onSettingsClick = { showSettings = true },
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
                    }
                )
            }

            if (showAddSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showAddSheet = false },
                    sheetState = addSheetState
                ) {
                    AddPasswordContent(
                        onConfirm = { name, username, password, website, comments,
                                      isSubscription, planType, price, subscriptionEmail,
                                      startDate, reminderEnabled ->
                            viewModel.resetAutoLockTimer()
                            if (reminderEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                            viewModel.addPassword(
                                name = name,
                                username = username,
                                password = password,
                                website = website.ifBlank { null },
                                comments = comments.ifBlank { null },
                                isSubscription = isSubscription,
                                planType = planType,
                                price = price,
                                subscriptionEmail = subscriptionEmail,
                                startDate = startDate,
                                reminderEnabled = reminderEnabled
                            )
                            scope.launch { addSheetState.hide() }.invokeOnCompletion {
                                if (!addSheetState.isVisible) showAddSheet = false
                            }
                        },
                        onCancel = {
                            scope.launch { addSheetState.hide() }.invokeOnCompletion {
                                if (!addSheetState.isVisible) showAddSheet = false
                            }
                        },
                        hapticsEnabled = settingsState.hapticsEnabled
                    )
                }
            }

            } // end Box (touch interceptor)
        }

        is VaultUiState.Error -> {
            val isVaultDeleted = state.previous is VaultUiState.Locked
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
                ExpressivePasswordDialog(
                    foundVaultFileName = (state.previous as? VaultUiState.PromptUnlock)?.fileName ?: "",
                    onDismiss = { viewModel.dismissError() },
                    onConfirm = { password ->
                        viewModel.unlockVault(password.toCharArray())
                    },
                    errorMessage = state.message,
                    hapticsEnabled = settingsState.hapticsEnabled
                )
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
    hapticsEnabled: Boolean = false
) {
    val view = LocalView.current
    var masterPasswordInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

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

                OutlinedTextField(
                    value = masterPasswordInput,
                    onValueChange = { masterPasswordInput = it },
                    label = { Text("Master Password") },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    isError = errorMessage != null,
                    trailingIcon = {
                        IconButton(onClick = {
                            HapticHelper.performClick(view, hapticsEnabled)
                            passwordVisible = !passwordVisible
                        }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

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
                            onConfirm(masterPasswordInput)
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
