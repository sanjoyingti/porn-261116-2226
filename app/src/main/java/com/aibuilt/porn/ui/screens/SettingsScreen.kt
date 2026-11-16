package com.aibuilt.porn.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aibuilt.porn.ui.theme.DividerGrey
import com.aibuilt.porn.ui.theme.NearBlack
import com.aibuilt.porn.ui.theme.Orange
import com.aibuilt.porn.ui.theme.SurfaceDark
import com.aibuilt.porn.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(vm: SettingsViewModel, onBack: () -> Unit) {
    val settings by vm.state.collectAsStateWithLifecycle()
    val hidden by vm.hiddenCount.collectAsStateWithLifecycle()
    val updating by vm.updating.collectAsStateWithLifecycle()
    val message by vm.message.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    var urlDraft by remember(settings.refreshUrl) { mutableStateOf(settings.refreshUrl) }

    LaunchedEffect(message) {
        message?.let {
            snackbar.showSnackbar(it)
            vm.consumeMessage()
        }
    }

    val version = remember {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull() ?: "1.0"
    }

    Scaffold(
        containerColor = NearBlack,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NearBlack),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = { Text("Settings", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            SettingSwitch("Autoplay related videos", settings.autoplayRelated, vm::setAutoplay)
            SettingSwitch("Cache thumbnails on disk", settings.cacheThumbnails, vm::setCache)
            SettingSwitch(
                "Adult content warning on launch",
                !settings.warningSeen,
                { on -> if (on) vm.setWarningOn() else vm.setWarningOff() }
            )

            HorizontalDivider(color = DividerGrey, modifier = Modifier.padding(vertical = 10.dp))

            Text("Catalog", fontWeight = FontWeight.Bold)
            Text(
                "Refresh URL — the app downloads a newer JSON catalog from here.",
                color = TextSecondary,
                fontSize = 12.sp
            )
            OutlinedTextField(
                value = urlDraft,
                onValueChange = { urlDraft = it },
                label = { Text("Refresh URL") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { vm.setRefreshUrl(urlDraft) }) { Text("Save URL") }
                Button(onClick = vm::updateCatalog, enabled = !updating) {
                    if (updating) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(2.dp),
                            strokeWidth = 2.dp,
                            color = NearBlack
                        )
                    } else {
                        Text("Update catalog")
                    }
                }
            }

            HorizontalDivider(color = DividerGrey, modifier = Modifier.padding(vertical = 10.dp))

            Button(onClick = vm::clearCache, modifier = Modifier.fillMaxWidth()) {
                Text("Clear thumbnail cache")
            }
            Button(onClick = vm::clearHistory, modifier = Modifier.fillMaxWidth()) {
                Text("Clear search history")
            }
            Button(
                onClick = vm::restoreHidden,
                enabled = hidden > 0,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Restore $hidden hidden videos")
            }

            HorizontalDivider(color = DividerGrey, modifier = Modifier.padding(vertical = 10.dp))

            Text("About", fontWeight = FontWeight.Bold)
            Text("Version $version", color = TextSecondary, fontSize = 12.sp)
            Text(
                "Videos are not hosted by this app. Each scene plays through the source " +
                    "site's own embedded player — Pornhub, xHamster, Eporner, SpankBang, TXXX, " +
                    "XNXX, HQporner and YouPorn. Full scenes only; no trailers or reviews. " +
                    "Thumbnails are hotlinked or cached. The Child tab uses YouTube, OK.ru, " +
                    "VK and archive.org uploads because full scenes survive there.",
                color = TextSecondary,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )
        }
    }
}

@Composable
private fun SettingSwitch(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, modifier = Modifier.padding(end = 12.dp))
        Switch(checked = checked, onCheckedChange = onChange)
    }
}
