package com.lonley.dev.vault

import android.util.Log
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
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lonley.dev.vault.crypto.VaultCrypto
import com.lonley.dev.vault.util.VaultLogger
import com.lonley.dev.vault.viewmodel.VaultViewModel
import com.lonley.dev.vault.views.AddPasswordContent
import com.lonley.dev.vault.views.HomeScreen
import com.lonley.dev.vault.views.PasswordEntry
import com.lonley.dev.vault.views.VaultScreen
import org.json.JSONObject
import java.io.File
import java.util.UUID

object Routes {
    const val HOME = "home"
    const val SIGN_UP = "sign_up"
    const val UPLOAD_VAULT = "upload_vault"
    const val USER_VAULT = "user_vault"
}

sealed class InitializationState {
    data object Loading : InitializationState()
    data class DecisionMade(val destination: String) : InitializationState()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultApp() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val viewModel: VaultViewModel = viewModel()

    var initializationState by remember { mutableStateOf<InitializationState>(InitializationState.Loading) }
    var foundVaultFileName by remember { mutableStateOf("") }
// State for the password dialog
    var showPasswordDialog by remember { mutableStateOf(false) }
    var masterPasswordInput by remember { mutableStateOf("") }
    var authenticationResult by remember { mutableStateOf<Boolean?>(null) } // To track successful/failed auth

    // Coroutine scope for performing asynchronous checks and actions
    val coroutineScope = rememberCoroutineScope()

    // Effect to run the check when the composable first enters the composition
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            val filesDir = context.filesDir
            val vaultFiles = filesDir.listFiles { file ->
                file.name.endsWith(".vlt", ignoreCase = true) || file.name.endsWith(".vault", ignoreCase = true)
            }
            val existingVaultFileFound = vaultFiles?.isNotEmpty() == true

            if (existingVaultFileFound) {
                Log.d("AppNavigation", "Existing vault found. Preparing to ask for password.")
                // Instead of directly deciding the destination, we trigger the dialog
                foundVaultFileName = vaultFiles.first().name
                showPasswordDialog = true
                // IMPORTANT: We don't set initializationState yet. We wait for user input.
            } else {
                Log.d("AppNavigation", "No existing vault found. Navigating to HOME.")
                initializationState = InitializationState.DecisionMade(Routes.HOME)
            }
        }
    }

    // --- Password Dialog ---
    if (showPasswordDialog) {
        ExpressivePasswordDialog(
            foundVaultFileName = foundVaultFileName,
            onDismiss = {
                showPasswordDialog = false
                // Handle cancellation logic (e.g., navigate home)
                initializationState = InitializationState.DecisionMade(Routes.HOME)
            },
            onConfirm = { password ->
                // Handle the password input
                if (password.isNotEmpty()) {
                    showPasswordDialog = false
                    // Proceed to unlock logic
                    initializationState = InitializationState.DecisionMade(Routes.USER_VAULT)
                }
            }
        )
    }

    // Display a loading indicator or placeholder while the start destination is being determined
    when (val state = initializationState) {
        InitializationState.Loading -> {
            // Show a loading screen while we determine the start destination
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        is InitializationState.DecisionMade -> {

    NavHost(navController = navController, startDestination = state.destination) {
        composable(Routes.HOME) {
            HomeScreen(
                createNewVault = { vaultName, username, email, masterPassword, encryptionType ->
                    VaultLogger.i("Navigation", "Creating new vault: name=$vaultName, user=$username, encryption=$encryptionType")

                    val json = JSONObject().apply {
                        put("vaultName", vaultName)
                        put("username", username)
                        put("email", email)
                        put("encryptionType", encryptionType)
                    }
                    val usersVaultFile = File(context.filesDir, ".$username.vlt")

                    try {
                        val encryptedBytes = VaultCrypto.encrypt(
                            plaintextBytes = json.toString().toByteArray(Charsets.UTF_8),
                            password = masterPassword,
                            algorithm = encryptionType
                        )
                        usersVaultFile.outputStream().use { it.write(encryptedBytes) }
                        VaultLogger.i("Navigation", "Vault file written: ${usersVaultFile.absolutePath} (${encryptedBytes.size} bytes)")

                        viewModel.initNewVault(usersVaultFile, masterPassword, json)
                        navController.navigate(Routes.USER_VAULT) {
                            popUpTo(Routes.HOME) { inclusive = true }
                        }
                        VaultLogger.d("Navigation", "Navigated to USER_VAULT")
                    } catch (e: IllegalArgumentException) {
                        VaultLogger.e("Navigation", "Vault creation failed (algorithm guard)", e)
                    } catch (e: Exception) {
                        VaultLogger.e("Navigation", "Vault creation failed", e)
                    }
                },
                onUploadClick = { /* TODO */ }
            )
        }
        composable(Routes.USER_VAULT) {
            val vaultName by viewModel.vaultName.collectAsState()
            val entries by viewModel.passwordEntries.collectAsState()
            var showAddSheet by remember { mutableStateOf(false) }
            val addSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            val scope = rememberCoroutineScope()

            VaultScreen(
                vaultName = vaultName,
                passwordEntries = entries,
                onAddPasswordClick = { showAddSheet = true },
                onBackClick = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.USER_VAULT) { inclusive = true }
                    }
                }
            )

            if (showAddSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showAddSheet = false },
                    sheetState = addSheetState
                ) {
                    AddPasswordContent(
                        onConfirm = { name, username, password, website ->
                            viewModel.addPassword(
                                PasswordEntry(
                                    id = UUID.randomUUID().toString(),
                                    name = name,
                                    username = username,
                                    password = password,
                                    website = website.ifBlank { null }
                                )
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
    }}}
}

@Preview(showBackground = true)
@Composable
fun VaultAppPreview() {
    VaultApp()
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpressivePasswordDialog(
    foundVaultFileName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var masterPasswordInput by remember { mutableStateOf("") }

    // 1. BasicAlertDialog for full control over expressive motion/layout
    BasicAlertDialog(onDismissRequest = onDismiss) {
        Surface(
            // 2. Use Material 3 Expressive shapes (ExtraLarge or custom morphs)
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp,
            modifier = Modifier.widthIn(max = 320.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 3. Emphasized Typography for the title
                Text(
                    text = "Unlock Vault",
                    style = MaterialTheme.typography.headlineMedium, // Emphasized role
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = foundVaultFileName.substring(1),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                OutlinedTextField(
                    value = masterPasswordInput,
                    onValueChange = { masterPasswordInput = it },
                    label = { Text("Master Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium // Consistent expressive rounding
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 4. ButtonGroup for expressive action layout
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
                        // Expressive buttons often use high-tonal contrast
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
