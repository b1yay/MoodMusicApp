package com.example.moodmusicapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.moodmusicapp.ui.*
import com.example.moodmusicapp.ui.theme.AppBackground
import com.example.moodmusicapp.ui.theme.ArbitifyTheme
import com.example.moodmusicapp.ui.theme.BrandPurple

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

    val authViewModel: AuthViewModel = viewModel()
    val authState by authViewModel.authState.collectAsState()
    val jamendoViewModel: JamendoViewModel = viewModel(factory = JamendoViewModel.Factory())
    val favouritesViewModel: FavouritesViewModel = viewModel(factory = FavouritesViewModel.Companion.Factory())

    val showBottomBar = currentRoute != Screen.Login.route && 
                       currentRoute != Screen.SignUp.route && 
                       currentRoute != Screen.NowPlaying.route && 
                       authState is AuthState.Authenticated

    if (authState is AuthState.Loading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppBackground),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = BrandPurple)
        }
        return
    }

    val startDestination = if (authState is AuthState.Authenticated) Screen.Home.route else Screen.Login.route

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
            startDestination = startDestination,
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
                SignInScreen(navController, authViewModel)
            }

            composable(
                route = Screen.SignUp.route,
                enterTransition = { EnterTransition.None },
                exitTransition = { fadeOut() }
            ) {
                SignUpScreen(navController, authViewModel)
            }

            composable(route = Screen.Home.route) {
                HomeScreen(
                    navController = navController,
                    authViewModel = authViewModel,
                    favouritesViewModel = favouritesViewModel,
                    onLogout = {
                        authViewModel.signOut()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onMoodClick = { mood ->
                        navController.navigate(Screen.Playlist.createRoute(mood))
                    }
                )
            }

            composable(
                route = Screen.Playlist.route,
                arguments = listOf(navArgument("moodName") { type = NavType.StringType })
            ) { backStackEntry ->
                val mood = backStackEntry.arguments?.getString("moodName") ?: "Happy"
                PlaylistScreen(
                    moodName = mood,
                    onBack = { navController.popBackStack() },
                    onNavigateToNowPlaying = { navController.navigate(Screen.NowPlaying.route) },
                    jamendoViewModel = jamendoViewModel,
                    favouritesViewModel = favouritesViewModel
                )
            }

            composable(route = Screen.Favourites.route) {
                FavouritesScreen(
                    favouritesViewModel = favouritesViewModel,
                    onNavigateToNowPlaying = {
                        navController.navigate(Screen.NowPlaying.route)
                    }
                )
            }

            composable(
                route = Screen.NowPlaying.route,
                enterTransition = { slideInVertically(initialOffsetY = { it }) },
                exitTransition = { slideOutVertically(targetOffsetY = { it }) }
            ) {
                NowPlayingScreen(
                    favouritesViewModel = favouritesViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(route = Screen.Discover.route) {
                DiscoverScreen(
                    navController = navController,
                    jamendoViewModel = jamendoViewModel,
                    favouritesViewModel = favouritesViewModel
                )
            }
            composable(route = Screen.Profile.route) {
                ProfileScreen(
                    navController = navController,
                    authViewModel = authViewModel,
                    favouritesViewModel = favouritesViewModel
                )
            }
            composable(route = "editprofile") {
                EditProfileScreen(navController = navController, authViewModel = authViewModel)
            }
            composable(route = "privacy") {
                PrivacyScreen(
                    navController = navController,
                    authViewModel = authViewModel,
                    favouritesViewModel = favouritesViewModel
                )
            }
            composable(route = "notifications") {
                NotificationScreen(navController = navController)
            }
            composable(route = "about") {
                AboutScreen(navController = navController)
            }
        }
    }
}
