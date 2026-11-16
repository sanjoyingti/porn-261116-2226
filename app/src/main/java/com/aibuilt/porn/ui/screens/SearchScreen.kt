package com.aibuilt.porn.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    vm: SearchViewModel,
    onBack: () -> Unit,
    onOpenVideo: (VideoItem) -> Unit
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) { runCatching { focusRequester.requestFocus() } }

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
                title = {
                    TextField(
                        value = state.query,
                        onValueChange = vm::updateQuery,
                        placeholder = { Text("Search videos and categories") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { vm.submit(state.query) }),
                        trailingIcon = {
                            if (state.query.isNotEmpty()) {
                                IconButton(onClick = { vm.updateQuery("") }) {
                                    Icon(Icons.Filled.Close, contentDescription = "Clear")
                                }
                            }
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Orange,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier.focusRequester(focusRequester)
                    )
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            when {
                state.query.isBlank() -> {
                    if (state.recent.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Recent searches", color = TextSecondary, fontSize = 13.sp)
                            TextButton(onClick = vm::clearRecent) { Text("Clear") }
                        }
                        FlowRow(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            state.recent.forEach { recent ->
                                AssistChip(
                                    onClick = { vm.submit(recent) },
                                    label = { Text(recent) }
                                )
                            }
                        }
                    }
                    Text(
                        text = "Popular searches",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(start = 12.dp, top = 12.dp, bottom = 4.dp)
                    )
                    FlowRow(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        vm.suggestions.forEach { suggestion ->
                            AssistChip(
                                onClick = { vm.submit(suggestion) },
                                label = { Text(suggestion) }
                            )
                        }
                    }
                }

                state.results.isEmpty() -> {
                    EmptyState(
                        title = "No results for \"${state.query}\"",
                        subtitle = "Try a shorter word or search the web.",
                        actionLabel = "Search the web",
                        onAction = {
                            context.openBrowser(
                                "https://www.google.com/search?q=" +
                                    java.net.URLEncoder.encode(state.query, "UTF-8") + "+sex+scene"
                            )
                        }
                    )
                }

                else -> {
                    VideoGrid(
                        items = state.results,
                        onOpen = onOpenVideo,
                        onOpenBrowser = { context.openBrowser(it.watchUrl) },
                        onHide = { vm.hide(it.id) },
                        columns = 1
                    )
                }
            }
        }
    }
}
