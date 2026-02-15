package com.lonley.dev.vault.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lonley.dev.vault.model.PasswordEntry
import com.lonley.dev.vault.model.SaveState
import com.lonley.dev.vault.model.VaultUiState
import com.lonley.dev.vault.repository.VaultRepository
import com.lonley.dev.vault.util.VaultLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File
import java.util.UUID

class VaultViewModel(private val repository: VaultRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<VaultUiState>(VaultUiState.Initializing)
    val uiState: StateFlow<VaultUiState> = _uiState.asStateFlow()

    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState: StateFlow<SaveState> = _saveState.asStateFlow()

    private var masterPassword: CharArray? = null
    private var vaultFile: File? = null
    private var encryptionType: String = "AES-256-GCM"
    private var vaultMetadata: JSONObject? = null
    private val entries = mutableListOf<PasswordEntry>()

    fun initialize() {
        viewModelScope.launch {
            try {
                val existingFile = repository.findExistingVault()
                if (existingFile != null) {
                    VaultLogger.i("ViewModel", "Found existing vault: ${existingFile.name}")
                    _uiState.value = VaultUiState.PromptUnlock(existingFile.name)
                } else {
                    VaultLogger.i("ViewModel", "No existing vault found")
                    _uiState.value = VaultUiState.Locked
                }
            } catch (e: Exception) {
                VaultLogger.e("ViewModel", "Initialization failed", e)
                _uiState.value = VaultUiState.Error("Failed to check for vaults", null)
            }
        }
    }
    fun unlockVault(password: CharArray) {
        viewModelScope.launch {
            val currentState = _uiState.value
            val fileName = when (currentState) {
                is VaultUiState.PromptUnlock -> currentState.fileName
                is VaultUiState.Error -> {
                    (currentState.previous as? VaultUiState.PromptUnlock)?.fileName
                }
                else -> null
            } ?: return@launch

            _uiState.value = VaultUiState.Loading("Decrypting vault...")
            try {
                val file = File(vaultFile?.parent ?: repository.findExistingVault()?.parent ?: return@launch, fileName)
                val metadata = repository.decryptVault(file, password)

                vaultFile = file
                masterPassword = password.copyOf()
                encryptionType = metadata.optString("encryptionType", "AES-256-GCM")
                vaultMetadata = metadata
                entries.clear()
                entries.addAll(repository.parseEntries(metadata))

                val vaultName = metadata.optString("vaultName", "Vault")
                VaultLogger.i("ViewModel", "Vault unlocked: $vaultName, ${entries.size} entries")
                _uiState.value = VaultUiState.Unlocked(vaultName, entries.toList())
            } catch (e: Exception) {
                VaultLogger.e("ViewModel", "Unlock failed", e)
                _uiState.value = VaultUiState.Error(
                    "Wrong password or corrupt vault",
                    VaultUiState.PromptUnlock(fileName)
                )
            }
        }
    }

    fun createVault(
        vaultName: String,
        username: String,
        email: String,
        password: CharArray,
        encryptionType: String
    ) {
        viewModelScope.launch {
            _uiState.value = VaultUiState.Loading("Creating vault...")
            try {
                val (file, metadata) = repository.createVault(
                    username, vaultName, email, password, encryptionType
                )
                vaultFile = file
                masterPassword = password.copyOf()
                this@VaultViewModel.encryptionType = encryptionType
                vaultMetadata = metadata
                entries.clear()

                VaultLogger.i("ViewModel", "Vault created: $vaultName")
                _uiState.value = VaultUiState.Unlocked(vaultName, emptyList())
            } catch (e: Exception) {
                VaultLogger.e("ViewModel", "Vault creation failed", e)
                _uiState.value = VaultUiState.Error("Failed to create vault: ${e.message}", VaultUiState.Locked)
            }
        }
    }

    fun addPassword(name: String, username: String, password: String, website: String?) {
        val entry = PasswordEntry(
            id = UUID.randomUUID().toString(),
            name = name,
            username = username,
            password = password,
            website = website
        )
        VaultLogger.i("ViewModel", "Adding password entry: name=${entry.name}, id=${entry.id}")
        entries.add(entry)

        val currentState = _uiState.value
        if (currentState is VaultUiState.Unlocked) {
            _uiState.value = currentState.copy(entries = entries.toList())
        }

        saveVaultAsync()
    }

    private fun saveVaultAsync() {
        val file = vaultFile ?: return
        val pw = masterPassword ?: return
        val algorithm = encryptionType
        val metadata = vaultMetadata ?: return
        val snapshot = entries.toList()

        viewModelScope.launch {
            _saveState.value = SaveState.Saving
            try {
                repository.saveVault(file, pw, algorithm, metadata, snapshot)
                _saveState.value = SaveState.SaveSuccess
            } catch (e: Exception) {
                VaultLogger.e("ViewModel", "Save failed", e)
                _saveState.value = SaveState.SaveError("Failed to save: ${e.message}")
            }
        }
    }

    fun dismissError() {
        val currentState = _uiState.value
        if (currentState is VaultUiState.Error) {
            _uiState.value = currentState.previous ?: VaultUiState.Locked
        }
    }

    fun resetSaveState() {
        _saveState.value = SaveState.Idle
    }

    fun lockVault() {
        masterPassword?.fill('\u0000')
        masterPassword = null
        vaultFile = null
        vaultMetadata = null
        entries.clear()
        _uiState.value = VaultUiState.Locked
        _saveState.value = SaveState.Idle
        VaultLogger.i("ViewModel", "Vault locked")
    }

    override fun onCleared() {
        super.onCleared()
        masterPassword?.fill('\u0000')
        masterPassword = null
    }

    class Factory(private val filesDir: File) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return VaultViewModel(VaultRepository(filesDir)) as T
        }
    }
}
