package com.lonley.dev.vault

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lonley.dev.vault.data.SettingsPreferences
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
            val prefs = getSharedPreferences("vault_security", Context.MODE_PRIVATE)
            val settingsPrefs = getSharedPreferences("vault_settings", Context.MODE_PRIVATE)
            val settingsPreferences = SettingsPreferences(settingsPrefs)
            val viewModel: VaultViewModel = viewModel(
                factory = VaultViewModel.Factory(filesDir, prefs, settingsPreferences)
            )
            DisposableEffect(viewModel) {
                val lifecycle = ProcessLifecycleOwner.get().lifecycle
                lifecycle.addObserver(viewModel)
                onDispose { lifecycle.removeObserver(viewModel) }
            }
            val themeMode by viewModel.themeMode.collectAsState()
            val accentColor by viewModel.accentColor.collectAsState()
            val settingsState by viewModel.settingsState.collectAsState()

            VaultTheme(themeMode = themeMode, accentColor = accentColor, fontScale = settingsState.fontScale.factor) {
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
