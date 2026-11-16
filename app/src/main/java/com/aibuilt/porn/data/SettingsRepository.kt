package com.aibuilt.porn.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "porn_settings")

data class AppSettings(
    val autoplayRelated: Boolean = true,
    val cacheThumbnails: Boolean = true,
    val warningSeen: Boolean = false,
    val refreshUrl: String = DEFAULT_REFRESH_URL
) {
    companion object {
        const val DEFAULT_REFRESH_URL =
            "https://raw.githubusercontent.com/aibuilt-porn/catalog/main/catalog.json"
    }
}

class SettingsRepository(private val context: Context) {

    private object Keys {
        val AUTOPLAY = booleanPreferencesKey("autoplay_related")
        val CACHE = booleanPreferencesKey("cache_thumbnails")
        val WARNING = booleanPreferencesKey("warning_seen")
        val REFRESH_URL = stringPreferencesKey("refresh_url")
    }

    val settings: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            autoplayRelated = prefs[Keys.AUTOPLAY] ?: true,
            cacheThumbnails = prefs[Keys.CACHE] ?: true,
            warningSeen = prefs[Keys.WARNING] ?: false,
            refreshUrl = prefs[Keys.REFRESH_URL] ?: AppSettings.DEFAULT_REFRESH_URL
        )
    }

    suspend fun setAutoplayRelated(value: Boolean) {
        context.dataStore.edit { it[Keys.AUTOPLAY] = value }
    }

    suspend fun setCacheThumbnails(value: Boolean) {
        context.dataStore.edit { it[Keys.CACHE] = value }
    }

    suspend fun setWarningSeen(value: Boolean = true) {
        context.dataStore.edit { it[Keys.WARNING] = value }
    }

    suspend fun setRefreshUrl(value: String) {
        context.dataStore.edit { it[Keys.REFRESH_URL] = value }
    }
}
