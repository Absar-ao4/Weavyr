package com.weavyr.screen.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkRemove
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.weavyr.screen.components.FloatingBottomNavBar
import com.weavyr.screen.components.MatchDialog // ⭐ Added Import
import com.weavyr.model.Researcher
import com.weavyr.viewmodel.MainViewModel
import com.weavyr.model.User
import com.weavyr.model.Achievement

@Composable
fun MainAppScreen(
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    val mainViewModel: MainViewModel = viewModel()

    // ⭐ Observe the match state from the ViewModel
    val matchEvent by mainViewModel.matchEvent.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            FloatingBottomNavBar(navController)
        }
    ) { padding ->

        // ⭐ Show the Match Dialog if we have a match!
        // This is placed outside the NavHost so it floats over EVERYTHING.
        matchEvent?.let { matchedUser ->
            MatchDialog(
                matchedUser = matchedUser,
                onDismiss = { mainViewModel.clearMatch() },
                onSendMessage = {
                    mainViewModel.clearMatch()
                    // If you build a chat screen later, navigate to it here:
                    // navController.navigate("chat/${matchedUser.id}")
                }
            )
        }

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
                    actionColor = MaterialTheme.colorScheme.error,
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
                    actionColor = MaterialTheme.colorScheme.primary,
                    emptyText = "No rejected profiles.",
                    onActionClick = { researcher ->
                        // ⭐ FIX: Remove from rejected list so they can show up in Discover again
                        mainViewModel.rejectedProfiles.remove(researcher)
                        // (You will eventually want an undo API call here to delete the reject from the DB)
                    }
                )
            }

            // ⭐ ADDED: Sent requests route
            composable("sent") {
                ProfileListsScreen(
                    profiles = mainViewModel.connectionRequests, // Viewmodel uses connectionRequests for "Sent"
                    actionIcon = Icons.Default.HourglassEmpty,
                    actionColor = MaterialTheme.colorScheme.outline,
                    emptyText = "You haven't sent any requests yet.",
                    onActionClick = { /* No action needed, just waiting for them to accept */ }
                )
            }

            composable("requests") {
                ProfileListsScreen(
                    profiles = mainViewModel.incomingRequests,

                    // Primary Button (Accept)
                    actionIcon = Icons.Default.Check,
                    actionColor = MaterialTheme.colorScheme.primary,
                    onActionClick = { researcher ->
                        mainViewModel.acceptRequest(researcher)
                    },

                    // Secondary Button (Decline)
                    secondaryActionIcon = Icons.Default.Close,
                    secondaryActionColor = MaterialTheme.colorScheme.error,
                    onSecondaryActionClick = { researcher ->
                        mainViewModel.rejectRequest(researcher)
                    },

                    emptyText = "No collaboration requests yet."
                )
            }

            composable("edit_profile") {
                EditProfileScreen(viewModel = mainViewModel, navController = navController)
            }
        }
    }
}

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

        // ✅ FIXED HERE
        interests = researcher.interests,

        papersAuthored = emptyList(),
        badges = emptyList()
    )
}