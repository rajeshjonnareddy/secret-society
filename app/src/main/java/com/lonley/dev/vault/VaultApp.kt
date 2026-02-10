package com.lonley.dev.vault

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
            HomeScreen()
        }
//        composable(Routes.SIGN_UP) {
//            SignUpScreen()
//        }
//        composable(Routes.UPLOAD_VAULT) {
//            UploadVaultScreen()
//        }
    }
}

@Preview(showBackground = true)
@Composable
fun VaultAppPreview() {
    VaultApp()
}