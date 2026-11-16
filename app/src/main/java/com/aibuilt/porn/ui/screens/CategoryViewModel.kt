package com.aibuilt.porn.ui.screens

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aibuilt.porn.data.CatalogRepository
import com.aibuilt.porn.model.Categories
import com.aibuilt.porn.model.SortOrder
import com.aibuilt.porn.model.VideoItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CategoryUiState(
    val name: String = "",
    val items: List<VideoItem> = emptyList(),
    val sort: SortOrder = SortOrder.NEWEST,
    val endReached: Boolean = false
)

private const val PAGE_SIZE = 20

class CategoryViewModel(
    savedStateHandle: SavedStateHandle,
    private val repo: CatalogRepository
) : ViewModel() {

    private val slug: String = savedStateHandle["slug"] ?: ""
    private val categoryName: String = Categories.bySlug(slug)?.name ?: slug
    private val sort = MutableStateFlow(SortOrder.NEWEST)
    private val visible = MutableStateFlow(PAGE_SIZE)

    val state: StateFlow<CategoryUiState> = combine(repo.videos, sort, visible) { videos, order, count ->
        val filtered = videos.filter { it.category == categoryName }
        val sorted = when (order) {
            SortOrder.NEWEST -> filtered.sortedByDescending { it.date }
            SortOrder.LONGEST -> filtered.sortedByDescending { it.duration }
            SortOrder.MOST_VIEWED -> filtered.sortedByDescending { it.views }
        }
        CategoryUiState(
            name = categoryName,
            items = sorted.take(count),
            sort = order,
            endReached = count >= sorted.size && sorted.isNotEmpty()
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        CategoryUiState(name = categoryName)
    )

    fun setSort(order: SortOrder) {
        sort.value = order
        visible.value = PAGE_SIZE
    }

    fun loadMore() {
        visible.value += PAGE_SIZE
    }

    fun hide(id: String) {
        viewModelScope.launch { repo.hide(id) }
    }
}
