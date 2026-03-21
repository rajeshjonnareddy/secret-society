package com.lonley.dev.vault.viewmodel

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lonley.dev.vault.data.SettingsPreferences
import com.lonley.dev.vault.model.AccentColor
import com.lonley.dev.vault.model.AutoLockTimeout
import com.lonley.dev.vault.model.FontScale
import com.lonley.dev.vault.crypto.RecoveryCrypto
import com.lonley.dev.vault.model.EntryType
import com.lonley.dev.vault.model.PasswordEntry
import com.lonley.dev.vault.model.PlanType
import com.lonley.dev.vault.model.RecoveryState
import com.lonley.dev.vault.model.ChangePasswordState
import com.lonley.dev.vault.model.SaveState
import com.lonley.dev.vault.model.SettingsState
import com.lonley.dev.vault.model.SwipeAction
import com.lonley.dev.vault.model.VaultUiState
import com.lonley.dev.vault.model.nextRenewalDate
import com.lonley.dev.vault.notification.VaultNotificationHelper
import com.lonley.dev.vault.repository.VaultRepository
import com.lonley.dev.vault.ui.theme.ThemeMode
import com.lonley.dev.vault.util.VaultLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONObject
import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
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
            VaultLogger.i("ViewModel", "App foregrounded — suspending vault for re-auth")
            suspendVault()
        }
        wasBackgrounded = false
    }

    private fun startAutoLockTimer() {
        autoLockJob?.cancel()
        val timeout = _settingsState.value.autoLockTimeout
        if (timeout == AutoLockTimeout.Never || timeout == AutoLockTimeout.EveryUpdate) return
        autoLockJob = viewModelScope.launch {
            delay(timeout.millis)
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

    private val _hasRecovery = MutableStateFlow(false)
    val hasRecovery: StateFlow<Boolean> = _hasRecovery.asStateFlow()

    private val _recoveryState = MutableStateFlow<RecoveryState>(RecoveryState.None)
    val recoveryState: StateFlow<RecoveryState> = _recoveryState.asStateFlow()

    private val _changePasswordState = MutableStateFlow<ChangePasswordState>(ChangePasswordState.Idle)
    val changePasswordState: StateFlow<ChangePasswordState> = _changePasswordState.asStateFlow()

    private val _lastExportedAt = MutableStateFlow(prefs.getLong("last_exported_at", 0L))
    val lastExportedAt: StateFlow<Long> = _lastExportedAt.asStateFlow()

    fun recordExport() {
        val now = System.currentTimeMillis()
        _lastExportedAt.value = now
        prefs.edit().putLong("last_exported_at", now).apply()
        VaultLogger.i("ViewModel", "Vault exported at $now")
    }

    fun getUsername(): String {
        return (uiState.value as? VaultUiState.Unlocked)?.username ?: ""
    }

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

    fun setAutoLockTimeout(timeout: AutoLockTimeout) {
        updateSettings { it.copy(autoLockTimeout = timeout) }
        if (_uiState.value is VaultUiState.Unlocked) {
            startAutoLockTimer()
        }
    }

    fun setSwipeActionsEnabled(enabled: Boolean) {
        updateSettings { it.copy(swipeActionsEnabled = enabled) }
    }

    fun setSwipeLeftAction(action: SwipeAction) {
        updateSettings { it.copy(swipeLeftAction = action) }
    }

    fun setSwipeRightAction(action: SwipeAction) {
        updateSettings { it.copy(swipeRightAction = action) }
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

    fun exportToDownloads(context: Context, fileName: String) {
        val bytes = getVaultBytes()
        if (bytes == null) {
            VaultLogger.w("ViewModel", "No vault data to export")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            VaultNotificationHelper.showExportProgress(context, fileName)
            try {
                val uri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // API 29+ — use MediaStore
                    val contentValues = ContentValues().apply {
                        put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                        put(MediaStore.Downloads.MIME_TYPE, "application/octet-stream")
                        put(MediaStore.Downloads.IS_PENDING, 1)
                    }
                    val insertUri = context.contentResolver.insert(
                        MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues
                    ) ?: throw Exception("Failed to create file in Downloads")

                    context.contentResolver.openOutputStream(insertUri)?.use { it.write(bytes) }
                        ?: throw Exception("Failed to write to Downloads")

                    contentValues.clear()
                    contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
                    context.contentResolver.update(insertUri, contentValues, null, null)

                    insertUri
                } else {
                    // API 24-28 — use legacy external storage
                    @Suppress("DEPRECATION")
                    val downloadsDir = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOWNLOADS
                    )
                    downloadsDir.mkdirs()
                    val outFile = File(downloadsDir, fileName)
                    outFile.writeBytes(bytes)
                    Uri.fromFile(outFile)
                }

                recordExport()
                VaultNotificationHelper.showExportComplete(context, fileName, uri)
                VaultLogger.i("ViewModel", "Vault exported to Downloads: $fileName")
            } catch (e: Exception) {
                VaultLogger.e("ViewModel", "Export to Downloads failed", e)
                VaultNotificationHelper.showExportFailed(
                    context, fileName, e.message ?: "Unknown error"
                )
            }
        }
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
                    val username = existingFile.name.removePrefix(".").removeSuffix(".vlt")
                    _hasRecovery.value = username.isNotEmpty() && repository.hasRecoveryBlob(username)
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
                val username = metadata.optString("username", "")
                VaultLogger.i("ViewModel", "Vault unlocked: $vaultName, ${entries.size} entries")
                clearFailedAttempts(fileName)
                lastUpdatedAt = file.lastModified()
                _hasRecovery.value = username.isNotEmpty() && repository.hasRecoveryBlob(username)
                _uiState.value = VaultUiState.Unlocked(
                    vaultName = vaultName,
                    entries = entries.toList(),
                    username = username,
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
                    encryptionType = encryptionType,
                    justCreated = true
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
        reminderEnabled: Boolean = false,
        entryType: EntryType = EntryType.Password,
        phraseWordCount: Int? = null
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
            reminderEnabled = reminderEnabled,
            entryType = entryType,
            phraseWordCount = phraseWordCount
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
        reminderEnabled: Boolean = false,
        entryType: EntryType = EntryType.Password,
        phraseWordCount: Int? = null
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
            reminderEnabled = reminderEnabled,
            entryType = entryType,
            phraseWordCount = phraseWordCount
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
                if (_settingsState.value.autoLockTimeout == AutoLockTimeout.EveryUpdate) {
                    VaultLogger.i("ViewModel", "EveryUpdate auto-lock — suspending vault after save")
                    suspendVault()
                }
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
            _recoveryState.value = RecoveryState.None
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

    fun changeMasterPassword(oldPassword: CharArray, newPassword: CharArray) {
        viewModelScope.launch {
            try {
                val file = vaultFile ?: throw IllegalStateException("No vault file")

                // Verify old password by attempting decryption
                repository.decryptVault(file, oldPassword)

                // Re-encrypt with new password
                repository.saveVault(file, newPassword, encryptionType, vaultMetadata!!, entries.toList())

                // Update in-memory password
                masterPassword?.fill('\u0000')
                masterPassword = newPassword.copyOf()
                oldPassword.fill('\u0000')

                // Invalidate recovery blob (tied to old password)
                val username = (uiState.value as? VaultUiState.Unlocked)?.username ?: ""
                if (username.isNotEmpty()) {
                    repository.deleteRecoveryBlob(username)
                    _hasRecovery.value = false
                }

                _changePasswordState.value = ChangePasswordState.Success
                VaultLogger.i("ViewModel", "Master password changed successfully")
            } catch (e: Exception) {
                oldPassword.fill('\u0000')
                newPassword.fill('\u0000')
                VaultLogger.e("ViewModel", "Change password failed", e)
                _changePasswordState.value = ChangePasswordState.Error("Incorrect current password")
            }
        }
    }

    fun resetChangePasswordState() {
        _changePasswordState.value = ChangePasswordState.Idle
    }

    fun generateRecoveryPhrase() {
        val pw = masterPassword ?: return
        viewModelScope.launch {
            try {
                val mnemonic = RecoveryCrypto.generateMnemonic()
                val blob = RecoveryCrypto.createRecoveryBlob(pw, mnemonic)
                val username = (uiState.value as? VaultUiState.Unlocked)?.username ?: return@launch
                repository.saveRecoveryBlob(username, blob)
                _hasRecovery.value = true
                _recoveryState.value = RecoveryState.ShowPhrase(mnemonic)
                VaultLogger.i("ViewModel", "Recovery phrase generated for user: $username")
            } catch (e: Exception) {
                VaultLogger.e("ViewModel", "Failed to generate recovery phrase", e)
                _recoveryState.value = RecoveryState.Error("Failed to generate recovery phrase: ${e.message}")
            }
        }
    }

    fun confirmRecoveryPhraseSeen() {
        _recoveryState.value = RecoveryState.None
    }

    fun startRecoveryFlow() {
        _recoveryState.value = RecoveryState.PromptEntry
    }

    fun attemptRecovery(words: List<String>, newPassword: CharArray) {
        viewModelScope.launch {
            val currentState = _uiState.value
            val fileName = when (currentState) {
                is VaultUiState.PromptUnlock -> currentState.fileName
                is VaultUiState.Error -> {
                    (currentState.previous as? VaultUiState.PromptUnlock)?.fileName
                }
                else -> null
            } ?: return@launch

            try {
                // Extract username from filename: .username.vlt → username
                val username = fileName.removePrefix(".").removeSuffix(".vlt")
                val blob = repository.loadRecoveryBlob(username)
                    ?: throw IllegalStateException("No recovery data found")

                val recoveredPassword = RecoveryCrypto.recoverMasterPassword(blob, words)

                // Decrypt vault with recovered password
                val file = repository.findVaultByName(fileName)
                    ?: throw IllegalStateException("Vault file not found")
                val metadata = repository.decryptVault(file, recoveredPassword)

                // Re-encrypt vault with new password
                vaultFile = file
                encryptionType = metadata.optString("encryptionType", "AES-256-GCM")
                vaultMetadata = metadata
                entries.clear()
                entries.addAll(repository.parseEntries(metadata))

                // Save with new password
                repository.saveVault(file, newPassword, encryptionType, metadata, entries.toList())

                // Set new password as current
                masterPassword = newPassword.copyOf()

                // Delete old recovery blob (stale after password change)
                repository.deleteRecoveryBlob(username)
                _hasRecovery.value = false

                // Clear recovered password from memory
                recoveredPassword.fill('\u0000')

                val vaultName = metadata.optString("vaultName", "Vault")
                clearFailedAttempts(fileName)
                lastUpdatedAt = file.lastModified()
                _recoveryState.value = RecoveryState.None
                _uiState.value = VaultUiState.Unlocked(
                    vaultName = vaultName,
                    entries = entries.toList(),
                    username = metadata.optString("username", ""),
                    email = metadata.optString("email", ""),
                    encryptionType = encryptionType,
                    lastUpdatedAt = lastUpdatedAt
                )
                startAutoLockTimer()
                VaultLogger.i("ViewModel", "Recovery successful — vault unlocked with new password")
            } catch (e: Exception) {
                VaultLogger.e("ViewModel", "Recovery failed", e)
                _recoveryState.value = RecoveryState.Error(
                    "Recovery failed. Check your 12 words and try again."
                )
            }
        }
    }

    fun dismissRecoveryError() {
        _recoveryState.value = RecoveryState.PromptEntry
    }

    fun cancelRecovery() {
        _recoveryState.value = RecoveryState.None
    }

    fun clearJustCreated() {
        val currentState = _uiState.value
        if (currentState is VaultUiState.Unlocked && currentState.justCreated) {
            _uiState.value = currentState.copy(justCreated = false)
        }
    }

    fun hasRecoveryForVault(fileName: String): Boolean {
        val username = fileName.removePrefix(".").removeSuffix(".vlt")
        return username.isNotEmpty() && repository.hasRecoveryBlob(username)
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
