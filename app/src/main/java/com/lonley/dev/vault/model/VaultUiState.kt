package com.lonley.dev.vault.model

sealed interface VaultUiState {
    data object Initializing : VaultUiState
    data object Locked : VaultUiState
    data class PromptUnlock(val fileName: String) : VaultUiState
    data class Loading(val message: String) : VaultUiState
    data class Unlocked(
        val vaultName: String,
        val entries: List<PasswordEntry>,
        val isLoading: Boolean = false,
        val username: String = "",
        val email: String = "",
        val encryptionType: String = "AES-256-GCM",
        val lastUpdatedAt: Long = 0L
    ) : VaultUiState
    data object PickFile : VaultUiState
    data class Error(val message: String, val previous: VaultUiState?, val attemptsRemaining: Int = -1) : VaultUiState
}

sealed interface SaveState {
    data object Idle : SaveState
    data object Saving : SaveState
    data object SaveSuccess : SaveState
    data class SaveError(val message: String) : SaveState
}
