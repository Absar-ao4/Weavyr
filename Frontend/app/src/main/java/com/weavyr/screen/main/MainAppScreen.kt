package com.weavyr.screen.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkRemove
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
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
import com.weavyr.screen.settingspac.PrivacySecurityScreen
import com.weavyr.screen.settingspac.PushNotificationsScreen
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
                arguments = listOf(navArgument("userId") { type = NavType.IntType })
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getInt("userId") ?: return@composable

                // Trigger the fetch when this screen opens
                LaunchedEffect(userId) {
                    mainViewModel.fetchOtherUserProfile(userId)
                }

                // Observe the downloaded profile
                val viewedUser by mainViewModel.viewedUserProfile.collectAsState()
                val isViewedUserLoading by mainViewModel.isViewedUserLoading.collectAsState()

                if (isViewedUserLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else if (viewedUser != null) {
                    UserProfileScreen(
                        user = viewedUser!!,
                        onBackClick = {
                            mainViewModel.clearViewedProfile()
                            navController.popBackStack()
                        },
                        onCollaborateClick = {
                            val researcher = Researcher(
                                id = viewedUser!!.id,
                                username = viewedUser!!.username,
                                name = viewedUser!!.name,
                                organization = viewedUser!!.organization,
                                field = viewedUser!!.field,
                                interests = viewedUser!!.interests ?: emptyList(),
                                papers = viewedUser!!.numberOfPapers ?: 0,
                                citations = viewedUser!!.totalCitations ?: 0,
                                experienceYears = viewedUser!!.experienceYears ?: 0,
                                achievements = viewedUser!!.achievements?.map { it.title } ?: emptyList(),
                                profilePhoto = viewedUser!!.profilePhoto,
                                roles = viewedUser!!.roles ?: emptyList() // ⭐ Added roles here
                            )
                            mainViewModel.addConnectionRequest(researcher)
                            navController.popBackStack()
                        }
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Could not load profile.")
                    }
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
            composable("notifications") {
                NotificationScreen(
                    viewModel = mainViewModel,
                    navController = navController
                )
            }

            composable("notify") {
                PushNotificationsScreen(
                    navController = navController
                )
            }
            composable("privacy") {
                PrivacySecurityScreen(
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
        badges = emptyList(),
        roles = researcher.roles // ⭐ Added roles mapping here
    )
}