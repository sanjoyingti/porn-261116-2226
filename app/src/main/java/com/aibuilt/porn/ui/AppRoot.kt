=== ABOUT: Fixed the thumbnail-cache switch so it actually reaches the video cards ===
package com.aibuilt.porn.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil3.SingletonImageLoader
import com.aibuilt.porn.AppContainer
import com.aibuilt.porn.data.AppSettings
import com.aibuilt.porn.ui.components.OfflineBanner
import com.aibuilt.porn.ui.components.PornBottomBar
import com.aibuilt.porn.ui.screens.CategoriesScreen
import com.aibuilt.porn.ui.screens.CategoryScreen
import com.aibuilt.porn.ui.screens.ChildScreen
import com.aibuilt.porn.ui.screens.FavoritesScreen
import com.aibuilt.porn.ui.screens.HomeScreen
import com.aibuilt.porn.ui.screens.PlayerScreen
import com.aibuilt.porn.ui.screens.SearchScreen
import com.aibuilt.porn.ui.screens.SettingsScreen
import com.aibuilt.porn.ui.screens.SettingsViewModel
import com.aibuilt.porn.ui.screens.SplashScreen
import com.aibuilt.porn.ui.screens.TorScreen
import com.aibuilt.porn.ui.theme.NearBlack
import kotlinx.coroutines.launch

object Routes {
    const val SPLASH = "splash"
    const val HOME = "home"
    const val CATEGORIES = "categories"
    const val CATEGORY = "category/{slug}"
    const val SEARCH = "search"
    const val CHILD = "child"
    const val TOR = "tor"
    const val FAVORITES = "favorites"
    const val SETTINGS = "settings"
    const val PLAYER = "player/{id}"

    fun category(slug: String) = "category/$slug"
    fun player(id: String) = "player/$id"
}

@Composable
fun AppRoot(container: AppContainer) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val route = backStackEntry?.destination?.route
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val settings by container.settings.settings
        .collectAsStateWithLifecycle(initialValue = AppSettings())
    val online by container.networkMonitor.isOnline
        .collectAsStateWithLifecycle(initialValue = true)

    LaunchedEffect(Unit) { container.catalogRepository.ensureSeeded() }

    val showBottomBar = route == Routes.HOME || route == Routes.CATEGORIES ||
        route == Routes.CHILD || route == Routes.TOR

    CompositionLocalProvider(LocalCacheThumbnails provides settings.cacheThumbnails) {
        Column(modifier = Modifier.fillMaxSize().background(NearBlack)) {
            if (!online && route != null && route != Routes.SPLASH) {
                OfflineBanner()
            }
            Box(modifier = Modifier.weight(1f)) {
                NavHost(
                    navController = navController,
                    startDestination = Routes.SPLASH,
                    modifier = Modifier.fillMaxSize()
                ) {
                    composable(Routes.SPLASH) {
                        SplashScreen(onDone = {
                            navController.navigate(Routes.HOME) {
                                popUpTo(Routes.SPLASH) { inclusive = true }
                            }
                        })
                    }

                    composable(Routes.HOME) {
                        HomeScreen(
                            vm = viewModel(factory = VmFactory.home(container)),
                            onOpenVideo = { navController.navigate(Routes.player(it.id)) },
                            onSearch = { navController.navigate(Routes.SEARCH) },
                            onFavorites = { navController.navigate(Routes.FAVORITES) },
                            onSettings = { navController.navigate(Routes.SETTINGS) }
                        )
                    }

                    composable(Routes.CATEGORIES) {
                        CategoriesScreen(
                            vm = viewModel(factory = VmFactory.categories(container)),
                            onOpenCategory = { slug -> navController.navigate(Routes.category(slug)) },
                            onSearch = { navController.navigate(Routes.SEARCH) },
                            onFavorites = { navController.navigate(Routes.FAVORITES) }
                        )
                    }

                    composable(
                        route = Routes.CATEGORY,
                        arguments = listOf(navArgument("slug") { type = NavType.StringType })
                    ) {
                        CategoryScreen(
                            vm = viewModel(factory = VmFactory.category(container)),
                            onBack = { navController.popBackStack() },
                            onOpenVideo = { navController.navigate(Routes.player(it.id)) }
                        )
                    }

                    composable(Routes.SEARCH) {
                        SearchScreen(
                            vm = viewModel(factory = VmFactory.search(container)),
                            onBack = { navController.popBackStack() },
                            onOpenVideo = { navController.navigate(Routes.player(it.id)) }
                        )
                    }

                    composable(Routes.CHILD) {
                        ChildScreen(
                            vm = viewModel(factory = VmFactory.child(container)),
                            onOpenVideo = { navController.navigate(Routes.player(it.id)) },
                            onFavorites = { navController.navigate(Routes.FAVORITES) }
                        )
                    }

                    composable(Routes.TOR) {
                        TorScreen(vm = viewModel(factory = VmFactory.tor(container)))
                    }

                    composable(Routes.FAVORITES) {
                        FavoritesScreen(
                            vm = viewModel(factory = VmFactory.favorites(container)),
                            onBack = { navController.popBackStack() },
                            onOpenVideo = { navController.navigate(Routes.player(it.id)) }
                        )
                    }

                    composable(Routes.SETTINGS) {
                        val ctx = LocalContext.current
                        val vm: SettingsViewModel = viewModel(
                            factory = VmFactory.settings(container) {
                                val loader = SingletonImageLoader.get(ctx)
                                scope.launch {
                                    loader.memoryCache?.clear()
                                    loader.diskCache?.clear()
                                }
                            }
                        )
                        SettingsScreen(vm = vm, onBack = { navController.popBackStack() })
                    }

                    composable(
                        route = Routes.PLAYER,
                        arguments = listOf(navArgument("id") { type = NavType.StringType })
                    ) {
                        PlayerScreen(
                            vm = viewModel(factory = VmFactory.player(container)),
                            autoplayRelated = settings.autoplayRelated,
                            onBack = { navController.popBackStack() },
                            onOpenVideo = { item ->
                                navController.navigate(Routes.player(item.id)) {
                                    popUpTo(Routes.PLAYER) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                }
            }
            if (showBottomBar) {
                PornBottomBar(
                    currentRoute = route,
                    onSelect = { target ->
                        navController.navigate(target) {
                            popUpTo(Routes.HOME) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    }

    if (!settings.warningSeen && route != null && route != Routes.SPLASH) {
        AlertDialog(
            onDismissRequest = { scope.launch { container.settings.setWarningSeen() } },
            title = { Text("This app contains adult content") },
            text = {
                Text(
                    "Everything here is 18+ except the Child tab, which lists real girls " +
                        "roughly 10–15 in real scenes. Tap Continue to enter."
                )
            },
            confirmButton = {
                TextButton(onClick = { scope.launch { container.settings.setWarningSeen() } }) {
                    Text("Continue")
                }
            }
        )
    }
}
