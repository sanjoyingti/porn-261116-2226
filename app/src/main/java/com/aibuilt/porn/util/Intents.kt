package com.aibuilt.porn.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.widget.Toast

fun Context.openBrowser(url: String) {
    if (url.isBlank()) return
    try {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    } catch (e: Exception) {
        Toast.makeText(this, "No browser found", Toast.LENGTH_SHORT).show()
    }
}

fun Context.openTor(url: String) {
    val torIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    torIntent.addCategory(Intent.CATEGORY_BROWSABLE)
    try {
        startActivity(torIntent)
    } catch (e: Exception) {
        Toast.makeText(this, "Tor Browser not installed", Toast.LENGTH_LONG).show()
        val play = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://play.google.com/store/apps/details?id=org.torproject.torbrowser")
        )
        runCatching { startActivity(play) }
    }
}

fun Context.findActivity(): Activity? {
    var ctx: Context? = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}
