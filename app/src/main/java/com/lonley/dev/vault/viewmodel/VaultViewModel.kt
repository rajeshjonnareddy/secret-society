package com.lonley.dev.vault.viewmodel

import androidx.lifecycle.ViewModel
import com.lonley.dev.vault.crypto.VaultCrypto
import com.lonley.dev.vault.util.VaultLogger
import com.lonley.dev.vault.views.PasswordEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class VaultViewModel : ViewModel() {

    private val _passwordEntries = MutableStateFlow<List<PasswordEntry>>(emptyList())
    val passwordEntries: StateFlow<List<PasswordEntry>> = _passwordEntries.asStateFlow()

    private val _vaultName = MutableStateFlow("")
    val vaultName: StateFlow<String> = _vaultName.asStateFlow()

    private var vaultFile: File? = null
    private var masterPassword: String? = null
    private var encryptionType: String? = null
    private var vaultMetadata: JSONObject? = null

    fun initNewVault(file: File, password: String, metadata: JSONObject) {
        VaultLogger.i("ViewModel", "Initializing new vault session: file=${file.name}")
        vaultFile = file
        masterPassword = password
        encryptionType = metadata.optString("encryptionType", "AES-256-GCM")
        vaultMetadata = metadata.apply {
            if (!has("passwords")) put("passwords", JSONArray())
        }
        _vaultName.value = metadata.optString("vaultName", "Vault")
        _passwordEntries.value = emptyList()
        VaultLogger.d("ViewModel", "Vault session ready: name=${_vaultName.value}, algorithm=$encryptionType")
    }

    fun addPassword(entry: PasswordEntry) {
        VaultLogger.i("ViewModel", "Adding password entry: name=${entry.name}, id=${entry.id}")
        _passwordEntries.value = _passwordEntries.value + entry
        saveVault()
    }

    private fun saveVault() {
        val file = vaultFile ?: run {
            VaultLogger.w("ViewModel", "saveVault aborted: no vault file set")
            return
        }
        val password = masterPassword ?: run {
            VaultLogger.w("ViewModel", "saveVault aborted: no master password set")
            return
        }
        val algorithm = encryptionType ?: run {
            VaultLogger.w("ViewModel", "saveVault aborted: no encryption type set")
            return
        }
        val metadata = vaultMetadata ?: run {
            VaultLogger.w("ViewModel", "saveVault aborted: no vault metadata set")
            return
        }

        val passwordsArray = JSONArray()
        for (entry in _passwordEntries.value) {
            passwordsArray.put(JSONObject().apply {
                put("id", entry.id)
                put("name", entry.name)
                put("username", entry.username)
                put("password", entry.password)
                put("website", entry.website ?: JSONObject.NULL)
                put("createdAt", entry.createdAt)
            })
        }
        metadata.put("passwords", passwordsArray)

        try {
            val plaintext = metadata.toString().toByteArray(Charsets.UTF_8)
            val encrypted = VaultCrypto.encrypt(plaintext, password, algorithm)
            file.outputStream().use { it.write(encrypted) }
            VaultLogger.i("ViewModel", "Vault saved: ${_passwordEntries.value.size} entries, ${encrypted.size} bytes written")
        } catch (e: Exception) {
            VaultLogger.e("ViewModel", "Failed to save vault", e)
        }
    }
}
