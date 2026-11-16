=== ABOUT: Fixed a missing import so the dark splash background actually draws ===
package com.aibuilt.porn.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aibuilt.porn.ui.theme.NearBlack
import com.aibuilt.porn.ui.theme.Orange
import com.aibuilt.porn.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onDone: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(1100)
        onDone()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NearBlack),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = null,
                tint = Orange,
                modifier = Modifier.size(64.dp)
            )
            Text(
                text = "Porn",
                color = Orange,
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp
            )
            Text(
                text = "real scenes, in the app",
                color = TextSecondary,
                fontSize = 12.sp
            )
        }
    }
}
