package com.aibuilt.porn.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatDuration(seconds: Int): String {
    val safe = seconds.coerceAtLeast(0)
    val h = safe / 3600
    val m = (safe % 3600) / 60
    val s = safe % 60
    return if (h > 0) String.format(Locale.US, "%d:%02d:%02d", h, m, s)
    else String.format(Locale.US, "%d:%02d", m, s)
}

fun formatViews(views: Long): String = when {
    views >= 1_000_000 -> String.format(Locale.US, "%.1fM", views / 1_000_000.0)
    views >= 1_000 -> String.format(Locale.US, "%.1fK", views / 1_000.0)
    else -> views.toString()
}

fun formatDate(epochMs: Long): String {
    if (epochMs <= 0L) return ""
    val diff = System.currentTimeMillis() - epochMs
    val days = diff / 86_400_000L
    return when {
        days < 1 -> "today"
        days < 2 -> "1d ago"
        days < 30 -> "${days}d ago"
        days < 365 -> "${days / 30}mo ago"
        else -> SimpleDateFormat("d MMM yyyy", Locale.US).format(Date(epochMs))
    }
}
