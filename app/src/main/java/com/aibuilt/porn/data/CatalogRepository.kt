package com.aibuilt.porn.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import com.aibuilt.porn.model.TorLink
import com.aibuilt.porn.model.VideoItem

class CatalogRepository(
    private val context: Context,
    private val db: AppDatabase,
    private val settings: SettingsRepository,
    private val client: OkHttpClient
) {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    val videos: Flow<List<VideoItem>> =
        combine(db.catalogDao().observeAll(), db.hiddenDao().observeIds()) { list, hidden ->
            val hiddenIds = hidden.toHashSet()
            list.filterNot { it.id in hiddenIds }.map { it.toModel() }
        }

    val favorites: Flow<List<VideoItem>> =
        combine(db.favoriteDao().observeFavorites(), db.hiddenDao().observeIds()) { list, hidden ->
            val hiddenIds = hidden.toHashSet()
            list.filterNot { it.id in hiddenIds }.map { it.toModel() }
        }

    val favoriteIds: Flow<Set<String>> =
        db.favoriteDao().observeIds().map { it.toHashSet() }

    val recentSearches: Flow<List<String>> =
        db.recentSearchDao().observeAll().map { rows -> rows.map { it.query } }

    val hiddenCount: Flow<Int> = db.hiddenDao().observeCount()

    val torLinks: Flow<List<TorLink>> =
        db.torDao().observeAll().map { rows -> rows.map { it.toModel() } }

    val categoryCounts: Flow<Map<String, Int>> =
        db.catalogDao().observeCounts().map { rows -> rows.associate { it.category to it.total } }

    suspend fun ensureSeeded() = withContext(Dispatchers.IO) {
        if (db.catalogDao().count() == 0) {
            val items = readAssetVideos()
            if (items.isNotEmpty()) db.catalogDao().insertAll(items.map { it.toEntity() })
        }
        if (db.torDao().count() == 0) {
            val links = readAssetTor()
            if (links.isNotEmpty()) db.torDao().insertAll(links.map { it.toEntity() })
        }
    }

    private suspend fun readAssetVideos(): List<VideoItem> = withContext(Dispatchers.IO) {
        runCatching {
            val text = context.assets.open("catalog.json").bufferedReader().use { it.readText() }
            json.decodeFromString<List<VideoItem>>(text)
        }.getOrDefault(emptyList())
    }

    private suspend fun readAssetTor(): List<TorLink> = withContext(Dispatchers.IO) {
        runCatching {
            val text = context.assets.open("tor.json").bufferedReader().use { it.readText() }
            json.decodeFromString<List<TorLink>>(text)
        }.getOrDefault(emptyList())
    }

    private suspend fun fetchVideos(url: String): Int {
        val request = Request.Builder().url(url).header("User-Agent", "PornApp/1.0").build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) error("HTTP ${response.code}")
            val body = response.body?.string().orEmpty()
            val parsed = json.decodeFromString<List<VideoItem>>(body)
            require(parsed.isNotEmpty()) { "Catalog is empty" }
            db.catalogDao().insertAll(parsed.map { it.toEntity() })
            return parsed.size
        }
    }

    suspend fun refresh(): Result<Int> = withContext(Dispatchers.IO) {
        val url = settings.settings.first().refreshUrl
        val attempt = runCatching { fetchVideos(url) }
        attempt.recoverCatching { error ->
            if (db.catalogDao().count() == 0) {
                val items = readAssetVideos()
                if (items.isEmpty()) throw error
                db.catalogDao().insertAll(items.map { it.toEntity() })
                items.size
            } else {
                throw error
            }
        }
    }

    suspend fun video(id: String): VideoItem? = withContext(Dispatchers.IO) {
        db.catalogDao().byId(id)?.toModel()
    }

    suspend fun related(id: String, category: String): List<VideoItem> =
        withContext(Dispatchers.IO) {
            db.catalogDao().related(id, category).map { it.toModel() }
        }

    suspend fun toggleFavorite(id: String) = withContext(Dispatchers.IO) {
        val current = db.favoriteDao().observeIds().first()
        if (id in current) db.favoriteDao().remove(id)
        else db.favoriteDao().add(FavoriteEntity(id, System.currentTimeMillis()))
    }

    suspend fun hide(id: String) = withContext(Dispatchers.IO) {
        db.hiddenDao().add(HiddenEntity(id))
    }

    suspend fun unhideAll() = withContext(Dispatchers.IO) {
        db.hiddenDao.clear()
    }

    suspend fun markDead(id: String) = withContext(Dispatchers.IO) {
        db.deadLinkDao().add(DeadLinkEntity(id, System.currentTimeMillis()))
    }

    suspend fun saveSearch(query: String) = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.length < 2) return@withContext
        db.recentSearchDao().add(RecentSearchEntity(trimmed, System.currentTimeMillis()))
    }

    suspend fun clearSearches() = withContext(Dispatchers.IO) {
        db.recentSearchDao().clear()
    }
}
