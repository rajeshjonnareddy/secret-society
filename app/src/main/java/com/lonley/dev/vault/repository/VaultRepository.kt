package com.lonley.dev.vault.repository

import com.lonley.dev.vault.crypto.VaultCrypto
import com.lonley.dev.vault.model.EntryType
import com.lonley.dev.vault.model.Network
import com.lonley.dev.vault.model.PasswordEntry
import com.lonley.dev.vault.model.PlanType
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

    suspend fun findVaultByName(fileName: String): File? = withContext(Dispatchers.IO) {
        val file = File(filesDir, fileName)
        if (file.exists()) file else findExistingVault()
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
                put("username", entry.username ?: JSONObject.NULL)
                put("email", entry.email ?: JSONObject.NULL)
                put("password", entry.password)
                put("website", entry.website ?: JSONObject.NULL)
                put("comments", entry.comments ?: JSONObject.NULL)
                put("createdAt", entry.createdAt)
                put("isFavorite", entry.isFavorite)
                put("isSubscription", entry.isSubscription)
                put("planType", entry.planType?.name ?: JSONObject.NULL)
                put("price", entry.price ?: JSONObject.NULL)
                put("subscriptionEmail", entry.subscriptionEmail ?: JSONObject.NULL)
                put("startDate", entry.startDate ?: JSONObject.NULL)
                put("reminderEnabled", entry.reminderEnabled)
                put("entryType", entry.entryType.name)
                put("phraseWordCount", entry.phraseWordCount ?: JSONObject.NULL)
                put("walletAddress", entry.walletAddress ?: JSONObject.NULL)
                put("seedPhrase", entry.seedPhrase ?: JSONObject.NULL)
                put("network", entry.network?.name ?: JSONObject.NULL)
                put("exchange", entry.exchange ?: JSONObject.NULL)
                put("tokenSymbol", entry.tokenSymbol ?: JSONObject.NULL)
                put("tokenAmount", entry.tokenAmount ?: JSONObject.NULL)
                put("tokenValueUsd", entry.tokenValueUsd ?: JSONObject.NULL)
                put("l2Network", entry.l2Network ?: JSONObject.NULL)
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

    suspend fun saveRecoveryBlob(username: String, blob: ByteArray) = withContext(Dispatchers.IO) {
        val file = File(filesDir, ".$username.recovery")
        file.outputStream().use { it.write(blob) }
        VaultLogger.i("Repository", "Recovery blob saved: ${file.name} (${blob.size} bytes)")
    }

    suspend fun loadRecoveryBlob(username: String): ByteArray? = withContext(Dispatchers.IO) {
        val file = File(filesDir, ".$username.recovery")
        if (file.exists()) file.readBytes() else null
    }

    fun hasRecoveryBlob(username: String): Boolean {
        return File(filesDir, ".$username.recovery").exists()
    }

    suspend fun deleteRecoveryBlob(username: String) = withContext(Dispatchers.IO) {
        val file = File(filesDir, ".$username.recovery")
        if (file.exists()) {
            val deleted = file.delete()
            VaultLogger.i("Repository", "Deleted recovery blob: ${file.name} (success=$deleted)")
        }
    }

    suspend fun deleteAllVaultFiles() = withContext(Dispatchers.IO) {
        val vaultFiles = filesDir.listFiles { file ->
            file.name.endsWith(".vlt", ignoreCase = true) ||
                file.name.endsWith(".recovery", ignoreCase = true)
        }
        vaultFiles?.forEach { file ->
            val deleted = file.delete()
            VaultLogger.i("Repository", "Deleted vault file: ${file.name} (success=$deleted)")
        }
    }

    fun parseEntries(metadata: JSONObject): List<PasswordEntry> {
        val passwordsArray = metadata.optJSONArray("passwords") ?: return emptyList()
        return (0 until passwordsArray.length()).map { i ->
            val obj = passwordsArray.getJSONObject(i)
            PasswordEntry(
                id = obj.getString("id"),
                name = obj.getString("name"),
                username = if (obj.isNull("username")) null else obj.optString("username").ifBlank { null },
                email = if (obj.isNull("email")) null else obj.optString("email").ifBlank { null },
                password = obj.getString("password"),
                website = if (obj.isNull("website")) null else obj.optString("website"),
                comments = if (obj.isNull("comments")) null else obj.optString("comments"),
                createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                isFavorite = obj.optBoolean("isFavorite", false),
                isSubscription = obj.optBoolean("isSubscription", false),
                planType = obj.optString("planType", "").let { name ->
                    PlanType.entries.firstOrNull { it.name == name }
                },
                price = if (obj.isNull("price")) null else obj.optString("price"),
                subscriptionEmail = if (obj.isNull("subscriptionEmail")) null else obj.optString("subscriptionEmail"),
                startDate = if (obj.isNull("startDate")) null else obj.optLong("startDate", 0L).takeIf { it > 0 },
                reminderEnabled = obj.optBoolean("reminderEnabled", false),
                entryType = obj.optString("entryType", "").let { name ->
                    val parsed = EntryType.entries.firstOrNull { it.name == name } ?: EntryType.Password
                    // Migrate old Passphrase entries to CryptoWallet
                    if (parsed == EntryType.Passphrase) EntryType.CryptoWallet else parsed
                },
                phraseWordCount = if (obj.isNull("phraseWordCount")) null else obj.optInt("phraseWordCount", 0).takeIf { it > 0 },
                walletAddress = if (obj.isNull("walletAddress")) null else obj.optString("walletAddress").ifBlank { null },
                seedPhrase = if (obj.isNull("seedPhrase")) null else obj.optString("seedPhrase").ifBlank { null },
                network = obj.optString("network", "").let { name ->
                    Network.entries.firstOrNull { it.name == name }
                },
                exchange = if (obj.isNull("exchange")) null else obj.optString("exchange").ifBlank { null },
                tokenSymbol = if (obj.isNull("tokenSymbol")) null else obj.optString("tokenSymbol").ifBlank { null },
                tokenAmount = if (obj.isNull("tokenAmount")) null else obj.optString("tokenAmount").ifBlank { null },
                tokenValueUsd = if (obj.isNull("tokenValueUsd")) null else obj.optString("tokenValueUsd").ifBlank { null },
                l2Network = if (obj.isNull("l2Network")) null else obj.optString("l2Network").ifBlank { null }
            )
        }
    }
}
