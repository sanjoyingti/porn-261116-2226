package com.aibuilt.porn.ui.components

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.diskCachePolicy
import com.aibuilt.porn.model.VideoItem
import com.aibuilt.porn.ui.LocalCacheThumbnails
import com.aibuilt.porn.ui.theme.NearBlack
import com.aibuilt.porn.ui.theme.Orange
import com.aibuilt.porn.ui.theme.TextSecondary
import com.aibuilt.porn.util.formatDate
import com.aibuilt.porn.util.formatDuration
import com.aibuilt.porn.util.formatViews

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VideoCard(
    item: VideoItem,
    onClick: () -> Unit,
    onOpenBrowser: () -> Unit,
    onHide: () -> Unit,
    modifier: Modifier = Modifier
) {
    var menuOpen by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val cacheEnabled = LocalCacheThumbnails.current

    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = { menuOpen = true }
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(item.thumbnailUrl)
                        .crossfade(true)
                        .diskCachePolicy(if (cacheEnabled) CachePolicy.ENABLED else CachePolicy.DISABLED)
                        .build(),
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Badge(text = item.source, background = Orange, textColor = NearBlack)
                    if (item.age != null) {
                        Badge(
                            text = "Age ${item.age}",
                            background = Color.Black.copy(alpha = 0.75f),
                            textColor = Orange
                        )
                    }
                }
                Text(
                    text = formatDuration(item.duration),
                    color = Color.White,
                    fontSize = 11.sp,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.Black.copy(alpha = 0.7f))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                )
            }
            Text(
                text = item.title,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 13.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 6.dp)
            )
            Text(
                text = "${formatViews(item.views)} views · ${formatDate(item.date)}",
                color = TextSecondary,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
            )
        }

        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
            DropdownMenuItem(
                text = { Text("Open in browser") },
                onClick = {
                    menuOpen = false
                    onOpenBrowser()
                }
            )
            DropdownMenuItem(
                text = { Text("Copy link") },
                onClick = {
                    menuOpen = false
                    clipboard.setText(AnnotatedString(item.watchUrl))
                    Toast.makeText(context, "Link copied", Toast.LENGTH_SHORT).show()
                }
            )
            DropdownMenuItem(
                text = { Text("Hide video") },
                onClick = {
                    menuOpen = false
                    onHide()
                    Toast.makeText(context, "Hidden", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

@Composable
fun Badge(text: String, background: Color, textColor: Color) {
    Text(
        text = text,
        color = textColor,
        fontSize = 10.sp,
        maxLines = 1,
        modifier = Modifier
            .clip(RoundedCornerShape(3.dp))
            .background(background)
            .padding(horizontal = 5.dp, vertical = 2.dp)
    )
}
