package com.lonley.dev.vault.model

data class PasswordEntry(
    val id: String,
    val name: String,
    val username: String,
    val password: String,
    val website: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
