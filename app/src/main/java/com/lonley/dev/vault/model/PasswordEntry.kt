package com.lonley.dev.vault.model

data class PasswordEntry(
    val id: String,
    val name: String,
    val username: String? = null,
    val email: String? = null,
    val password: String,
    val website: String? = null,
    val comments: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val isSubscription: Boolean = false,
    val planType: PlanType? = null,
    val price: String? = null,
    val subscriptionEmail: String? = null,
    val startDate: Long? = null,
    val reminderEnabled: Boolean = false,
    val entryType: EntryType = EntryType.Password,
    val phraseWordCount: Int? = null
)
