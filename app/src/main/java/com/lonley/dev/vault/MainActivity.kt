package com.lonley.dev.vault

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lonley.dev.vault.ui.theme.VaultTheme
import com.lonley.dev.vault.util.VaultLogger
import com.lonley.dev.vault.viewmodel.VaultViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        VaultLogger.init(applicationContext)
        VaultLogger.i("App", "MainActivity created")
        enableEdgeToEdge()
        setContent {
            val viewModel: VaultViewModel = viewModel(
                factory = VaultViewModel.Factory(filesDir)
            )
            val themeMode by viewModel.themeMode.collectAsState()

            VaultTheme(themeMode = themeMode) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color.Transparent
                ) { padding ->
                    Box(modifier = Modifier.padding(padding)) {
                        VaultApp(viewModel = viewModel)
                    }
                }
            }
        }
    }
}
