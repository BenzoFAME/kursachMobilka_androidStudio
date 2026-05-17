package com.example.demoapp.ui.theme.screens

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val CHANNELS = "channels"
    const val POSTS = "posts/{channelId}"
    const val COMMENTS = "comments/{postId}"

    fun posts(channelId: Long) = "posts/$channelId"
    fun comments(postId: Long) = "comments/$postId"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.LOGIN) {

        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.CHANNELS) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate(Routes.REGISTER) }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Routes.CHANNELS) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        composable(Routes.CHANNELS) {
            ChannelsScreen(
                onOpenChannel = { channelId ->
                    navController.navigate(Routes.posts(channelId))
                }
            )
        }

        composable(Routes.POSTS) { backStack ->
            val channelId = backStack.arguments?.getString("channelId")?.toLong() ?: return@composable
            PostsScreen(
                channelId = channelId,
                onOpenPost = { postId -> navController.navigate(Routes.comments(postId)) },
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
    }
}