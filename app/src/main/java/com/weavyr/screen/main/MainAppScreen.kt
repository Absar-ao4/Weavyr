package com.weavyr.screen.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkRemove
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.weavyr.screen.components.FloatingBottomNavBar
import com.weavyr.model.Researcher
import com.weavyr.ui.theme.WeavyrBackground
import com.weavyr.viewmodel.MainViewModel
import com.weavyr.model.User
import com.weavyr.model.Interest
import com.weavyr.model.Achievement

@Composable
fun MainAppScreen(
    onLogout: () -> Unit
) {
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
                HomeScreen(
                    viewModel = mainViewModel,
                    navController = navController
                )
            }

            composable("myprofile") {
                MyProfile(
                    viewModel = mainViewModel,
                    navController = navController
                )
            }

            // --- FIXED: User Profile Route ---
            composable(
                route = "user_profile/{userId}",
                arguments = listOf(navArgument("userId") { type = NavType.StringType })
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId")

                // Use collectAsState to reactively get the researchers list
                val researchers by mainViewModel.allResearchers.collectAsState()
                val researcher = researchers.find { it.id == userId?.toIntOrNull() }
                researcher?.let {
                    UserProfileScreen(
                        user = mapResearcherToUser(it),
                        onBackClick = { navController.popBackStack() }
                    )
                }
            }

            composable("settings") {
                SettingsScreen(
                    navController = navController,
                    onLogout = onLogout
                )
            }

            composable("bookmarks") {
                LaunchedEffect(Unit) {
                    mainViewModel.fetchMyBookmarks()
                }

                ProfileListsScreen(
                    profiles = mainViewModel.bookmarkedProfiles,
                    actionIcon = Icons.Default.BookmarkRemove,
                    actionColor = Color(0xFFE57373),
                    emptyText = "No bookmarked profiles yet.",
                    onActionClick = { researcher ->
                        mainViewModel.removeBookmark(researcher)
                    }
                )
            }

            composable("rejected") {
                ProfileListsScreen(
                    profiles = mainViewModel.rejectedProfiles,
                    actionIcon = Icons.Default.Refresh,
                    actionColor = Color.Gray,
                    emptyText = "No rejected profiles.",
                    onActionClick = { researcher ->
                        // Logic to move back to deck could go here
                    }
                )
            }

            composable("requests") {
                ProfileListsScreen(
                    profiles = mainViewModel.connectionRequests,
                    actionIcon = Icons.Default.Send,
                    actionColor = Color(0xFF00C853),
                    emptyText = "No requests sent.",
                    onActionClick = { researcher ->
                        // Logic to cancel request
                    }
                )
            }

            composable("edit_profile") {
                EditProfileScreen(viewModel = mainViewModel, navController = navController)
            }
        }
    }
}

/**
 * FIXED Helper function to convert 'Researcher' to 'User'.
 * Addresses Int/String ID conflicts and nested object construction.
 */
fun mapResearcherToUser(researcher: Researcher): User {

    return User(
        id = researcher.id,
        username = "user${researcher.id}",

        name = researcher.name,
        email = null,

        education = null,
        field = researcher.field,
        organization = researcher.organization,
        experienceYears = researcher.experienceYears,

        profilePhoto = null,

        numberOfPapers = researcher.papers,
        totalCitations = researcher.citations,

        achievements = researcher.achievements.mapIndexed { index, title ->
            Achievement(
                id = index,
                title = title,
                description = null,
                year = null
            )
        },

        interests = researcher.interests.mapIndexed { index, name ->
            Interest(
                id = index,
                name = name
            )
        },

        papersAuthored = emptyList(),
        badges = emptyList()
    )
}