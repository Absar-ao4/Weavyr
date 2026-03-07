package com.weavyr.screen.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.weavyr.ui.theme.*
import androidx.compose.animation.core.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.ui.text.font.FontWeight
import com.weavyr.viewmodel.MainViewModel
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun HomeScreen(viewModel: MainViewModel, navController: NavController) {

    val allResearchers by viewModel.allResearchers.collectAsState()
    val isDeckLoading by viewModel.isDeckLoading.collectAsState()

    val filteredDeck = allResearchers.filter { profile ->
        !viewModel.connectionRequests.contains(profile) &&
                !viewModel.rejectedProfiles.contains(profile) &&
                !viewModel.bookmarkedProfiles.contains(profile)
    }

    Box(modifier = Modifier.fillMaxSize().background(WeavyrBackground)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(20.dp))

            // HEADER
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("CONNECT", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp, color = WeavyrTextPrimary)
                Text("W", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp, color = WeavyrPrimary)
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text("Find your next research partner", style = MaterialTheme.typography.labelMedium, color = WeavyrTextSecondary, modifier = Modifier.padding(horizontal = 24.dp))
            Spacer(modifier = Modifier.height(20.dp))

            // CARD AREA
            Box(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), contentAlignment = Alignment.Center) {
                if (isDeckLoading) {
                    CircularProgressIndicator(color = WeavyrPrimary)
                } else {
                    SwipeStack(
                        researchers = filteredDeck,
                        viewModel = viewModel,
                        onViewProfile = { researcher ->
                            // Navigate to UserProfileScreen using the researcher's ID
                            navController.navigate("user_profile/${researcher.id}")
                        }
                    )
                }
            }
        }

        if (!viewModel.hasSeenTutorial && filteredDeck.isNotEmpty()) {
            HomeTutorialOverlay(onDismiss = { viewModel.hasSeenTutorial = true })
        }
    }
}

@Composable
fun HomeTutorialOverlay(onDismiss: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.75f))) {
        SwipeHint(onNext = onDismiss)
    }
}

@Composable
fun SwipeHint(onNext: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val offset by infiniteTransition.animateFloat(
        initialValue = -20f, targetValue = 20f,
        animationSpec = infiniteRepeatable(animation = tween(800), repeatMode = RepeatMode.Reverse),
        label = ""
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = WeavyrPrimary, modifier = Modifier.offset(x = offset.dp).size(48.dp))
        Spacer(modifier = Modifier.height(16.dp))
        TutorialBubble(text = "Swipe right to connect\nSwipe left to skip", onClick = onNext)
    }
}

@Composable
fun TutorialBubble(text: String, onClick: () -> Unit) {
    Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = WeavyrSurface), modifier = Modifier.padding(horizontal = 32.dp)) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(text = text, color = WeavyrTextPrimary)
            Spacer(modifier = Modifier.height(12.dp))
            TextButton(onClick = onClick) { Text("Got it →", color = WeavyrPrimary) }
        }
    }
}