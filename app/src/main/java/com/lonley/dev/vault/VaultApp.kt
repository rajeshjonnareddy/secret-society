package com.lonley.dev.vault

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lonley.dev.vault.views.HomeScreen

object Routes {
    const val HOME = "home"
    const val SIGN_UP = "sign_up"
    const val UPLOAD_VAULT = "upload_vault"
}

@Composable
fun VaultApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                onVaultCreated = { vaultName, username, masterPassword, encryptionType ->
                    // TODO: Handle vault creation
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
