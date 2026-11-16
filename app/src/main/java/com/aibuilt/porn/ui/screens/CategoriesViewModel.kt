package com.aibuilt.porn.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aibuilt.porn.data.CatalogRepository
import com.aibuilt.porn.model.Categories
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class CategoryTile(val name: String, val slug: String, val count: Int, val cover: String?)

class CategoriesViewModel(repo: CatalogRepository) : ViewModel() {

    val tiles: StateFlow<List<CategoryTile>> = combine(
        repo.categoryCounts,
        repo.videos
    ) { counts, videos ->
        Categories.ALL.map { category ->
            val cover = videos.firstOrNull { it.category == category.name }?.thumbnailUrl
            CategoryTile(
                name = category.name,
                slug = category.slug,
                count = counts[category.name] ?: 0,
                cover = cover
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
