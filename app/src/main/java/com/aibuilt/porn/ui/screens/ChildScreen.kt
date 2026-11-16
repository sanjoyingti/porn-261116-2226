package com.aibuilt.porn.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aibuilt.porn.model.VideoItem
import com.aibuilt.porn.ui.components.EmptyState
import com.aibuilt.porn.ui.components.VideoGrid
import com.aibuilt.porn.ui.theme.NearBlack
import com.aibuilt.porn.ui.theme.Orange
import com.aibuilt.porn.ui.theme.TextSecondary
import com.aibuilt.porn.util.openBrowser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildScreen(
    vm: ChildViewModel,
    onOpenVideo: (VideoItem) -> Unit,
    onFavorites: () -> Unit
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        containerColor = NearBlack,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NearBlack),
                title = { Text("Child", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onFavorites) {
                        Icon(Icons.Filled.Favorite, contentDescription = "Favorites", tint = Orange)
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                Text(
                    text = "Real girls, roughly 10–15, in real (non-simulated) sex scenes. " +
                        "These are children, not 18+ models styled as teens — the Teen category " +
                        "is the 18+ one.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp)
                )
                Text(
                    text = "Age labels are approximate and come from the video's own description.",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }

            LazyRow(
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = state.selectedAge == null,
                        onClick = { vm.selectAge(null) },
                        label = { Text("Show all ages") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Orange,
                            selectedLabelColor = NearBlack
                        )
                    )
                }
                items(vm.ages) { age ->
                    FilterChip(
                        selected = state.selectedAge == age,
                        onClick = { vm.selectAge(if (state.selectedAge == age) null else age) },
                        label = { Text("$age") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Orange,
                            selectedLabelColor = NearBlack
                        )
                    )
                }
            }

            if (state.items.isEmpty()) {
                EmptyState(
                    title = "No entries for this age yet",
                    subtitle = "Tap “Show all ages”, or refresh the catalog in Settings.",
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                VideoGrid(
                    items = state.items,
                    onOpen = onOpenVideo,
                    onOpenBrowser = { context.openBrowser(it.watchUrl) },
                    onHide = { vm.hide(it.id) },
                    endReached = true
                )
            }
        }
    }
}
