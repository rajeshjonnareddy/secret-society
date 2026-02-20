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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.IconButton
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import com.lonley.dev.vault.model.PasswordEntry
import com.lonley.dev.vault.model.VaultUiState
import com.lonley.dev.vault.viewmodel.VaultViewModel
import com.lonley.dev.vault.views.AddPasswordContent
import com.lonley.dev.vault.views.HomeScreen
import com.lonley.dev.vault.views.PasswordDetailDialog
import com.lonley.dev.vault.views.SettingsScreen
import com.lonley.dev.vault.views.VaultScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultApp(viewModel: VaultViewModel) {
    val context = LocalContext.current

    val uiState by viewModel.uiState.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()

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
                onUploadClick = { viewModel.openExistingVault() }
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
                onUploadClick = { viewModel.openExistingVault() }
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
                }
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
            var selectedEntry by remember { mutableStateOf<PasswordEntry?>(null) }
            var editMode by remember { mutableStateOf(false) }
            val addSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            val scope = rememberCoroutineScope()
            val clipboardManager = LocalClipboardManager.current

            val copyToClipboard: (String) -> Unit = { text ->
                clipboardManager.setText(AnnotatedString(text))
                Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
            }

            val launchDownload = {
                downloadLauncher.launch(viewModel.getVaultFileName())
            }

            if (showSettings) {
                SettingsScreen(
                    username = state.username,
                    email = state.email,
                    encryptionType = state.encryptionType,
                    themeMode = themeMode,
                    onThemeModeChange = { viewModel.setThemeMode(it) },
                    onBackClick = { showSettings = false },
                    onLockClick = { viewModel.lockVault() },
                    onDownloadClick = launchDownload
                )
            } else {
                VaultScreen(
                    vaultName = state.vaultName,
                    passwordEntries = state.entries,
                    isLoading = state.isLoading,
                    onAddPasswordClick = { showAddSheet = true },
                    onBackClick = { viewModel.lockVault() },
                    onDownloadClick = launchDownload,
                    onSettingsClick = { showSettings = true },
                    onEntryClick = { entry ->
                        selectedEntry = entry
                        editMode = false
                    },
                    onCopyPassword = { entry ->
                        copyToClipboard(entry.password)
                    },
                    onEditEntry = { entry ->
                        selectedEntry = entry
                        editMode = true
                    }
                )
            }

            if (selectedEntry != null) {
                PasswordDetailDialog(
                    entry = selectedEntry!!,
                    startInEditMode = editMode,
                    themeMode = themeMode,
                    onDismiss = { selectedEntry = null },
                    onSave = { id, name, username, password, website, comments ->
                        viewModel.updatePassword(id, name, username, password, website, comments)
                        selectedEntry = selectedEntry?.copy(
                            name = name,
                            username = username,
                            password = password,
                            website = website,
                            comments = comments
                        )
                        editMode = false
                    },
                    onDelete = { id ->
                        viewModel.deletePassword(id)
                        selectedEntry = null
                    },
                    onCopyToClipboard = copyToClipboard
                )
            }

            if (showAddSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showAddSheet = false },
                    sheetState = addSheetState
                ) {
                    AddPasswordContent(
                        onConfirm = { name, username, password, website, comments ->
                            viewModel.addPassword(
                                name = name,
                                username = username,
                                password = password,
                                website = website.ifBlank { null },
                                comments = comments.ifBlank { null }
                            )
                            scope.launch { addSheetState.hide() }.invokeOnCompletion {
                                if (!addSheetState.isVisible) showAddSheet = false
                            }
                        },
                        onCancel = {
                            scope.launch { addSheetState.hide() }.invokeOnCompletion {
                                if (!addSheetState.isVisible) showAddSheet = false
                            }
                        }
                    )
                }
            }
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
                    errorMessage = state.message
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
    errorMessage: String? = null
) {
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
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password"
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
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onConfirm(masterPasswordInput) },
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
