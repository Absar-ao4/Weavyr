package com.weavyr.screen.main

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkRemove
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Refresh
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
import com.weavyr.model.Achievement
import com.weavyr.model.Researcher
import com.weavyr.model.User
import com.weavyr.screen.components.FloatingBottomNavBar
import com.weavyr.screen.components.MatchDialog
import com.weavyr.viewmodel.MainViewModel

@Composable
fun MainAppScreen(
    onLogout: () -> Unit
) {

    val navController = rememberNavController()

    val mainViewModel: MainViewModel = viewModel()

    val matchEvent by mainViewModel.matchEvent.collectAsState()

    Scaffold(
        modifier = Modifier.systemBarsPadding(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            FloatingBottomNavBar(navController)
        }
    ) { padding ->

        matchEvent?.let { matchedUser ->

            MatchDialog(
                matchedUser = matchedUser,
                onDismiss = { mainViewModel.clearMatch() },
                onViewProfile = {

                    mainViewModel.clearMatch()

                    navController.navigate("user_profile/${matchedUser.id}")
                }
            )
        }

        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(padding)
        ) {

            composable("articles") {
                ArticlesScreen()
            }

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

            composable(
                route = "user_profile/{userId}",
                arguments = listOf(
                    navArgument("userId") { type = NavType.StringType }
                )
            ) { backStackEntry ->

                val userId =
                    backStackEntry.arguments
                        ?.getString("userId")
                        ?.toIntOrNull()

                val researchers by mainViewModel.allResearchers.collectAsState()

                val researcher =
                    researchers.find { it.id == userId }
                        ?: mainViewModel.matchedResearchers.find { it.id == userId }
                        ?: mainViewModel.incomingRequests.find { it.id == userId }
                        ?: mainViewModel.connectionRequests.find { it.id == userId }
                        ?: mainViewModel.rejectedProfiles.find { it.id == userId }
                        ?: mainViewModel.bookmarkedProfiles.find { it.id == userId }

                researcher?.let {

                    UserProfileScreen(
                        user = mapResearcherToUser(it),

                        onBackClick = {
                            navController.popBackStack()
                        },

                        onCollaborateClick = {
                            mainViewModel.addConnectionRequest(it)
                            navController.popBackStack()
                        }
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
                    },

                    onProfileClick = { researcher ->
                        navController.navigate("user_profile/${researcher.id}")
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
                        mainViewModel.rejectedProfiles.remove(researcher)
                    },

                    onProfileClick = { researcher ->
                        navController.navigate("user_profile/${researcher.id}")
                    }
                )
            }

            composable("sent") {

                ProfileListsScreen(
                    profiles = mainViewModel.connectionRequests,

                    actionIcon = Icons.Default.HourglassEmpty,
                    actionColor = MaterialTheme.colorScheme.outline,

                    emptyText = "You haven't sent any requests yet.",

                    onActionClick = { },

                    onProfileClick = { researcher ->
                        navController.navigate("user_profile/${researcher.id}")
                    }
                )
            }

            composable("edit_profile") {
                EditProfileScreen(
                    viewModel = mainViewModel,
                    navController = navController
                )
            }
        }
    }
}

fun mapResearcherToUser(
    researcher: Researcher
): User {

    return User(

        id = researcher.id,

        username = "user${researcher.id}",

        name = researcher.name ?: "",
        email = null,

        education = null,

        field = researcher.field ?: "",
        organization = researcher.organization ?: "",

        experienceYears = researcher.experienceYears,

        profilePhoto = researcher.profilePhoto,

        numberOfPapers = researcher.papers,
        totalCitations = researcher.citations,

        achievements =
            researcher.achievements.mapIndexed { index, title ->

                Achievement(
                    id = index,
                    title = title,
                    description = null,
                    year = null
                )
            },

        interests = researcher.interests,

        papersAuthored = emptyList(),
        badges = emptyList()
    )
}