package com.aibuilt.porn.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aibuilt.porn.data.CatalogRepository
import com.aibuilt.porn.model.Categories
import com.aibuilt.porn.model.VideoItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

data class HomeUiState(
    val items: List<VideoItem> = emptyList(),
    val endReached: Boolean = false,
    val refreshing: Boolean = false,
    val loading: Boolean = true,
    val error: String? = null,
    val selectedCategory: String? = null
)

private data class Flags(
    val refreshing: Boolean = false,
    val loading: Boolean = true,
    val error: String? = null
)

private const val PAGE_SIZE = 20

class HomeViewModel(private val repo: CatalogRepository) : ViewModel() {

    private val selectedCategory = MutableStateFlow<String?>(null)
    private val visible = MutableStateFlow(PAGE_SIZE)
    private val flags = MutableStateFlow(Flags())

    val state: StateFlow<HomeUiState> = combine(
        repo.videos,
        selectedCategory,
        visible,
        flags
    ) { videos, category, visibleCount, flagsNow ->
        val filtered = if (category == null) videos else videos.filter { it.category == category }
        HomeUiState(
            items = filtered.take(visibleCount),
            endReached = visibleCount >= filtered.size && filtered.isNotEmpty(),
            refreshing = flagsNow.refreshing,
            loading = flagsNow.loading,
            error = flagsNow.error,
            selectedCategory = category
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    val chips = Categories.ALL

    init {
        viewModelScope.launch {
            withTimeoutOrNull(2_500) { repo.videos.first { it.isNotEmpty() } }
            delay(200)
            flags.value = flags.value.copy(loading = false)
        }
    }

    fun loadMore() {
        visible.value += PAGE_SIZE
    }

    fun selectCategory(name: String?) {
        selectedCategory.value = name
        visible.value = PAGE_SIZE
    }

    fun refresh() {
        viewModelScope.launch {
            flags.value = flags.value.copy(refreshing = true, error = null)
            repo.refresh()
                .onFailure { flags.value = flags.value.copy(error = it.message ?: "Refresh failed") }
            flags.value = flags.value.copy(refreshing = false, loading = false)
        }
    }

    fun hide(id: String) {
        viewModelScope.launch { repo.hide(id) }
    }

    fun dismissError() {
        flags.value = flags.value.copy(error = null)
    }
}
