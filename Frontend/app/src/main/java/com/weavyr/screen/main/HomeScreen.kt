package com.weavyr.screen.main

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.weavyr.screen.components.CoolTutorialOverlay
import com.weavyr.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    navController: NavController
) {
    val allResearchers by viewModel.allResearchers.collectAsState()
    val isDeckLoading by viewModel.isDeckLoading.collectAsState()

    val incomingCount = viewModel.incomingRequests.size

    val context = LocalContext.current
    val sharedPreferences = remember {
        context.getSharedPreferences("weavyr_prefs", Context.MODE_PRIVATE)
    }

    var hasSeenTutorial by remember {
        mutableStateOf(
            sharedPreferences.getBoolean("has_seen_tutorial_v3", false)
        )
    }

    var showLikeCount by remember {
        mutableStateOf(incomingCount > 0)
    }

    LaunchedEffect(incomingCount) {
        if (incomingCount > 0) {
            showLikeCount = true
        }
    }

    val filters = listOf("Peer", "Mentor", "Mentee")
    var selectedFilter by remember {
        mutableStateOf(filters[0])
    }

    /*
     * Base deck:
     * Remove profiles already interacted with.
     *
     * Important:
     * Compare using id, not contains(profile).
     * contains(profile) compares the full data class object,
     * so small differences in roles/photo/name can cause mismatch.
     */
    val filteredDeck by remember(
        allResearchers,
        viewModel.connectionRequests.size,
        viewModel.rejectedProfiles.size,
        viewModel.bookmarkedProfiles.size
    ) {
        derivedStateOf {
            allResearchers.filter { profile ->
                viewModel.connectionRequests.none { it.id == profile.id } &&
                        viewModel.rejectedProfiles.none { it.id == profile.id } &&
                        viewModel.bookmarkedProfiles.none { it.id == profile.id }
            }
        }
    }

    /*
     * Active deck:
     * Show profiles according to selected role tab.
     *
     * Handles both:
     * ["PEER", "MENTOR"]
     * ["PEER, MENTOR"]
     */
    val activeDeck by remember(
        filteredDeck,
        selectedFilter
    ) {
        derivedStateOf {
            val selectedRoleUpper = selectedFilter.uppercase()

            filteredDeck.filter { profile ->
                val normalizedRoles = profile.roles
                    .flatMap { role -> role.split(",") }
                    .map { role -> role.trim().uppercase() }
                    .filter { role ->
                        role == "PEER" || role == "MENTOR" || role == "MENTEE"
                    }
                    .distinct()

                if (normalizedRoles.isEmpty()) {
                    selectedRoleUpper == "PEER"
                } else {
                    normalizedRoles.contains(selectedRoleUpper)
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // HEADER
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DISCOVER",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Box(
                    contentAlignment = Alignment.TopEnd,
                    modifier = Modifier.clickable {
                        navController.navigate("notifications")
                    }
                ) {
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        )
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.Notifications,
                                contentDescription = "Incoming Requests",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                    if (incomingCount > 0 && showLikeCount) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .size(20.dp)
                                .offset(x = 2.dp, y = (-2).dp)
                                .clip(CircleShape),
                            border = BorderStroke(
                                width = 2.dp,
                                color = MaterialTheme.colorScheme.background
                            )
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (incomingCount > 9) "9+" else incomingCount.toString(),
                                    color = MaterialTheme.colorScheme.onError,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 1.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Find who you want to collaborate with on a project",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // FILTER CHIPS
            LazyRow(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filters.size) { index ->
                    val filter = filters[index]
                    val isSelected = selectedFilter == filter

                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedFilter = filter
                        },
                        label = {
                            Text(
                                text = filter,
                                fontWeight = if (isSelected) {
                                    FontWeight.Bold
                                } else {
                                    FontWeight.Normal
                                }
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(16.dp),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                    )
                }
            }

            // CARD STACK
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        top = 8.dp,
                        bottom = 16.dp
                    ),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isDeckLoading -> {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    activeDeck.isEmpty() -> {
                        EmptyDeckState(
                            onRefresh = {
                                viewModel.fetchDiscoverDeck()
                                showLikeCount = false
                            }
                        )
                    }

                    else -> {
                        SwipeStack(
                            researchers = activeDeck,
                            viewModel = viewModel,
                            onViewProfile = { profile ->
                                navController.navigate("user_profile/${profile.id}")
                            }
                        )
                    }
                }
            }
        }

        if (!hasSeenTutorial && !isDeckLoading && filteredDeck.isNotEmpty()) {
            CoolTutorialOverlay(
                onDismiss = {
                    hasSeenTutorial = true
                    sharedPreferences.edit()
                        .putBoolean("has_seen_tutorial_v3", true)
                        .apply()
                }
            )
        }
    }
}