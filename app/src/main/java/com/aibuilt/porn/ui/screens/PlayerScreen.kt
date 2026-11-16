package com.aibuilt.porn.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Color as AndroidColor
import android.os.Message
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.aibuilt.porn.model.VideoItem
import com.aibuilt.porn.ui.theme.NearBlack
import com.aibuilt.porn.ui.theme.Orange
import com.aibuilt.porn.ui.theme.SurfaceHigher
import com.aibuilt.porn.ui.theme.TextSecondary
import com.aibuilt.porn.util.findActivity
import com.aibuilt.porn.util.formatDate
import com.aibuilt.porn.util.formatDuration
import com.aibuilt.porn.util.formatViews
import com.aibuilt.porn.util.openBrowser
import kotlinx.coroutines.delay

private const val MOBILE_UA =
    "Mozilla/5.0 (Linux; Android 14; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) " +
        "Chrome/124.0.0.0 Mobile Safari/537.36"

private val INTERNAL_HOSTS = listOf(
    "pornhub.com", "xhamster.com", "eporner.com", "spankbang.com", "xnxx.com",
    "txxx.com", "hqporner.com", "youporn.com", "youtube.com", "youtu.be",
    "dailymotion.com", "dai.ly", "ok.ru", "vk.com", "archive.org"
)

private const val CLEANUP_JS = """
(function(){
  var css = [
    '.mgid','.ad','.ads','.ad-container','.advertisement','.adsbygoogle',
    '[id^="ad_"]','[id*="banner"]','.popup','.overlay','.modal','.subscribe',
    '.app-banner','.download-app','.sticky-banner','.ph-app-banner','.js_appBanner',
    '.promoBanner','.xhamster-ad','.advertisment','.cookie-banner','.topBanner'
  ].join(',') + '{display:none !important;visibility:hidden !important;height:0 !important;}';
  var style = document.createElement('style');
  style.type = 'text/css';
  style.appendChild(document.createTextNode(css));
  (document.head || document.documentElement).appendChild(style);
  var noise = ['.overlay','.popup','.modal','.ad','.ads','.banner','.promo','.cookies'];
  function strip(){
    try {
      noise.forEach(function(sel){
        var list = document.querySelectorAll(sel);
        for (var i = 0; i < list.length; i++) {
          var el = list[i];
          if (el.querySelector('video') || el.querySelector('iframe')) continue;
          var r = el.getBoundingClientRect();
          if (r.width < 40 || r.height < 40) el.style.setProperty('display','none','important');
        }
      });
      var blocks = document.querySelectorAll('body > div');
      for (var j = 0; j < blocks.length; j++) {
        var b = blocks[j];
        if (b.querySelector('video') || b.querySelector('iframe')) continue;
        var rr = b.getBoundingClientRect();
        if (rr.top > window.innerHeight * 0.7) b.style.setProperty('display','none','important');
      }
      var vids = document.querySelectorAll('video');
      for (var k = 0; k < vids.length; k++) { vids[k].setAttribute('playsinline',''); }
    } catch (e) {}
  }
  strip();
  setTimeout(strip, 1200);
  setTimeout(strip, 3500);
  setTimeout(strip, 7000);
})();
"""

