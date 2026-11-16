package com.aibuilt.porn.ui

import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.aibuilt.porn.AppContainer
import com.aibuilt.porn.ui.screens.CategoriesViewModel
import com.aibuilt.porn.ui.screens.CategoryViewModel
import com.aibuilt.porn.ui.screens.ChildViewModel
import com.aibuilt.porn.ui.screens.FavoritesViewModel
import com.aibuilt.porn.ui.screens.HomeViewModel
import com.aibuilt.porn.ui.screens.PlayerViewModel
import com.aibuilt.porn.ui.screens.SearchViewModel
import com.aibuilt.porn.ui.screens.SettingsViewModel
import com.aibuilt.porn.ui.screens.TorViewModel

object VmFactory {
    fun home(c: AppContainer) = viewModelFactory {
        initializer { HomeViewModel(c.catalogRepository) }
    }

    fun categories(c: AppContainer) = viewModelFactory {
        initializer { CategoriesViewModel(c.catalogRepository) }
    }

    fun category(c: AppContainer) = viewModelFactory {
        initializer { CategoryViewModel(createSavedStateHandle(), c.catalogRepository) }
    }

    fun search(c: AppContainer) = viewModelFactory {
        initializer { SearchViewModel(c.catalogRepository) }
    }

    fun child(c: AppContainer) = viewModelFactory {
        initializer { ChildViewModel(c.catalogRepository) }
    }

    fun tor(c: AppContainer) = viewModelFactory {
        initializer { TorViewModel(c.catalogRepository) }
    }

    fun favorites(c: AppContainer) = viewModelFactory {
        initializer { FavoritesViewModel(c.catalogRepository) }
    }

    fun player(c: AppContainer) = viewModelFactory {
        initializer { PlayerViewModel(createSavedStateHandle(), c.catalogRepository) }
    }

    fun settings(c: AppContainer, clearImages: () -> Unit) = viewModelFactory {
        initializer { SettingsViewModel(c.settings, c.catalogRepository, clearImages) }
    }
}
