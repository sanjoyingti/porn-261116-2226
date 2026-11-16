package com.aibuilt.porn.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aibuilt.porn.model.TorLink
import com.aibuilt.porn.ui.components.EmptyState
import com.aibuilt.porn.ui.theme.NearBlack
import com.aibuilt.porn.ui.theme.Orange
import com.aibuilt.porn.ui.theme.SurfaceDark
import com.aibuilt.porn.ui.theme.TextSecondary
import com.aibuilt.porn.util.openTor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TorScreen(vm: TorViewModel) {
    val groups by vm.groups.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    Scaffold(
        containerColor = NearBlack,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NearBlack),
                title = { Text("Tor / .onion", fontWeight = FontWeight.Bold) },
                actions = {
                    Text(
                        "dark web",
                        color = Orange,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(end = 14.dp)
                    )
                }
            )
        }
    ) { padding ->
        if (groups.isEmpty()) {
            EmptyState(
                title = "No Tor links loaded",
                subtitle = "Refresh the catalog from Settings to fetch the onion list.",
                modifier = Modifier.padding(padding)
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(padding)
            ) {
                groups.forEach { (group, links) ->
                    item(key = "header-$group") {
                        Text(
                            text = group,
                            color = Orange,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 12.dp, bottom = 2.dp)
                        )
                    }
                    items(links, key = { it.id }) { link ->
                        TorRow(
                            link = link,
                            onCopy = {
                                clipboard.setText(AnnotatedString(link.url))
                                Toast.makeText(context, "Onion link copied", Toast.LENGTH_SHORT).show()
                            },
                            onOpen = {
                                clipboard.setText(AnnotatedString(link.url))
                                context.openTor(link.url)
                            }
                        )
                    }
                }
                item {
                    Text(
                        text = "Onion links change often. If one fails, refresh the catalog and " +
                            "open it in Tor Browser rather than a normal browser.",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TorRow(link: TorLink, onCopy: () -> Unit, onOpen: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(SurfaceDark)
            .padding(12.dp)
    ) {
        Text(text = link.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Text(
            text = link.description,
            color = TextSecondary,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 2.dp)
        )
        Text(
            text = link.url,
            color = Orange,
            fontSize = 11.sp,
            modifier = Modifier.padding(top = 6.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onCopy) { Text("Copy link") }
            TextButton(onClick = onOpen) { Text("Open in Tor Browser") }
        }
    }
}