@Composable
fun PlayerScreen(
    vm: PlayerViewModel,
    autoplayRelated: Boolean,
    onBack: () -> Unit,
    onOpenVideo: (VideoItem) -> Unit
) {
    val video by vm.video.collectAsStateWithLifecycle()
    val related by vm.related.collectAsStateWithLifecycle()
    val favorite by vm.isFavorite.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var fullscreen by remember { mutableStateOf(false) }
    var failed by remember { mutableStateOf(false) }
    var progress by remember { mutableIntStateOf(0) }
    var loaded by remember { mutableStateOf(false) }
    var reloadKey by remember { mutableIntStateOf(0) }
    var webView by remember { mutableStateOf<WebView?>(null) }

    val activity = remember(context) { context.findActivity() }

    BackHandler(enabled = true) {
        if (fullscreen) fullscreen = false else onBack()
    }

    DisposableEffect(fullscreen) {
        val window = activity?.window
        val controller = window?.let { WindowCompat.getInsetsController(it, it.decorView) }
        if (fullscreen) {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            controller?.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller?.hide(WindowInsetsCompat.Type.systemBars())
        } else {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            controller?.show(WindowInsetsCompat.Type.systemBars())
        }
        onDispose {
            controller?.show(WindowInsetsCompat.Type.systemBars())
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            webView?.loadUrl("about:blank")
            webView?.destroy()
        }
    }

    LaunchedEffect(video?.id, failed, reloadKey) {
        val current = video
        if (current == null || failed) return@LaunchedEffect
        loaded = false
        progress = 0
        delay(15_000)
        if (!loaded) failed = true
    }

    val scroll = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize().background(NearBlack)) {
        Column(
            modifier = if (fullscreen) Modifier.fillMaxSize()
            else Modifier
                .fillMaxSize()
                .verticalScroll(scroll)
        ) {
            Box(
                modifier = (if (fullscreen) Modifier.fillMaxSize()
                else Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f))
                    .background(Color.Black)
            ) {
                val current = video
                if (current != null && !failed) {
                    key(current.id, reloadKey) {
                        AndroidView(
                            modifier = Modifier.fillMaxSize(),
                            factory = { ctx ->
                                buildWebView(
                                    context = ctx,
                                    embed = current.embed,
                                    onCreated = { webView = it },
                                    onProgress = { progress = it },
                                    onLoaded = { loaded = true },
                                    onFail = { failed = true },
                                    onExternal = { url -> context.openBrowser(url) }
                                )
                            }
                        )
                    }
                } else if (current == null) {
                    CircularProgressIndicator(
                        color = Orange,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                if (progress in 1..99 && !failed) {
                    LinearProgressIndicator(
                        progress = { progress / 100f },
                        color = Orange,
                        trackColor = Color.Transparent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter)
                    )
                }

                if (!failed) {
                    IconButton(
                        onClick = { fullscreen = !fullscreen },
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Icon(
                            imageVector = if (fullscreen) Icons.Filled.FullscreenExit
                            else Icons.Filled.Fullscreen,
                            contentDescription = if (fullscreen) "Exit fullscreen" else "Fullscreen",
                            tint = Color.White
                        )
                    }
                }
            }

            if (!fullscreen && !failed) {
                val current = video
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, end = 8.dp, top = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = current?.title ?: "Loading…",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        if (current != null) {
                            Text(
                                text = "${current.source} · ${formatDuration(current.duration)} · " +
                                    "${current.category} · ${formatViews(current.views)} views · " +
                                    formatDate(current.date),
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                    IconButton(onClick = { vm.toggleFavorite() }) {
                        Icon(
                            imageVector = if (favorite) Icons.Filled.Favorite
                            else Icons.Filled.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (favorite) Orange else TextSecondary
                        )
                    }
                }

                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(onClick = {
                        vm.markDead()
                        failed = true
                    }) { Text("Report dead link") }
                    OutlinedButton(onClick = {
                        vm.hide()
                        onBack()
                    }) { Text("Hide") }
                    OutlinedButton(onClick = {
                        current?.let { context.openBrowser(it.watchUrl) }
                    }) { Text("Browser") }
                }

                if (autoplayRelated && related.isNotEmpty()) {
                    Text(
                        text = "Related",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(start = 14.dp, top = 12.dp)
                    )
                    LazyRow(
                        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(related, key = { it.id }) { item ->
                            RelatedCard(item = item, onClick = { onOpenVideo(item) })
                        }
                    }
                } else {
                    Box(modifier = Modifier.padding(20.dp))
                }
            }
        }

        val current = video
        if (failed && current != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(NearBlack)
                    .padding(28.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = current.title,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "This scene can't play inside the app.",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 10.dp, bottom = 18.dp)
                    )
                    Text(
                        text = "${current.source} · ${formatDuration(current.duration)} · ${current.category}",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 18.dp)
                    )
                    Button(
                        onClick = { context.openBrowser(current.watchUrl) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Open in browser")
                    }
                    TextButton(onClick = {
                        failed = false
                        reloadKey += 1
                    }) {
                        Text("Try again in the app")
                    }
                    TextButton(onClick = onBack) {
                        Text("Back")
                    }
                }
            }
        }
    }
}

