package com.weavyr.screen.main

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
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

    var showLikeCount by remember { mutableStateOf(incomingCount > 0) }

    LaunchedEffect(incomingCount) {
        if (incomingCount > 0) {
            showLikeCount = true
        }
    }

    val filteredDeck = allResearchers.filter { profile ->
        !viewModel.connectionRequests.contains(profile) &&
                !viewModel.rejectedProfiles.contains(profile) &&
                !viewModel.bookmarkedProfiles.contains(profile)
    }

    val filters = listOf("Peer", "Mentor", "Mentee")
    var selectedFilter by remember { mutableStateOf(filters[0]) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
    ) {

        Column(modifier = Modifier.fillMaxSize()) {

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

                Box(contentAlignment = Alignment.TopEnd) {

                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Likes",
                        tint = Color.Red,
                        modifier = Modifier.size(28.dp)
                    )

                    if (incomingCount > 0 && showLikeCount) {

                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        ) {

                            Box(contentAlignment = Alignment.Center) {

                                Text(
                                    text = incomingCount.toString(),
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )

                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // SUBTITLE
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

            // CARD STACK
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
                contentAlignment = Alignment.Center
            ) {

                if (isDeckLoading) {

                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)

                } else if (filteredDeck.isEmpty()) {

                    EmptyDeckState(
                        onRefresh = {
                            viewModel.fetchDiscoverDeck()
                            showLikeCount = false
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
                    sharedPreferences.edit()
                        .putBoolean("has_seen_tutorial", true)
                        .apply()
                }
            )
        }
    }
}
