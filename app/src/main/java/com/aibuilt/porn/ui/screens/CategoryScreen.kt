package com.aibuilt.porn.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aibuilt.porn.model.SortOrder
import com.aibuilt.porn.model.VideoItem
import com.aibuilt.porn.ui.components.EmptyState
import com.aibuilt.porn.ui.components.EndOfFeed
import com.aibuilt.porn.ui.components.VideoGrid
import com.aibuilt.porn.ui.theme.NearBlack
import com.aibuilt.porn.ui.theme.Orange
import com.aibuilt.porn.util.openBrowser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    vm: CategoryViewModel,
    onBack: () -> Unit,
    onOpenVideo: (VideoItem) -> Unit
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        containerColor = NearBlack,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NearBlack),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                title = { Text(state.name, fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(SortOrder.entries.toList()) { order ->
                    FilterChip(
                        selected = state.sort == order,
                        onClick = { vm.setSort(order) },
                        label = { Text(order.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Orange,
                            selectedLabelColor = NearBlack
                        )
                    )
                }
            }
            if (state.items.isEmpty()) {
                EmptyState(
                    title = "No videos in this category yet",
                    subtitle = "Refresh the catalog from Settings or Home.",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                VideoGrid(
                    items = state.items,
                    onOpen = onOpenVideo,
                    onOpenBrowser = { context.openBrowser(it.watchUrl) },
                    onHide = { vm.hide(it.id) },
                    endReached = state.endReached,
                    onLoadMore = vm::loadMore,
                    footer = { if (state.endReached) EndOfFeed() }
                )
            }
        }
    }
}
