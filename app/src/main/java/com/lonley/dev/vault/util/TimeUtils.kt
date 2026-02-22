package com.lonley.dev.vault.util

fun formatRelativeTime(timestampMs: Long, neverText: String = "Never", justNowText: String = "Just now"): String {
    if (timestampMs == 0L) return neverText
    val diff = System.currentTimeMillis() - timestampMs
    val minutes = diff / 60_000
    val hours = minutes / 60
    val days = hours / 24
    return when {
        minutes < 1 -> justNowText
        minutes < 60 -> "${minutes}m ago"
        hours < 24 -> "${hours}h ago"
        days < 30 -> "${days}d ago"
        days < 365 -> "${days / 30}mo ago"
        else -> "${days / 365}y ago"
    }
}
