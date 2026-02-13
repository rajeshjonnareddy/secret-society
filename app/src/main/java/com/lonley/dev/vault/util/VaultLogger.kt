package com.lonley.dev.vault.util

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object VaultLogger {

    private const val TAG = "Vault"
    private const val LOG_FILE_NAME = "vault.log"
    private const val MAX_LOG_SIZE_BYTES = 2 * 1024 * 1024 // 2 MB

    private var logFile: File? = null
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)

    fun init(context: Context) {
        logFile = File(context.filesDir, LOG_FILE_NAME)
        i("Logger", "Log file initialized: ${logFile?.absolutePath}")
    }

    fun d(area: String, message: String) {
        Log.d(TAG, "[$area] $message")
        writeToFile("D", area, message)
    }

    fun i(area: String, message: String) {
        Log.i(TAG, "[$area] $message")
        writeToFile("I", area, message)
    }

    fun w(area: String, message: String) {
        Log.w(TAG, "[$area] $message")
        writeToFile("W", area, message)
    }

    fun e(area: String, message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Log.e(TAG, "[$area] $message", throwable)
            writeToFile("E", area, "$message\n${Log.getStackTraceString(throwable)}")
        } else {
            Log.e(TAG, "[$area] $message")
            writeToFile("E", area, message)
        }
    }

    private fun writeToFile(level: String, area: String, message: String) {
        val file = logFile ?: return
        try {
            rotateIfNeeded(file)
            val timestamp = dateFormat.format(Date())
            val line = "$timestamp $level/$TAG [$area] $message\n"
            FileWriter(file, true).use { it.write(line) }
        } catch (_: Exception) {
            // Silently ignore file write failures — logcat still works
        }
    }

    private fun rotateIfNeeded(file: File) {
        if (file.exists() && file.length() > MAX_LOG_SIZE_BYTES) {
            val oldFile = File(file.parent, "vault.log.old")
            if (oldFile.exists()) oldFile.delete()
            file.renameTo(oldFile)
        }
    }
}
