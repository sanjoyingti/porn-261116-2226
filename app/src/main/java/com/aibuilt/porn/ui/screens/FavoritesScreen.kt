package com.aibuilt.porn.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aibuilt.porn.model.VideoItem
import com.aibuilt.porn.ui.components.EmptyState
import com.aibuilt.porn.ui.components.VideoGrid
import com.aibuilt.porn.ui.theme.NearBlack
import com.aibuilt.porn.util.openBrowser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    vm: FavoritesViewModel,
    onBack: () -> Unit,
    onOpenVideo: (VideoItem) -> Unit
) {
    val items by vm.items.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        containerColor = NearBlack,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NearBlack),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = { Text("Favorites", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            if (items.isEmpty()) {
                EmptyState(
                    title = "No favorites yet",
                    subtitle = "Open a video and tap the heart to save it here.",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                VideoGrid(
                    items = items,
                    onOpen = onOpenVideo,
                    onOpenBrowser = { context.openBrowser(it.watchUrl) },
                    onHide = { vm.hide(it.id) },
                    endReached = true
                )
            }
        }
    }
}
