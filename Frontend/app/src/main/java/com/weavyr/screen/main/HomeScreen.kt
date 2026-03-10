package com.weavyr.screen.main

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.weavyr.screen.components.CoolTutorialOverlay
import com.weavyr.viewmodel.MainViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: MainViewModel, navController: NavController) {

    val allResearchers by viewModel.allResearchers.collectAsState()
    val isDeckLoading by viewModel.isDeckLoading.collectAsState()
    val incomingCount = viewModel.incomingRequests.size

    val context = LocalContext.current
    val sharedPreferences = remember {
        context.getSharedPreferences("weavyr_prefs", Context.MODE_PRIVATE)
    }

    var hasSeenTutorial by remember {
        mutableStateOf(sharedPreferences.getBoolean("has_seen_tutorial", false))
    }

    val filteredDeck = allResearchers.filter { profile ->
        !viewModel.connectionRequests.contains(profile) &&
                !viewModel.rejectedProfiles.contains(profile) &&
                !viewModel.bookmarkedProfiles.contains(profile)
    }

    // Your filters
    val filters = listOf("Peer", "Mentor", "Mentee")
    var selectedFilter by remember { mutableStateOf(filters[0]) }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
        .systemBarsPadding()) {
        Column(modifier = Modifier.fillMaxSize()) {

            Spacer(modifier = Modifier.height(20.dp))

            // 1. HEADER
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

                // 🏆 LEADERBOARD TELEPORT ICON 🏆
                IconButton(
                    onClick = { navController.navigate("leaderboard") },
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                        .size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "Leaderboard",
                        tint = Color(0xFFFFCA28), // Nice gold color
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 2. SUBTITLE
            Text(
                text = "Find who you want to collaborate with on a project",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 3. FILTER CHIPS
            LazyRow(
                modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filters.size) { index ->
                    val isSelected = selectedFilter == filters[index]
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = filters[index] },
                        label = {
                            Text(
                                text = filters[index],
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
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

            // INCOMING LIKES BANNER
            if (incomingCount > 0) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                        .fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$incomingCount people want to collaborate!",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // THE CARD STACK (Takes up all remaining space!)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // This makes it stretch to the bottom!
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isDeckLoading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                } else if (filteredDeck.isEmpty()) {

                    // ⭐ HERE IS THE NEW EMPTY STATE ⭐
                    EmptyDeckState(
                        onRefresh = {
                            viewModel.fetchDiscoverDeck()
                        }
                    )

                } else {
                    SwipeStack(
                        researchers = filteredDeck,
                        viewModel = viewModel,
                        onViewProfile = { profile ->
                            navController.navigate("user_profile/${profile.id}")
                        }
                    )
                }
            }
        }

        if (!hasSeenTutorial && !isDeckLoading && filteredDeck.isNotEmpty()) {
            CoolTutorialOverlay(
                onDismiss = {
                    hasSeenTutorial = true
                    sharedPreferences.edit().putBoolean("has_seen_tutorial", true).apply()
                }
            )
        }
    }
}

