package com.aibuilt.porn.ui.screens

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aibuilt.porn.data.CatalogRepository
import com.aibuilt.porn.model.VideoItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PlayerViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val repo: CatalogRepository
) : ViewModel() {

    private val videoId: String = savedStateHandle["id"] ?: ""

    private val _video = MutableStateFlow<VideoItem?>(null)
    val video: StateFlow<VideoItem?> = _video.asStateFlow()

    private val _related = MutableStateFlow<List<VideoItem>>(emptyList())
    val related: StateFlow<List<VideoItem>> = _related.asStateFlow()

    val isFavorite: StateFlow<Boolean> = repo.favoriteIds
        .map { ids -> videoId in ids }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    init {
        viewModelScope.launch {
            val loaded = repo.video(videoId)
            _video.value = loaded
            if (loaded != null) {
                savedStateHandle["lastUrl"] = loaded.embed
                _related.value = repo.related(loaded.id, loaded.category)
            }
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch { repo.toggleFavorite(videoId) }
    }

    fun markDead() {
        viewModelScope.launch { repo.markDead(videoId) }
    }

    fun hide() {
        viewModelScope.launch { repo.hide(videoId) }
    }

    fun lastUrl(): String? = savedStateHandle["lastUrl"]
}
