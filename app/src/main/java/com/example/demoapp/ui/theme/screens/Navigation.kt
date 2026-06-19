package com.example.demoapp.ui.theme.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import com.example.demoapp.data.local.Session
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
    const val CHATS = "chats"
    const val CHAT = "chat/{chatId}/{otherEmail}"

    fun posts(channelId: Long) = "posts/$channelId"
    fun comments(postId: Long) = "comments/$postId"
    fun wallComments(wallPostId: Long) = "wall_comments/$wallPostId"
    fun userProfile(email: String) =
        "profile/user/" + java.net.URLEncoder.encode(email, "UTF-8")
    fun chat(chatId: String, otherEmail: String) = "chat/$chatId/$otherEmail"
}

// Элементы нижней навигации
sealed class BottomNavItem(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Wall     : BottomNavItem(Routes.WALL,       "Стена",   Icons.Default.Home)
    object Channels : BottomNavItem(Routes.CHANNELS,   "Каналы",  Icons.Default.List)
    object Chats    : BottomNavItem(Routes.CHATS,      "Чаты",    Icons.AutoMirrored.Filled.Chat)
    object Profile  : BottomNavItem(Routes.MY_PROFILE, "Профиль", Icons.Default.AccountCircle)
}

@Composable
fun AppNavigation(startLoggedIn: Boolean = false) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Маршруты где показывается нижняя навигация
    val bottomNavRoutes = listOf(Routes.WALL, Routes.CHANNELS, Routes.CHATS, Routes.MY_PROFILE)
    val showBottomBar = currentDestination?.route in bottomNavRoutes

    val bottomItems = listOf(BottomNavItem.Wall, BottomNavItem.Channels, BottomNavItem.Chats, BottomNavItem.Profile)

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
            startDestination = if (startLoggedIn) Routes.WALL else Routes.LOGIN,
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
                    onBack = { navController.popBackStack() },
                    onLogout = {
                        navController.navigate(Routes.LOGIN) {
                            // Чистим весь back stack, чтобы нельзя было вернуться назад
                            popUpTo(navController.graph.id) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
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
                val rawEmail = backStack.arguments?.getString("email") ?: return@composable
                val email = java.net.URLDecoder.decode(rawEmail, "UTF-8")
                ProfileScreen(
                    email = email,
                    onBack = { navController.popBackStack() },
                    onWriteMessage = { otherEmail ->
                        val enc = java.net.URLEncoder.encode(otherEmail, "UTF-8")
                        navController.navigate("chats_open/$enc")
                    }
                )
            }

            // Список диалогов
            composable(Routes.CHATS) {
                ChatListScreen(
                    myEmail = Session.myEmail ?: "",
                    onOpenChat = { chatId, otherEmail ->
                        navController.navigate(Routes.chat(chatId, otherEmail))
                    }
                )
            }

            // Открытие диалога по email (с экрана профиля): создаём/находим и переходим
            composable("chats_open/{otherEmail}") { backStack ->
                val rawOther = backStack.arguments?.getString("otherEmail") ?: return@composable
                val otherEmail = java.net.URLDecoder.decode(rawOther, "UTF-8")
                val chatVm: com.example.demoapp.ui.theme.viewmodel.ChatViewModel =
                    androidx.lifecycle.viewmodel.compose.viewModel()
                val openError by chatVm.error.collectAsState()
                LaunchedEffect(otherEmail) {
                    chatVm.openConversationByEmail(
                        otherEmail = otherEmail,
                        myEmail = Session.myEmail ?: ""
                    ) { chatId ->
                        navController.navigate(Routes.chat(chatId, otherEmail)) {
                            popUpTo("chats_open/$rawOther") { inclusive = true }
                        }
                    }
                }
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    val err = openError
                    if (err == null) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(Modifier.height(12.dp))
                            Text("Открываем диалог…")
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Text(err, color = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.height(16.dp))
                            Button(onClick = {
                                chatVm.clearError()
                                navController.popBackStack()
                            }) { Text("Назад") }
                        }
                    }
                }
            }

            // Экран переписки
            composable(Routes.CHAT) { backStack ->
                val chatId = backStack.arguments?.getString("chatId") ?: return@composable
                val otherEmail = backStack.arguments?.getString("otherEmail") ?: ""
                ChatScreen(
                    chatId = chatId,
                    otherEmail = otherEmail,
                    myEmail = Session.myEmail ?: "",
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}