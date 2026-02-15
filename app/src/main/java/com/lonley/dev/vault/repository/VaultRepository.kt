package com.lonley.dev.vault.repository

import com.lonley.dev.vault.crypto.VaultCrypto
import com.lonley.dev.vault.model.PasswordEntry
import com.lonley.dev.vault.util.VaultLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class VaultRepository(private val filesDir: File) {

    suspend fun findExistingVault(): File? = withContext(Dispatchers.IO) {
        val vaultFiles = filesDir.listFiles { file ->
            file.name.endsWith(".vlt", ignoreCase = true)
        }
        vaultFiles?.firstOrNull()
    }

    suspend fun decryptVault(file: File, password: CharArray): JSONObject = withContext(Dispatchers.IO) {
        val passwordString = String(password)
        try {
            val encryptedBytes = file.readBytes()
            val decryptedBytes = VaultCrypto.decrypt(encryptedBytes, passwordString)
            JSONObject(String(decryptedBytes, Charsets.UTF_8))
        } finally {
            // Can't zero a String, but we limit its scope
        }
    }

    suspend fun createVault(
        username: String,
        vaultName: String,
        email: String,
        password: CharArray,
        encryptionType: String
    ): Pair<File, JSONObject> = withContext(Dispatchers.IO) {
        val metadata = JSONObject().apply {
            put("vaultName", vaultName)
            put("username", username)
            put("email", email)
            put("encryptionType", encryptionType)
            put("passwords", JSONArray())
        }

        val file = File(filesDir, ".$username.vlt")
        val passwordString = String(password)
        val encryptedBytes = VaultCrypto.encrypt(
            plaintextBytes = metadata.toString().toByteArray(Charsets.UTF_8),
            password = passwordString,
            algorithm = encryptionType
        )
        file.outputStream().use { it.write(encryptedBytes) }
        VaultLogger.i("Repository", "Vault file written: ${file.name} (${encryptedBytes.size} bytes)")

        Pair(file, metadata)
    }

    suspend fun saveVault(
        file: File,
        password: CharArray,
        algorithm: String,
        metadata: JSONObject,
        entries: List<PasswordEntry>
    ) = withContext(Dispatchers.IO) {
        val passwordsArray = JSONArray()
        for (entry in entries) {
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

        val passwordString = String(password)
        val plaintext = metadata.toString().toByteArray(Charsets.UTF_8)
        val encrypted = VaultCrypto.encrypt(plaintext, passwordString, algorithm)
        file.outputStream().use { it.write(encrypted) }
        VaultLogger.i("Repository", "Vault saved: ${entries.size} entries, ${encrypted.size} bytes")
    }

    suspend fun importVaultFile(bytes: ByteArray, fileName: String): File = withContext(Dispatchers.IO) {
        val safeName = fileName.ifBlank { "imported.vlt" }
            .let { if (it.endsWith(".vlt", ignoreCase = true)) it else "$it.vlt" }
        val dest = File(filesDir, safeName)
        dest.outputStream().use { it.write(bytes) }
        VaultLogger.i("Repository", "Imported vault file: ${dest.name} (${bytes.size} bytes)")
        dest
    }

    fun parseEntries(metadata: JSONObject): List<PasswordEntry> {
        val passwordsArray = metadata.optJSONArray("passwords") ?: return emptyList()
        return (0 until passwordsArray.length()).map { i ->
            val obj = passwordsArray.getJSONObject(i)
            PasswordEntry(
                id = obj.getString("id"),
                name = obj.getString("name"),
                username = obj.getString("username"),
                password = obj.getString("password"),
                website = if (obj.isNull("website")) null else obj.optString("website"),
                createdAt = obj.optLong("createdAt", System.currentTimeMillis())
            )
        }
    }
}
