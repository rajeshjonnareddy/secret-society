package com.lonley.dev.vault.model

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

enum class PlanType(val label: String) {
    Weekly("Weekly"),
    Monthly("Monthly"),
    Yearly("Yearly")
}

fun PasswordEntry.nextRenewalDate(): Long? {
    if (!isSubscription || planType == null || startDate == null) return null
    val startLocal = Instant.ofEpochMilli(startDate)
        .atZone(ZoneId.systemDefault()).toLocalDate()
    val today = LocalDate.now()
    var next = startLocal
    while (!next.isAfter(today)) {
        next = when (planType) {
            PlanType.Weekly -> next.plusWeeks(1)
            PlanType.Monthly -> next.plusMonths(1)
            PlanType.Yearly -> next.plusYears(1)
        }
    }
    return next.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
}

fun formatPrice(raw: String?): String? {
    if (raw.isNullOrBlank()) return null
    if ('.' in raw) return raw  // old format, already "9.99"
    val cents = raw.toLongOrNull() ?: return raw
    return "%.2f".format(cents / 100.0)
}
