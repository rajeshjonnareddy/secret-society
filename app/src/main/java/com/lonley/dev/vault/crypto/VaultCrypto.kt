package com.lonley.dev.vault.crypto

import android.os.Build
import androidx.annotation.RequiresApi
import com.lambdapioneer.argon2kt.Argon2Kt
import com.lambdapioneer.argon2kt.Argon2Mode
import com.lonley.dev.vault.util.VaultLogger
import java.nio.ByteBuffer
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object VaultCrypto {

    private const val VERSION: Byte = 0x01
    private const val ARGON2_MEMORY_KB = 65536    // 64 MB
    private const val ARGON2_ITERATIONS = 3
    private const val ARGON2_PARALLELISM = 1
    private const val KEY_LENGTH_BYTES = 32        // 256-bit key
    private const val SALT_LENGTH = 32

    private const val GCM_IV_LENGTH = 12
    private const val CBC_IV_LENGTH = 16
    private const val CHACHA20_NONCE_LENGTH = 12
    private const val GCM_TAG_BITS = 128

    private const val ALGORITHM_AES_GCM: Byte = 0x00
    private const val ALGORITHM_AES_CBC: Byte = 0x01
    private const val ALGORITHM_CHACHA20: Byte = 0x02

    fun encrypt(plaintextBytes: ByteArray, password: String, algorithm: String): ByteArray {
        VaultLogger.d("Crypto", "Encrypting ${plaintextBytes.size} bytes with $algorithm")
        val algorithmId = when (algorithm) {
            "AES-256-GCM" -> ALGORITHM_AES_GCM
            "AES-256-CBC" -> ALGORITHM_AES_CBC
            "ChaCha20-Poly1305" -> {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                    throw IllegalArgumentException(
                        "ChaCha20-Poly1305 requires API 28+ (current: ${Build.VERSION.SDK_INT})"
                    )
                }
                ALGORITHM_CHACHA20
            }
            else -> throw IllegalArgumentException("Unknown algorithm: $algorithm")
        }

        val secureRandom = SecureRandom()

        // Generate salt and derive key via Argon2id
        val salt = ByteArray(SALT_LENGTH).also { secureRandom.nextBytes(it) }
        val derivedKey = deriveKey(password, salt)

        // Encrypt based on algorithm
        val iv: ByteArray
        val ciphertext: ByteArray

        when (algorithmId) {
            ALGORITHM_AES_GCM -> {
                iv = ByteArray(GCM_IV_LENGTH).also { secureRandom.nextBytes(it) }
                val keySpec = SecretKeySpec(derivedKey, "AES")
                val cipher = Cipher.getInstance("AES/GCM/NoPadding")
                cipher.init(Cipher.ENCRYPT_MODE, keySpec, GCMParameterSpec(GCM_TAG_BITS, iv))
                ciphertext = cipher.doFinal(plaintextBytes)
            }
            ALGORITHM_AES_CBC -> {
                iv = ByteArray(CBC_IV_LENGTH).also { secureRandom.nextBytes(it) }
                val keySpec = SecretKeySpec(derivedKey, "AES")
                val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
                cipher.init(Cipher.ENCRYPT_MODE, keySpec, IvParameterSpec(iv))
                ciphertext = cipher.doFinal(plaintextBytes)
            }
            ALGORITHM_CHACHA20 -> {
                iv = ByteArray(CHACHA20_NONCE_LENGTH).also { secureRandom.nextBytes(it) }
                ciphertext = encryptChaCha20(derivedKey, iv, plaintextBytes)
            }
            else -> throw IllegalStateException("Unreachable")
        }

        // Clear derived key from memory
        derivedKey.fill(0)
        VaultLogger.d("Crypto", "Encryption complete: ciphertext=${ciphertext.size} bytes, iv=${iv.size} bytes")

        // Assemble binary output
        val totalSize = 1 + 1 + 4 + 4 + 1 + 1 + salt.size + 1 + iv.size + ciphertext.size
        return ByteBuffer.allocate(totalSize).apply {
            put(VERSION)
            put(algorithmId)
            putInt(ARGON2_MEMORY_KB)
            putInt(ARGON2_ITERATIONS)
            put(ARGON2_PARALLELISM.toByte())
            put(salt.size.toByte())
            put(salt)
            put(iv.size.toByte())
            put(iv)
            put(ciphertext)
        }.array()
    }

    fun decrypt(encryptedBytes: ByteArray, password: String): ByteArray {
        VaultLogger.d("Crypto", "Decrypting ${encryptedBytes.size} bytes")
        val buffer = ByteBuffer.wrap(encryptedBytes)

        val version = buffer.get()
        if (version != VERSION) {
            throw IllegalArgumentException("Unsupported vault version: $version")
        }

        val algorithmId = buffer.get()

        val memoryCostKb = buffer.getInt()
        val iterations = buffer.getInt()
        val parallelism = buffer.get().toInt() and 0xFF

        val saltLength = buffer.get().toInt() and 0xFF
        val salt = ByteArray(saltLength).also { buffer.get(it) }

        val ivLength = buffer.get().toInt() and 0xFF
        val iv = ByteArray(ivLength).also { buffer.get(it) }

        val ciphertext = ByteArray(buffer.remaining()).also { buffer.get(it) }

        VaultLogger.d("Crypto", "Decrypt header: version=$version, algorithmId=$algorithmId, salt=${saltLength}B, iv=${ivLength}B")
        val derivedKey = deriveKey(password, salt, memoryCostKb, iterations, parallelism)
        try {
            return when (algorithmId) {
                ALGORITHM_AES_GCM -> {
                    val keySpec = SecretKeySpec(derivedKey, "AES")
                    val cipher = Cipher.getInstance("AES/GCM/NoPadding")
                    cipher.init(Cipher.DECRYPT_MODE, keySpec, GCMParameterSpec(GCM_TAG_BITS, iv))
                    cipher.doFinal(ciphertext)
                }
                ALGORITHM_AES_CBC -> {
                    val keySpec = SecretKeySpec(derivedKey, "AES")
                    val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
                    cipher.init(Cipher.DECRYPT_MODE, keySpec, IvParameterSpec(iv))
                    cipher.doFinal(ciphertext)
                }
                ALGORITHM_CHACHA20 -> {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                        throw IllegalArgumentException(
                            "ChaCha20-Poly1305 requires API 28+ (current: ${Build.VERSION.SDK_INT})"
                        )
                    }
                    decryptChaCha20(derivedKey, iv, ciphertext)
                }
                else -> throw IllegalArgumentException("Unknown algorithm ID: $algorithmId")
            }
        } finally {
            derivedKey.fill(0)
        }
    }

    private fun deriveKey(password: String, salt: ByteArray): ByteArray {
        return deriveKey(password, salt, ARGON2_MEMORY_KB, ARGON2_ITERATIONS, ARGON2_PARALLELISM)
    }

    private fun deriveKey(
        password: String, salt: ByteArray,
        memoryCostKb: Int, iterations: Int, parallelism: Int
    ): ByteArray {
        val passwordBytes = password.toByteArray(Charsets.UTF_8)
        try {
            val argon2 = Argon2Kt()
            val result = argon2.hash(
                mode = Argon2Mode.ARGON2_ID,
                password = passwordBytes,
                salt = salt,
                tCostInIterations = iterations,
                mCostInKibibyte = memoryCostKb,
                parallelism = parallelism,
                hashLengthInBytes = KEY_LENGTH_BYTES
            )
            return result.rawHashAsByteArray()
        } finally {
            passwordBytes.fill(0)
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun encryptChaCha20(key: ByteArray, nonce: ByteArray, plaintext: ByteArray): ByteArray {
        val keySpec = SecretKeySpec(key, "ChaCha20")
        val cipher = Cipher.getInstance("ChaCha20-Poly1305")
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, IvParameterSpec(nonce))
        return cipher.doFinal(plaintext)
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun decryptChaCha20(key: ByteArray, nonce: ByteArray, ciphertext: ByteArray): ByteArray {
        val keySpec = SecretKeySpec(key, "ChaCha20")
        val cipher = Cipher.getInstance("ChaCha20-Poly1305")
        cipher.init(Cipher.DECRYPT_MODE, keySpec, IvParameterSpec(nonce))
        return cipher.doFinal(ciphertext)
    }
}
