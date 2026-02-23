package com.lonley.dev.vault.viewmodel

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lonley.dev.vault.data.SettingsPreferences
import com.lonley.dev.vault.model.AccentColor
import com.lonley.dev.vault.model.FontScale
import com.lonley.dev.vault.model.PasswordEntry
import com.lonley.dev.vault.model.PlanType
import com.lonley.dev.vault.model.SaveState
import com.lonley.dev.vault.model.SettingsState
import com.lonley.dev.vault.model.VaultUiState
import com.lonley.dev.vault.model.nextRenewalDate
import com.lonley.dev.vault.repository.VaultRepository
import com.lonley.dev.vault.ui.theme.ThemeMode
import com.lonley.dev.vault.util.VaultLogger
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONObject
import android.content.SharedPreferences
import kotlinx.coroutines.flow.SharingStarted
import java.io.File
import java.util.UUID

class VaultViewModel(
    private val repository: VaultRepository,
    private val prefs: SharedPreferences,
    private val settingsPreferences: SettingsPreferences,
    private val reminderPrefs: SharedPreferences
) : ViewModel(), DefaultLifecycleObserver {

    private var wasBackgrounded = false
    private var autoLockJob: Job? = null

    override fun onStop(owner: LifecycleOwner) {
        if (_uiState.value is VaultUiState.Unlocked) {
            wasBackgrounded = true
            VaultLogger.i("ViewModel", "App backgrounded while vault unlocked")
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        if (wasBackgrounded && _uiState.value is VaultUiState.Unlocked) {
            VaultLogger.i("ViewModel", "App foregrounded — auto-locking vault")
            lockVault()
        }
        wasBackgrounded = false
    }

    private fun startAutoLockTimer() {
        autoLockJob?.cancel()
        autoLockJob = viewModelScope.launch {
            delay(60_000L)
            if (_uiState.value is VaultUiState.Unlocked) {
                VaultLogger.i("ViewModel", "Inactivity timeout — suspending vault")
                suspendVault()
            }
        }
    }

    fun resetAutoLockTimer() {
        if (_uiState.value is VaultUiState.Unlocked) {
            startAutoLockTimer()
        }
    }

    private val _isSuspended = MutableStateFlow(false)
    val isSuspended: StateFlow<Boolean> = _isSuspended.asStateFlow()

    private val _suspendError = MutableStateFlow<String?>(null)
    val suspendError: StateFlow<String?> = _suspendError.asStateFlow()

    fun suspendVault() {
        autoLockJob?.cancel()
        _suspendError.value = null
        _isSuspended.value = true
    }

    fun verifyMasterPassword(password: CharArray): Boolean {
        val stored = masterPassword ?: return false
        val fileName = vaultFile?.name ?: return false
        val matches = password.contentEquals(stored)
        password.fill('\u0000')
        if (matches) {
            clearFailedAttempts(fileName)
            _suspendError.value = null
            _isSuspended.value = false
            startAutoLockTimer()
        } else {
            val attempts = getFailedAttempts(fileName) + 1
            setFailedAttempts(fileName, attempts)
            val remaining = MAX_ATTEMPTS - attempts

            if (remaining <= 0) {
                VaultLogger.w("ViewModel", "Max suspend attempts reached — deleting vault: $fileName")
                _isSuspended.value = false
                viewModelScope.launch {
                    repository.deleteAllVaultFiles()
                    clearFailedAttempts(fileName)
                    masterPassword?.fill('\u0000')
                    masterPassword = null
                    vaultFile = null
                    vaultMetadata = null
                    entries.clear()
                    _uiState.value = VaultUiState.Error(
                        "All 5 attempts used. Vault has been permanently deleted.",
                        VaultUiState.Locked
                    )
                }
            } else {
                _suspendError.value = "Wrong password.\n$remaining ${if (remaining == 1) "try" else "tries"} remaining before vault is deleted."
            }
        }
        return matches
    }

    private val _uiState = MutableStateFlow<VaultUiState>(VaultUiState.Initializing)
    val uiState: StateFlow<VaultUiState> = _uiState.asStateFlow()

    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState: StateFlow<SaveState> = _saveState.asStateFlow()

    private val _settingsState = MutableStateFlow(settingsPreferences.load())
    val settingsState: StateFlow<SettingsState> = _settingsState.asStateFlow()

    val themeMode: StateFlow<ThemeMode> = _settingsState
        .map { it.themeMode }
        .stateIn(viewModelScope, SharingStarted.Eagerly, _settingsState.value.themeMode)

    val accentColor: StateFlow<AccentColor> = _settingsState
        .map { it.accentColor }
        .stateIn(viewModelScope, SharingStarted.Eagerly, _settingsState.value.accentColor)

    fun setThemeMode(mode: ThemeMode) {
        updateSettings { it.copy(themeMode = mode) }
    }

    fun setAccentColor(color: AccentColor) {
        updateSettings { it.copy(accentColor = color) }
    }

    fun setHapticsEnabled(enabled: Boolean) {
        updateSettings { it.copy(hapticsEnabled = enabled) }
    }

    fun setScrollVibration(screen: String, enabled: Boolean) {
        updateSettings { current ->
            current.copy(scrollVibrations = current.scrollVibrations + (screen to enabled))
        }
    }

    fun setFontScale(scale: FontScale) {
        updateSettings { it.copy(fontScale = scale) }
    }

    private fun updateSettings(transform: (SettingsState) -> SettingsState) {
        val updated = transform(_settingsState.value)
        _settingsState.value = updated
        settingsPreferences.save(updated)
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
    private var lastUpdatedAt: Long = 0L

    companion object {
        private const val MAX_ATTEMPTS = 5
        private const val PREF_KEY_PREFIX = "failed_attempts_"
    }

    private fun attemptsKey(fileName: String): String = "$PREF_KEY_PREFIX$fileName"

    private fun getFailedAttempts(fileName: String): Int =
        prefs.getInt(attemptsKey(fileName), 0)

    private fun setFailedAttempts(fileName: String, count: Int) {
        prefs.edit().putInt(attemptsKey(fileName), count).apply()
    }

    private fun clearFailedAttempts(fileName: String) {
        prefs.edit().remove(attemptsKey(fileName)).apply()
    }

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
                val file = vaultFile
                    ?: repository.findVaultByName(fileName)
                    ?: return@launch
                val metadata = repository.decryptVault(file, password)

                vaultFile = file
                masterPassword = password.copyOf()
                encryptionType = metadata.optString("encryptionType", "AES-256-GCM")
                vaultMetadata = metadata
                entries.clear()
                entries.addAll(repository.parseEntries(metadata))

                val vaultName = metadata.optString("vaultName", "Vault")
                VaultLogger.i("ViewModel", "Vault unlocked: $vaultName, ${entries.size} entries")
                clearFailedAttempts(fileName)
                lastUpdatedAt = file.lastModified()
                _uiState.value = VaultUiState.Unlocked(
                    vaultName = vaultName,
                    entries = entries.toList(),
                    username = metadata.optString("username", ""),
                    email = metadata.optString("email", ""),
                    encryptionType = encryptionType,
                    lastUpdatedAt = lastUpdatedAt
                )
                startAutoLockTimer()
            } catch (e: Exception) {
                VaultLogger.e("ViewModel", "Unlock failed", e)
                val attempts = getFailedAttempts(fileName) + 1
                setFailedAttempts(fileName, attempts)
                val remaining = MAX_ATTEMPTS - attempts

                if (remaining <= 0) {
                    VaultLogger.w("ViewModel", "Max attempts reached — deleting vault: $fileName")
                    repository.deleteAllVaultFiles()
                    clearFailedAttempts(fileName)
                    vaultFile = null
                    _uiState.value = VaultUiState.Error(
                        "All 5 attempts used. Vault has been permanently deleted.",
                        VaultUiState.Locked
                    )
                } else {
                    _uiState.value = VaultUiState.Error(
                        "Wrong password.\n$remaining ${if (remaining == 1) "try" else "tries"} remaining before vault is deleted.",
                        VaultUiState.PromptUnlock(fileName),
                        attemptsRemaining = remaining
                    )
                }
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
                startAutoLockTimer()
            } catch (e: Exception) {
                VaultLogger.e("ViewModel", "Vault creation failed", e)
                _uiState.value = VaultUiState.Error("Failed to create vault: ${e.message}", VaultUiState.Locked)
            }
        }
    }

    fun addPassword(
        name: String,
        username: String?,
        password: String,
        website: String?,
        comments: String? = null,
        email: String? = null,
        isFavorite: Boolean = false,
        isSubscription: Boolean = false,
        planType: PlanType? = null,
        price: String? = null,
        subscriptionEmail: String? = null,
        startDate: Long? = null,
        reminderEnabled: Boolean = false
    ) {
        val entry = PasswordEntry(
            id = UUID.randomUUID().toString(),
            name = name,
            username = username,
            email = email,
            password = password,
            website = website,
            comments = comments,
            isFavorite = isFavorite,
            isSubscription = isSubscription,
            planType = planType,
            price = price,
            subscriptionEmail = subscriptionEmail,
            startDate = startDate,
            reminderEnabled = reminderEnabled
        )
        VaultLogger.i("ViewModel", "Adding password entry: name=${entry.name}, id=${entry.id}")
        entries.add(entry)
        lastUpdatedAt = System.currentTimeMillis()

        val currentState = _uiState.value
        if (currentState is VaultUiState.Unlocked) {
            _uiState.value = currentState.copy(entries = entries.toList(), lastUpdatedAt = lastUpdatedAt)
        }

        saveVaultAsync()
        syncReminders()
    }

    fun updatePassword(
        id: String,
        name: String,
        username: String?,
        password: String,
        website: String?,
        comments: String? = null,
        email: String? = null,
        isFavorite: Boolean = false,
        isSubscription: Boolean = false,
        planType: PlanType? = null,
        price: String? = null,
        subscriptionEmail: String? = null,
        startDate: Long? = null,
        reminderEnabled: Boolean = false
    ) {
        val index = entries.indexOfFirst { it.id == id }
        if (index == -1) return
        VaultLogger.i("ViewModel", "Updating password entry: name=$name, id=$id")
        entries[index] = entries[index].copy(
            name = name,
            username = username,
            email = email,
            password = password,
            website = website,
            comments = comments,
            isFavorite = isFavorite,
            isSubscription = isSubscription,
            planType = planType,
            price = price,
            subscriptionEmail = subscriptionEmail,
            startDate = startDate,
            reminderEnabled = reminderEnabled
        )
        lastUpdatedAt = System.currentTimeMillis()
        val currentState = _uiState.value
        if (currentState is VaultUiState.Unlocked) {
            _uiState.value = currentState.copy(entries = entries.toList(), lastUpdatedAt = lastUpdatedAt)
        }
        saveVaultAsync()
        syncReminders()
    }

    fun toggleFavorite(id: String) {
        val index = entries.indexOfFirst { it.id == id }
        if (index == -1) return
        entries[index] = entries[index].copy(isFavorite = !entries[index].isFavorite)
        lastUpdatedAt = System.currentTimeMillis()
        val currentState = _uiState.value
        if (currentState is VaultUiState.Unlocked) {
            _uiState.value = currentState.copy(entries = entries.toList(), lastUpdatedAt = lastUpdatedAt)
        }
        saveVaultAsync()
    }

    fun toggleReminder(id: String) {
        val index = entries.indexOfFirst { it.id == id }
        if (index == -1) return
        entries[index] = entries[index].copy(reminderEnabled = !entries[index].reminderEnabled)
        lastUpdatedAt = System.currentTimeMillis()
        val currentState = _uiState.value
        if (currentState is VaultUiState.Unlocked) {
            _uiState.value = currentState.copy(entries = entries.toList(), lastUpdatedAt = lastUpdatedAt)
        }
        saveVaultAsync()
        syncReminders()
    }

    private fun syncReminders() {
        val editor = reminderPrefs.edit()
        editor.clear()
        for (entry in entries) {
            if (entry.isSubscription && entry.reminderEnabled) {
                val nextRenewal = entry.nextRenewalDate() ?: continue
                editor.putString("${entry.id}_name", entry.name)
                editor.putLong("${entry.id}_nextRenewal", nextRenewal)
            }
        }
        editor.apply()
    }

    fun deletePassword(id: String) {
        val removed = entries.removeAll { it.id == id }
        if (!removed) return
        VaultLogger.i("ViewModel", "Deleted password entry: id=$id")
        lastUpdatedAt = System.currentTimeMillis()
        val currentState = _uiState.value
        if (currentState is VaultUiState.Unlocked) {
            _uiState.value = currentState.copy(entries = entries.toList(), lastUpdatedAt = lastUpdatedAt)
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
        autoLockJob?.cancel()
        autoLockJob = null
        wasBackgrounded = false
        _isSuspended.value = false
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
        _isSuspended.value = false
        _uiState.value = VaultUiState.Locked
        _saveState.value = SaveState.Idle
        VaultLogger.i("ViewModel", "No vault found.")
    }

    override fun onCleared() {
        super.onCleared()
        autoLockJob?.cancel()
        masterPassword?.fill('\u0000')
        masterPassword = null
    }

    class Factory(
        private val filesDir: File,
        private val prefs: SharedPreferences,
        private val settingsPreferences: SettingsPreferences,
        private val reminderPrefs: SharedPreferences
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return VaultViewModel(VaultRepository(filesDir), prefs, settingsPreferences, reminderPrefs) as T
        }
    }
}
