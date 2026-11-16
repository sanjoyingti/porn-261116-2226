package com.aibuilt.porn.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aibuilt.porn.data.CatalogRepository
import com.aibuilt.porn.model.VideoItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SearchUiState(
    val query: String = "",
    val results: List<VideoItem> = emptyList(),
    val recent: List<String> = emptyList()
)

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class SearchViewModel(private val repo: CatalogRepository) : ViewModel() {

    private val query = MutableStateFlow("")

    private val results = query.flatMapLatest { q ->
        if (q.isBlank()) flowOf(emptyList())
        else repo.videos.map { list ->
            list.filter {
                it.title.contains(q, ignoreCase = true) ||
                    it.category.contains(q, ignoreCase = true) ||
                    it.source.contains(q, ignoreCase = true)
            }.sortedByDescending { it.date }
        }
    }

    val state: StateFlow<SearchUiState> = combine(query, results, repo.recentSearches) { q, r, recent ->
        SearchUiState(query = q, results = r, recent = recent)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SearchUiState())

    val suggestions = listOf(
        "lesbian", "milf", "anal", "creampie", "amateur", "pov",
        "big tits", "squirt", "russian", "hardcore"
    )

    init {
        viewModelScope.launch {
            query.debounce(900).collect { q ->
                if (q.trim().length >= 3) repo.saveSearch(q)
            }
        }
    }

    fun updateQuery(value: String) {
        query.value = value
    }

    fun submit(queryValue: String) {
        query.value = queryValue
        viewModelScope.launch { repo.saveSearch(queryValue) }
    }

    fun clearRecent() {
        viewModelScope.launch { repo.clearSearches() }
    }

    fun hide(id: String) {
        viewModelScope.launch { repo.hide(id) }
    }
}
