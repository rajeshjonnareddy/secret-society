package com.lonley.dev.vault.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lonley.dev.vault.model.PasswordEntry
import com.lonley.dev.vault.model.SaveState
import com.lonley.dev.vault.model.VaultUiState
import com.lonley.dev.vault.repository.VaultRepository
import com.lonley.dev.vault.ui.theme.ThemeMode
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

    private val _themeMode = MutableStateFlow(ThemeMode.System)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
    }

    fun getVaultBytes(): ByteArray? {
        return vaultFile?.takeIf { it.exists() }?.readBytes()
    }

    fun getVaultFileName(): String {
        val name = vaultFile?.name ?: "vault.vlt"
        return if (name.startsWith(".")) name.substring(1) else name
    }

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
    fun openExistingVault() {
        viewModelScope.launch {
            try {
                val existingFile = repository.findExistingVault()
                if (existingFile != null) {
                    VaultLogger.i("ViewModel", "Open existing: found ${existingFile.name}")
                    _uiState.value = VaultUiState.PromptUnlock(existingFile.name)
                } else {
                    VaultLogger.i("ViewModel", "Open existing: no local vault, launching picker")
                    _uiState.value = VaultUiState.PickFile
                }
            } catch (e: Exception) {
                VaultLogger.e("ViewModel", "openExistingVault failed", e)
                _uiState.value = VaultUiState.Error("Failed to check for vaults", VaultUiState.Locked)
            }
        }
    }

    fun importVault(bytes: ByteArray, fileName: String) {
        viewModelScope.launch {
            try {
                val file = repository.importVaultFile(bytes, fileName)
                VaultLogger.i("ViewModel", "Imported vault: ${file.name}")
                _uiState.value = VaultUiState.PromptUnlock(file.name)
            } catch (e: Exception) {
                VaultLogger.e("ViewModel", "Import failed", e)
                _uiState.value = VaultUiState.Error("Failed to import vault: ${e.message}", VaultUiState.Locked)
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

            val displayName = fileName
                .removePrefix(".")
                .removeSuffix(".vlt")
                .ifEmpty { "Vault" }
            _uiState.value = VaultUiState.Unlocked(displayName, emptyList(), isLoading = true)
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
                _uiState.value = VaultUiState.Unlocked(
                    vaultName = vaultName,
                    entries = entries.toList(),
                    username = metadata.optString("username", ""),
                    email = metadata.optString("email", ""),
                    encryptionType = encryptionType
                )
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
                _uiState.value = VaultUiState.Unlocked(
                    vaultName = vaultName,
                    entries = emptyList(),
                    username = username,
                    email = email,
                    encryptionType = encryptionType
                )
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

    fun updatePassword(id: String, name: String, username: String, password: String, website: String?) {
        val index = entries.indexOfFirst { it.id == id }
        if (index == -1) return
        VaultLogger.i("ViewModel", "Updating password entry: name=$name, id=$id")
        entries[index] = entries[index].copy(
            name = name,
            username = username,
            password = password,
            website = website
        )
        val currentState = _uiState.value
        if (currentState is VaultUiState.Unlocked) {
            _uiState.value = currentState.copy(entries = entries.toList())
        }
        saveVaultAsync()
    }

    fun deletePassword(id: String) {
        val removed = entries.removeAll { it.id == id }
        if (!removed) return
        VaultLogger.i("ViewModel", "Deleted password entry: id=$id")
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
        viewModelScope.launch {
            // Save the vault before locking so all data is encrypted to disk
            val file = vaultFile
            val pw = masterPassword
            val algorithm = encryptionType
            val metadata = vaultMetadata
            if (file != null && pw != null && metadata != null) {
                try {
                    repository.saveVault(file, pw, algorithm, metadata, entries.toList())
                    VaultLogger.i("ViewModel", "Vault saved before lock")
                } catch (e: Exception) {
                    VaultLogger.e("ViewModel", "Save before lock failed", e)
                }
            }

            // Clear in-memory state
            masterPassword?.fill('\u0000')
            masterPassword = null
            vaultFile = null
            vaultMetadata = null
            entries.clear()
            _uiState.value = VaultUiState.Locked
            _saveState.value = SaveState.Idle
            VaultLogger.i("ViewModel", "Vault locked and local storage cleared")
        }
    }

    fun noVault() {
        masterPassword?.fill('\u0000')
        masterPassword = null
        vaultFile = null
        vaultMetadata = null
        entries.clear()
        _uiState.value = VaultUiState.Locked
        _saveState.value = SaveState.Idle
        VaultLogger.i("ViewModel", "No vault found.")
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
