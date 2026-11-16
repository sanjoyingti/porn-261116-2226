package com.aibuilt.porn.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aibuilt.porn.data.CatalogRepository
import com.aibuilt.porn.model.VideoItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ChildUiState(
    val items: List<VideoItem> = emptyList(),
    val selectedAge: Int? = null,
    val showAll: Boolean = true
)

class ChildViewModel(private val repo: CatalogRepository) : ViewModel() {

    private val age = MutableStateFlow<Int?>(null)

    val state: StateFlow<ChildUiState> = combine(repo.videos, age) { videos, selectedAge ->
        val child = videos.filter { it.category == "Child" }
        val filtered = if (selectedAge == null) child else child.filter { it.age == selectedAge }
        ChildUiState(
            items = filtered.sortedWith(
                compareBy({ it.age ?: 99 }, { it.source.lowercase() }, { -it.date })
            ),
            selectedAge = selectedAge,
            showAll = selectedAge == null
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ChildUiState())

    val ages = listOf(10, 11, 12, 13, 14, 15)

    fun selectAge(value: Int?) {
        age.value = value
    }

    fun hide(id: String) {
        viewModelScope.launch { repo.hide(id) }
    }
}
