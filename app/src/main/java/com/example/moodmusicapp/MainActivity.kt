package com.example.moodmusicapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.moodmusicapp.ui.*
import com.example.moodmusicapp.ui.theme.ArbitifyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ArbitifyTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute != Screen.Login.route && currentRoute != Screen.NowPlaying.route

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                Column {
                    MiniPlayer(onExpand = { navController.navigate(Screen.NowPlaying.route) })
                    BottomNavBar(navController = navController)
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Login.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) },
            popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) },
            popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) }
        ) {
            composable(
                route = Screen.Login.route,
                enterTransition = { EnterTransition.None },
                exitTransition = { fadeOut() }
            ) {
                LoginScreen(onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                })
            }

            composable(route = Screen.Home.route) {
                HomeScreen(onMoodClick = { mood ->
                    navController.navigate(Screen.Playlist.createRoute(mood))
                })
            }

            composable(
                route = Screen.Playlist.route,
                arguments = listOf(navArgument("moodName") { type = NavType.StringType })
            ) { backStackEntry ->
                val mood = backStackEntry.arguments?.getString("moodName") ?: "Happy"
                PlaylistScreen(
                    moodName = mood,
                    onBack = { navController.popBackStack() },
                    onNavigateToNowPlaying = { navController.navigate(Screen.NowPlaying.route) }
                )
            }

            composable(route = Screen.Favourites.route) {
                FavouritesScreen(onNavigateToNowPlaying = {
                    navController.navigate(Screen.NowPlaying.route)
                })
            }

            composable(
                route = Screen.NowPlaying.route,
                enterTransition = { slideInVertically(initialOffsetY = { it }) },
                exitTransition = { slideOutVertically(targetOffsetY = { it }) }
            ) {
                NowPlayingScreen(onBack = { navController.popBackStack() })
            }

            composable(route = "discover") { PlaceholderScreen("Discover") }
            composable(route = "profile") { PlaceholderScreen("Profile") }
        }
    }
}
