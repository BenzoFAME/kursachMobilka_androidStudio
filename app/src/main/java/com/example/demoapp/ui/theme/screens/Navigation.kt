package com.example.demoapp.ui.theme.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val WALL = "wall"
    const val CHANNELS = "channels"
    const val MY_PROFILE = "profile/me"
    const val POSTS = "posts/{channelId}"
    const val COMMENTS = "comments/{postId}"
    const val WALL_COMMENTS = "wall_comments/{wallPostId}"
    const val USER_PROFILE = "profile/user/{email}"

    fun posts(channelId: Long) = "posts/$channelId"
    fun comments(postId: Long) = "comments/$postId"
    fun wallComments(wallPostId: Long) = "wall_comments/$wallPostId"
    fun userProfile(email: String) = "profile/user/$email"
}

// Элементы нижней навигации
sealed class BottomNavItem(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Wall     : BottomNavItem(Routes.WALL,       "Стена",   Icons.Default.Home)
    object Channels : BottomNavItem(Routes.CHANNELS,   "Каналы",  Icons.Default.List)
    object Profile  : BottomNavItem(Routes.MY_PROFILE, "Профиль", Icons.Default.AccountCircle)
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Маршруты где показывается нижняя навигация
    val bottomNavRoutes = listOf(Routes.WALL, Routes.CHANNELS, Routes.MY_PROFILE)
    val showBottomBar = currentDestination?.route in bottomNavRoutes

    val bottomItems = listOf(BottomNavItem.Wall, BottomNavItem.Channels, BottomNavItem.Profile)

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomItems.forEach { item ->
                        NavigationBarItem(
                            selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.LOGIN,
            modifier = androidx.compose.ui.Modifier.padding(padding)
        ) {
            composable(Routes.LOGIN) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Routes.WALL) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = { navController.navigate(Routes.REGISTER) }
                )
            }

            composable(Routes.REGISTER) {
                RegisterScreen(
                    onRegisterSuccess = {
                        navController.navigate(Routes.WALL) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = { navController.popBackStack() }
                )
            }

            composable(Routes.WALL) {
                WallScreen(
                    onOpenComments = { navController.navigate(Routes.wallComments(it)) },
                    onOpenProfile = { navController.navigate(Routes.userProfile(it)) }
                )
            }

            composable(Routes.CHANNELS) {
                ChannelsScreen(
                    onOpenChannel = { navController.navigate(Routes.posts(it)) }
                )
            }

            composable(Routes.MY_PROFILE) {
                ProfileScreen(
                    email = null,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.POSTS) { backStack ->
                val channelId = backStack.arguments?.getString("channelId")?.toLong() ?: return@composable
                PostsScreen(
                    channelId = channelId,
                    onOpenPost = { navController.navigate(Routes.comments(it)) },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.COMMENTS) { backStack ->
                val postId = backStack.arguments?.getString("postId")?.toLong() ?: return@composable
                CommentsScreen(
                    postId = postId,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.WALL_COMMENTS) { backStack ->
                val wallPostId = backStack.arguments?.getString("wallPostId")?.toLong() ?: return@composable
                WallCommentsScreen(
                    wallPostId = wallPostId,
                    onBack = { navController.popBackStack() },
                    onOpenProfile = { navController.navigate(Routes.userProfile(it)) }
                )
            }

            composable(Routes.USER_PROFILE) { backStack ->
                val email = backStack.arguments?.getString("email") ?: return@composable
                ProfileScreen(
                    email = email,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}