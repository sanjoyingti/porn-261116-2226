package com.aibuilt.porn.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aibuilt.porn.data.CatalogRepository
import com.aibuilt.porn.model.VideoItem
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FavoritesViewModel(private val repo: CatalogRepository) : ViewModel() {

    val items: StateFlow<List<VideoItem>> = repo.favorites
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun hide(id: String) {
        viewModelScope.launch { repo.hide(id) }
    }
}
