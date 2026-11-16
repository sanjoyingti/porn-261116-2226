package com.aibuilt.porn.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aibuilt.porn.model.VideoItem
import com.aibuilt.porn.ui.components.EmptyState
import com.aibuilt.porn.ui.components.EndOfFeed
import com.aibuilt.porn.ui.components.LoadingBox
import com.aibuilt.porn.ui.components.VideoGrid
import com.aibuilt.porn.ui.theme.ErrorRed
import com.aibuilt.porn.ui.theme.NearBlack
import com.aibuilt.porn.ui.theme.Orange
import com.aibuilt.porn.ui.theme.TextSecondary
import com.aibuilt.porn.util.openBrowser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    vm: HomeViewModel,
    onOpenVideo: (VideoItem) -> Unit,
    onSearch: () -> Unit,
    onFavorites: () -> Unit,
    onSettings: () -> Unit
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var menuOpen by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = NearBlack,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NearBlack),
                title = {
                    Text(
                        text = "Porn",
                        color = Orange,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                },
                actions = {
                    IconButton(onClick = onSearch) {
                        Icon(Icons.Filled.Search, contentDescription = "Search", tint = Orange)
                    }
                    IconButton(onClick = onFavorites) {
                        Icon(Icons.Filled.Favorite, contentDescription = "Favorites", tint = Orange)
                    }
                    IconButton(onClick = { menuOpen = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "More", tint = Orange)
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        DropdownMenuItem(
                            text = { Text("Settings") },
                            onClick = {
                                menuOpen = false
                                onSettings()
                            }
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = state.selectedCategory == null,
                        onClick = { vm.selectCategory(null) },
                        label = { Text("All") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Orange,
                            selectedLabelColor = NearBlack
                        )
                    )
                }
                items(vm.chips) { chip ->
                    FilterChip(
                        selected = state.selectedCategory == chip.name,
                        onClick = {
                            vm.selectCategory(if (state.selectedCategory == chip.name) null else chip.name)
                        },
                        label = { Text(chip.name) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Orange,
                            selectedLabelColor = NearBlack
                        )
                    )
                }
            }

            state.error?.let { message ->
                Text(
                    text = "Update failed: $message — showing saved videos",
                    color = ErrorRed,
                    fontSize = 11.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }

            if (state.loading && state.items.isEmpty()) {
                LoadingBox()
            } else if (state.items.isEmpty()) {
                EmptyState(
                    title = "Nothing here yet",
                    subtitle = "Pull down to refresh the catalog or check your connection.",
                    actionLabel = "Try again",
                    onAction = { vm.refresh() }
                )
            } else {
                PullToRefreshBox(
                    isRefreshing = state.refreshing,
                    onRefresh = { vm.refresh() },
                    modifier = Modifier.fillMaxSize()
                ) {
                    VideoGrid(
                        items = state.items,
                        onOpen = onOpenVideo,
                        onOpenBrowser = { context.openBrowser(it.watchUrl) },
                        onHide = { vm.hide(it.id) },
                        endReached = state.endReached,
                        onLoadMore = vm::loadMore,
                        footer = {
                            if (state.endReached) {
                                EndOfFeed()
                            } else {
                                Text(
                                    text = "Loading more…",
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(20.dp)
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}
