package com.lonley.dev.vault.model

enum class PlanType(val label: String, val periodDays: Long) {
    Weekly("Weekly", 7),
    Monthly("Monthly", 30),
    Yearly("Yearly", 365)
}

fun PasswordEntry.nextRenewalDate(): Long? {
    if (!isSubscription || planType == null || startDate == null) return null
    val periodMs = planType.periodDays * 86_400_000L
    val now = System.currentTimeMillis()
    var next = startDate
    while (next!! < now) { next += periodMs }
    return next
}
