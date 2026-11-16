package com.aibuilt.porn.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val PornColors = darkColorScheme(
    primary = Orange,
    onPrimary = NearBlack,
    primaryContainer = OrangeDark,
    onPrimaryContainer = TextPrimary,
    secondary = Orange,
    onSecondary = NearBlack,
    background = NearBlack,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceHigher,
    onSurfaceVariant = TextSecondary,
    outline = DividerGrey,
    error = ErrorRed,
    onError = NearBlack
)

@Composable
fun PornTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = NearBlack.toArgb()
            window.navigationBarColor = NearBlack.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }
    MaterialTheme(
        colorScheme = PornColors,
        typography = AppTypography,
        content = content
    )
}
