package com.aibuilt.porn.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aibuilt.porn.data.AppSettings
import com.aibuilt.porn.data.CatalogRepository
import com.aibuilt.porn.data.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settings: SettingsRepository,
    private val repo: CatalogRepository,
    private val clearImages: () -> Unit
) : ViewModel() {

    val state: StateFlow<AppSettings> =
        settings.settings.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings())

    private val _hiddenCount = MutableStateFlow(0)
    val hiddenCount: StateFlow<Int> = _hiddenCount.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private val _updating = MutableStateFlow(false)
    val updating: StateFlow<Boolean> = _updating.asStateFlow()

    init {
        viewModelScope.launch {
            repo.hiddenCount.collect { _hiddenCount.value = it }
        }
    }

    fun setAutoplay(value: Boolean) {
        viewModelScope.launch { settings.setAutoplayRelated(value) }
    }

    fun setCache(value: Boolean) {
        viewModelScope.launch { settings.setCacheThumbnails(value) }
    }

    fun setWarningOn() {
        viewModelScope.launch { settings.setWarningSeen(false) }
    }

    fun setWarningOff() {
        viewModelScope.launch { settings.setWarningSeen(true) }
    }

    fun setRefreshUrl(value: String) {
        viewModelScope.launch {
            settings.setRefreshUrl(value.trim())
            _message.value = "Refresh URL saved"
        }
    }

    fun updateCatalog() {
        viewModelScope.launch {
            _updating.value = true
            repo.refresh()
                .onSuccess { _message.value = "Catalog updated ($it entries)" }
                .onFailure { _message.value = "Update failed: ${it.message}" }
            _updating.value = false
        }
    }

    fun clearCache() {
        clearImages()
        _message.value = "Thumbnail cache cleared"
    }

    fun clearHistory() {
        viewModelScope.launch {
            repo.clearSearches()
            _message.value = "Search history cleared"
        }
    }

    fun restoreHidden() {
        viewModelScope.launch {
            repo.unhideAll()
            _message.value = "Hidden videos restored"
        }
    }

    fun consumeMessage() {
        _message.value = null
    }
}
