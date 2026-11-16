package com.aibuilt.porn.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aibuilt.porn.ui.Routes
import com.aibuilt.porn.ui.theme.DividerGrey
import com.aibuilt.porn.ui.theme.NearBlack
import com.aibuilt.porn.ui.theme.Orange
import com.aibuilt.porn.ui.theme.SurfaceDark
import com.aibuilt.porn.ui.theme.TextSecondary

private data class BottomItem(val route: String, val label: String, val icon: ImageVector)

private val bottomItems = listOf(
    BottomItem(Routes.HOME, "Home", Icons.Filled.Home),
    BottomItem(Routes.CATEGORIES, "Categories", Icons.Filled.GridView),
    BottomItem(Routes.CHILD, "Child", Icons.Filled.ChildCare),
    BottomItem(Routes.TOR, "Tor", Icons.Filled.Security)
)

@Composable
fun PornBottomBar(currentRoute: String?, onSelect: (String) -> Unit) {
    NavigationBar(containerColor = SurfaceDark) {
        bottomItems.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick = { if (!selected) onSelect(item.route) },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label, fontSize = 11.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Orange,
                    selectedTextColor = Orange,
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextSecondary,
                    indicatorColor = NearBlack
                )
            )
        }
    }
}

@Composable
fun OfflineBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DividerGrey)
            .statusBarsPadding()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Offline — showing cached content only",
            color = Orange,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
