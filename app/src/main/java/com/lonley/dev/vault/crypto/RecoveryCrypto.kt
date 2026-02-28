package com.lonley.dev.vault.crypto

import com.lambdapioneer.argon2kt.Argon2Kt
import com.lambdapioneer.argon2kt.Argon2Mode
import com.lonley.dev.vault.util.VaultLogger
import java.nio.ByteBuffer
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

object RecoveryCrypto {

    private const val ARGON2_MEMORY_KB = 32768   // 32 MB (lower than vault's 64 MB — mnemonic has 128-bit entropy)
    private const val ARGON2_ITERATIONS = 3
    private const val ARGON2_PARALLELISM = 1
    private const val KEY_LENGTH_BYTES = 32       // 256-bit key
    private const val SALT_LENGTH = 32
    private const val GCM_IV_LENGTH = 12
    private const val GCM_TAG_BITS = 128

    /**
     * Generate a BIP39-compliant 12-word mnemonic.
     * 128 random bits → SHA-256 checksum (first 4 bits) → 132 bits → 12 groups of 11 bits → wordlist indices.
     */
    fun generateMnemonic(): List<String> {
        val entropy = ByteArray(16) // 128 bits
        SecureRandom().nextBytes(entropy)

        val hash = MessageDigest.getInstance("SHA-256").digest(entropy)
        val checksumBits = (hash[0].toInt() and 0xFF) ushr 4 // first 4 bits

        // Build 132-bit array: 128 bits of entropy + 4 bits of checksum
        val bits = BooleanArray(132)
        for (i in 0 until 128) {
            bits[i] = (entropy[i / 8].toInt() ushr (7 - i % 8)) and 1 == 1
        }
        for (i in 0 until 4) {
            bits[128 + i] = (checksumBits ushr (3 - i)) and 1 == 1
        }

        val wordlist = Bip39Wordlist.words
        val words = mutableListOf<String>()
        for (w in 0 until 12) {
            var index = 0
            for (b in 0 until 11) {
                if (bits[w * 11 + b]) {
                    index = index or (1 shl (10 - b))
                }
            }
            words.add(wordlist[index])
        }

        VaultLogger.i("RecoveryCrypto", "Generated 12-word mnemonic")
        return words
    }

    /**
     * Validate that a mnemonic has 12 valid words and a correct checksum.
     */
    fun validateMnemonic(words: List<String>): Boolean {
        if (words.size != 12) return false
        val wordlist = Bip39Wordlist.words

        val indices = words.map { word ->
            val idx = wordlist.indexOf(word.lowercase().trim())
            if (idx < 0) return false
            idx
        }

        // Reconstruct 132 bits
        val bits = BooleanArray(132)
        for (w in 0 until 12) {
            for (b in 0 until 11) {
                bits[w * 11 + b] = (indices[w] ushr (10 - b)) and 1 == 1
            }
        }

        // Extract entropy (128 bits) and checksum (4 bits)
        val entropy = ByteArray(16)
        for (i in 0 until 128) {
            if (bits[i]) {
                entropy[i / 8] = (entropy[i / 8].toInt() or (1 shl (7 - i % 8))).toByte()
            }
        }

        var checksum = 0
        for (i in 0 until 4) {
            if (bits[128 + i]) {
                checksum = checksum or (1 shl (3 - i))
            }
        }

        val hash = MessageDigest.getInstance("SHA-256").digest(entropy)
        val expectedChecksum = (hash[0].toInt() and 0xFF) ushr 4

        return checksum == expectedChecksum
    }

    /**
     * Create a recovery blob by encrypting the master password with a key derived from the mnemonic.
     * Output format: [salt(32)|iv(12)|ciphertext]
     */
    fun createRecoveryBlob(masterPassword: CharArray, mnemonic: List<String>): ByteArray {
        val secureRandom = SecureRandom()
        val salt = ByteArray(SALT_LENGTH).also { secureRandom.nextBytes(it) }
        val iv = ByteArray(GCM_IV_LENGTH).also { secureRandom.nextBytes(it) }

        val mnemonicString = mnemonic.joinToString(" ")
        val derivedKey = deriveKeyFromMnemonic(mnemonicString, salt)

        val passwordBytes = String(masterPassword).toByteArray(Charsets.UTF_8)
        try {
            val keySpec = SecretKeySpec(derivedKey, "AES")
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, GCMParameterSpec(GCM_TAG_BITS, iv))
            val ciphertext = cipher.doFinal(passwordBytes)

            val blob = ByteBuffer.allocate(SALT_LENGTH + GCM_IV_LENGTH + ciphertext.size).apply {
                put(salt)
                put(iv)
                put(ciphertext)
            }.array()

            VaultLogger.i("RecoveryCrypto", "Recovery blob created: ${blob.size} bytes")
            return blob
        } finally {
            derivedKey.fill(0)
            passwordBytes.fill(0)
        }
    }

    /**
     * Decrypt a recovery blob using the mnemonic to recover the master password.
     */
    fun recoverMasterPassword(blob: ByteArray, mnemonic: List<String>): CharArray {
        val buffer = ByteBuffer.wrap(blob)

        val salt = ByteArray(SALT_LENGTH).also { buffer.get(it) }
        val iv = ByteArray(GCM_IV_LENGTH).also { buffer.get(it) }
        val ciphertext = ByteArray(buffer.remaining()).also { buffer.get(it) }

        val mnemonicString = mnemonic.joinToString(" ")
        val derivedKey = deriveKeyFromMnemonic(mnemonicString, salt)

        try {
            val keySpec = SecretKeySpec(derivedKey, "AES")
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, keySpec, GCMParameterSpec(GCM_TAG_BITS, iv))
            val passwordBytes = cipher.doFinal(ciphertext)

            val password = String(passwordBytes, Charsets.UTF_8).toCharArray()
            passwordBytes.fill(0)
            VaultLogger.i("RecoveryCrypto", "Master password recovered successfully")
            return password
        } finally {
            derivedKey.fill(0)
        }
    }

    private fun deriveKeyFromMnemonic(mnemonic: String, salt: ByteArray): ByteArray {
        val mnemonicBytes = mnemonic.toByteArray(Charsets.UTF_8)
        try {
            val argon2 = Argon2Kt()
            val result = argon2.hash(
                mode = Argon2Mode.ARGON2_ID,
                password = mnemonicBytes,
                salt = salt,
                tCostInIterations = ARGON2_ITERATIONS,
                mCostInKibibyte = ARGON2_MEMORY_KB,
                parallelism = ARGON2_PARALLELISM,
                hashLengthInBytes = KEY_LENGTH_BYTES
            )
            return result.rawHashAsByteArray()
        } finally {
            mnemonicBytes.fill(0)
        }
    }
}
