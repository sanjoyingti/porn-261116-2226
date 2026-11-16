=== ABOUT: The in-app player: finishes the WebView setup, strips floating ads and overlays, and keeps fullscreen and back handling working ===
package com.aibuilt.porn.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Color as AndroidColor
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
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

    LaunchedEffect(video?.id, failed) {
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
                    key(current.id) {
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
                    Button(onClick = { context.openBrowser(current.watchUrl) }) {
                        Text("Open in browser")
                    }
                    TextButton(onClick = {
                        failed = false
                        progress = 0
                        loaded = false
                    }) { Text("Try again") }
                    TextButton(onClick = onBack) { Text("Back") }
                    Text(
                        text = "The link was marked as dead. Hide the entry to drop it from the feed.",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun RelatedCard(item: VideoItem, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(170.dp)
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
        Text(text = item.source, color = TextSecondary, fontSize = 10.sp)
    }
}

private val INTERNAL_HOSTS = listOf(
    "pornhub.com", "phncdn.com", "xhamster", "eporner", "spankbang", "txxx", "xnxx",
    "hqporner", "youporn", "youtube.com", "youtu.be", "ytimg.com", "dailymotion", "ok.ru",
    "vk.com", "archive.org", "about:blank"
)

private fun isInternal(url: String): Boolean =
    INTERNAL_HOSTS.any { url.contains(it, ignoreCase = true) }

private const val MOBILE_UA =
    "Mozilla/5.0 (Linux; Android 14; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) " +
        "Chrome/124.0.0.0 Mobile Safari/537.36"

private const val CLEANER_JS = """
(function() {
  if (window.__paCleaner) { return; }
  window.__paCleaner = true;
  var css = 'html,body{background:#000 !important;margin:0 !important;}' +
            '::-webkit-scrollbar{width:0;height:0;}';
  var style = document.createElement('style');
  style.type = 'text/css';
  style.appendChild(document.createTextNode(css));
  (document.head || document.documentElement).appendChild(style);

  var bad = ['advert','adbox','ad-container','ad_container','-ads','_ads','ads-','ads_',
             'adbanner','ad_banner','adunit','ad-unit','sponsor','overlay','popup','pop-up',
             'banner','subscribe','downloadapp','download-app','appbanner','app-banner',
             'upsell','promo','consent','gdpr','cookie','interstitial','notif','smartbar',
             'paywall','teaser','sticky'];

  function hide(el) {
    if (!el) { return; }
    el.setAttribute('data-pa', '1');
    el.style.setProperty('display', 'none', 'important');
  }

  function signature(el) {
    var cls = (typeof el.className === 'string') ? el.className : '';
    return ((el.id || '') + ' ' + cls + ' ' + (el.getAttribute('data-name') || '') + ' ' +
            (el.getAttribute('aria-label') || '')).toLowerCase();
  }

  function scan() {
    if (!document.body) { return; }
    var nodes = document.body.querySelectorAll('div,aside,section,ins,iframe,span,a');
    for (var i = 0; i < nodes.length; i++) {
      var el = nodes[i];
      if (el.getAttribute('data-pa') === '1') { continue; }
      if (el.tagName === 'VIDEO') { continue; }
      if (el.querySelector && el.querySelector('video')) { continue; }
      var rect = el.getBoundingClientRect();
      if (el.tagName === 'IFRAME') {
        if (rect.width > 320 && rect.height > 180) { continue; }
        hide(el);
        continue;
      }
      var sig = signature(el);
      var named = false;
      for (var j = 0; j < bad.length; j++) {
        if (sig.indexOf(bad[j]) !== -1) { named = true; break; }
      }
      var st = window.getComputedStyle(el);
      var floating = (st.position === 'fixed' || st.position === 'sticky');
      var z = parseInt(st.zIndex || '0', 10) || 0;
      var huge = rect.width > window.innerWidth * 0.8 && rect.height > window.innerHeight * 0.4;
      if (named || (floating && z > 900) || (floating && huge)) {
        hide(el);
      }
    }
  }

  scan();
  var runs = 0;
  var timer = setInterval(function() {
    scan();
    runs++;
    if (runs > 30) { clearInterval(timer); }
  }, 700);
})();
""";

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
    val web = WebView(context)
    web.setBackgroundColor(AndroidColor.BLACK)
    web.settings.apply {
        javaScriptEnabled = true
        domStorageEnabled = true
        databaseEnabled = true
        mediaPlaybackRequiresUserGesture = false
        loadWithOverviewMode = true
        useWideViewPort = true
        builtInZoomControls = false
        displayZoomControls = false
        mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        javaScriptCanOpenWindowsAutomatically = false
        setSupportMultipleWindows(false)
        userAgentString = MOBILE_UA
        cacheMode = android.webkit.WebSettings.LOAD_DEFAULT
    }
    CookieManager.getInstance().apply {
        setAcceptCookie(true)
        setAcceptThirdPartyCookies(web, true)
    }

    web.webChromeClient = object : WebChromeClient() {
        override fun onProgressChanged(view: WebView?, newProgress: Int) {
            onProgress(newProgress)
        }
    }

    web.webViewClient = object : WebViewClient() {
        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            val url = request?.url?.toString() ?: return false
            return if (url.startsWith("http") && !isInternal(url)) {
                onExternal(url)
                true
            } else {
                false
            }
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            onLoaded()
            view?.evaluateJavascript(CLEANER_JS, null)
        }

        override fun onReceivedError(
            view: WebView?,
            request: WebResourceRequest?,
            error: WebResourceError?
        ) {
            super.onReceivedError(view, request, error)
            if (request?.isForMainFrame == true) onFail()
        }
    }

    web.loadUrl(embed)
    onCreated(web)
    return web
}
