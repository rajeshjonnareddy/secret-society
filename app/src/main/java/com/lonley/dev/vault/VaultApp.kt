package com.lonley.dev.vault

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lonley.dev.vault.views.HomeScreen
import org.json.JSONObject
import java.io.File

object Routes {
    const val HOME = "home"
    const val SIGN_UP = "sign_up"
    const val UPLOAD_VAULT = "upload_vault"
}

@Composable
fun VaultApp() {
    val navController = rememberNavController()
    val context = LocalContext.current

    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                createNewVault = { vaultName, username, email, masterPassword, encryptionType ->
                    // createNewVault lambda function implementation
                    val json = JSONObject().apply {
                        put("vaultName", vaultName)
                        put("username", username)
                        put("email", email)
                        put("encryptionType", encryptionType)
                    }
                    // TODO: call a private function to encrypt the data with proper schema.
                    val usersVaultFile = File(context.filesDir, ".$username")

                    try {
                        usersVaultFile.outputStream().use { outputStream ->
                            outputStream.write(json.toString().toByteArray())
                        }
                        // ... navigation ...
                        // TODO: Redirect to show vault screen with data from the vault.
                    } catch (e: Exception) {
                        // ... error handling ...
                        //TODO: Create an Error screen and Redirect and log
                    }
                },
                onUploadClick = { /* TODO */ }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun VaultAppPreview() {
    VaultApp()
}
