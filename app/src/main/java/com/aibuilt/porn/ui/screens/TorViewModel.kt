package com.aibuilt.porn.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aibuilt.porn.data.CatalogRepository
import com.aibuilt.porn.model.TorLink
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

private val GROUP_ORDER = listOf(
    "Adult onion indexes",
    "Tube clones",
    "Forums",
    "Live cams"
)

class TorViewModel(repo: CatalogRepository) : ViewModel() {

    val groups: StateFlow<Map<String, List<TorLink>>> = repo.torLinks
        .map { links ->
            links
                .groupBy { it.group }
                .toList()
                .sortedBy { entry ->
                    val index = GROUP_ORDER.indexOf(entry.first)
                    if (index == -1) GROUP_ORDER.size else index
                }
                .toMap()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())
}