@Composable
private fun RelatedCard(item: VideoItem, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(160.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(6.dp))
                .background(SurfaceHigher)
        ) {
            AsyncImage(
                model = item.thumbnailUrl,
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Text(
                text = formatDuration(item.duration),
                color = Color.White,
                fontSize = 10.sp,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color.Black.copy(alpha = 0.7f))
                    .padding(horizontal = 4.dp, vertical = 1.dp)
            )
        }
        Text(
            text = item.title,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 12.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@SuppressLint("SetJavaScriptEnabled")
private fun buildWebView(
    context: Context,
    embed: String,
    onCreated: (WebView) -> Unit,
    onProgress: (Int) -> Unit,
    onLoaded: () -> Unit,
    onFail: () -> Unit,
    onExternal: (String) -> Unit
): WebView {
    val view = WebView(context)
    view.setBackgroundColor(AndroidColor.BLACK)

    view.settings.apply {
        javaScriptEnabled = true
        domStorageEnabled = true
        databaseEnabled = true
        mediaPlaybackRequiresUserGesture = false
        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        loadWithOverviewMode = true
        useWideViewPort = true
        builtInZoomControls = false
        displayZoomControls = false
        setSupportMultipleWindows(false)
        javaScriptCanOpenWindowsAutomatically = true
        userAgentString = MOBILE_UA
        allowFileAccess = true
        cacheMode = WebSettings.LOAD_DEFAULT
    }

    CookieManager.getInstance().apply {
        setAcceptCookie(true)
        setAcceptThirdPartyCookies(view, true)
    }

    onCreated(view)

    view.webChromeClient = object : WebChromeClient() {
        override fun onProgressChanged(v: WebView?, newProgress: Int) {
            onProgress(newProgress)
            if (newProgress >= 90) onLoaded()
        }

        override fun onCreateWindow(
            v: WebView?,
            isDialog: Boolean,
            isUserGesture: Boolean,
            resultMsg: Message?
        ): Boolean = false
    }

    view.webViewClient = object : WebViewClient() {
        override fun shouldOverrideUrlLoading(
            v: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            val url = request?.url?.toString() ?: return false
            return handleUrl(url, onExternal)
        }

        @Deprecated("Legacy override")
        override fun shouldOverrideUrlLoading(v: WebView?, url: String?): Boolean {
            val target = url ?: return false
            return handleUrl(target, onExternal)
        }

        override fun onPageFinished(v: WebView?, url: String?) {
            super.onPageFinished(v, url)
            v?.evaluateJavascript(CLEANUP_JS, null)
            onLoaded()
        }

        override fun onReceivedError(
            v: WebView?,
            request: WebResourceRequest?,
            error: WebResourceError?
        ) {
            if (request?.isForMainFrame == true) onFail()
        }
    }

    view.loadUrl(embed)
    return view
}

private fun handleUrl(url: String, onExternal: (String) -> Unit): Boolean {
    if (url.startsWith("intent:") || url.startsWith("market:") ||
        url.startsWith("tg:") || url.startsWith("mailto:")
    ) {
        onExternal(url)
        return true
    }
    if (!url.startsWith("http")) return false
    if (url.contains(".onion")) {
        onExternal(url)
        return true
    }
    val host = runCatching { java.net.URI(url).host.orEmpty() }.getOrDefault("")
    val internal = INTERNAL_HOSTS.any { host.endsWith(it) }
    if (internal) return false
    onExternal(url)
    return true
}
