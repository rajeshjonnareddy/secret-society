package com.lonley.dev.vault

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultApp() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val viewModel: VaultViewModel = viewModel()

    NavHost(navController = navController, startDestination = Routes.HOME) {
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
    }
}

@Preview(showBackground = true)
@Composable
fun VaultAppPreview() {
    VaultApp()
}
