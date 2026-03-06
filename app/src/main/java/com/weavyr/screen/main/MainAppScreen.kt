package com.weavyr.screen.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.*
import com.weavyr.screen.components.FloatingBottomNavBar
import com.weavyr.ui.theme.WeavyrBackground
import androidx.lifecycle.viewmodel.compose.viewModel
import com.weavyr.viewmodel.MainViewModel
import com.weavyr.screen.main.SettingsScreen

@Composable
fun MainAppScreen(
    onLogout: () -> Unit
)  {

    val navController = rememberNavController()
    val mainViewModel: MainViewModel = viewModel()

    Scaffold(
        containerColor = WeavyrBackground,
        bottomBar = {
            FloatingBottomNavBar(navController)
        }
    ) { padding ->

        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(padding)
        ) {

            composable("articles") { ArticlesScreen() }

            composable("home") {
                HomeScreen(viewModel=mainViewModel)
            }

            composable("myprofile") {
                MyProfile(
                    viewModel = mainViewModel,
                    navController = navController
                )
            }
            composable("settings") {
                SettingsScreen(
                    navController = navController,
                    onLogout = onLogout
                )
            }

            composable("bookmarks") {
                ProfileListsScreen(
                    title = "Bookmarked Profiles",
                    profiles = mainViewModel.bookmarkedProfiles
                )
            }

            composable("rejected") {
                ProfileListsScreen(
                    title = "Rejected Profiles",
                    profiles = mainViewModel.rejectedProfiles
                )
            }

            composable("requests") {
                ProfileListsScreen(
                    title = "Requests Sent",
                    profiles = mainViewModel.connectionRequests
                )
            }

            composable("edit_profile") {
                EditProfileScreen(viewModel = mainViewModel, navController = navController)
            }

        }
    }
}