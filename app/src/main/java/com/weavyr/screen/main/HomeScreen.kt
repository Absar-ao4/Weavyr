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
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.ui.text.font.FontWeight
import com.weavyr.viewmodel.MainViewModel
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen(viewModel: MainViewModel) {

    var selectedRole by remember { mutableStateOf("Peer") }
    var tutorialStep by remember { mutableStateOf(1) }
    val showTutorial = tutorialStep <= 2

    val researchers = remember {
        List(20) { index ->
            Researcher(
                name = "Researcher ${index + 1}",
                organization = listOf(
                    "MIT Media Lab",
                    "IISc Bangalore",
                    "Oxford Biotech Lab",
                    "Stanford AI Lab",
                    "Cambridge Quantum Center"
                )[index % 5],
                location = listOf(
                    "USA",
                    "India",
                    "UK",
                    "Germany",
                    "Canada"
                )[index % 5],
                field = listOf(
                    "Artificial Intelligence",
                    "Quantum Physics",
                    "Genomics",
                    "Cybersecurity",
                    "Climate Science"
                )[index % 5],
                role = listOf("Peer", "Mentor", "Mentee")[index % 3],
                interests = listOf("AI", "ML", "Research", "Innovation"),
                papers = (1..60).random(),
                citations = (10..5000).random(),
                collaborations = (1..25).random(),
                experienceYears = (1..15).random(),
                achievements = listOf("Published in Nature", "IEEE Award Winner"),
                linkedIn = "linkedin.com/user$index",
                scholar = "scholar.google.com/user$index"
            )
        }
    }

    val filtered = researchers.filter { it.role == selectedRole }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WeavyrBackground)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
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
                    text = "CONNECT",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp,
                    color = WeavyrTextPrimary
                )

                Text(
                    text = "W",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp,
                    color = WeavyrPrimary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Find your next research partner",
                style = MaterialTheme.typography.labelMedium,
                color = WeavyrTextSecondary,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

// ROLES
            Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                RoleSelector(
                    selectedRole = selectedRole,
                    onRoleSelected = { selectedRole = it }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // CARD AREA (separate spacing)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                SwipeStack(
                    researchers = filtered,
                    viewModel = viewModel
                )
            }
        }

        if (showTutorial) {
            HomeTutorialOverlay(
                step = tutorialStep,
                onNext = { tutorialStep = 2 },
                onDismiss = { tutorialStep = 3 }
            )
        }
    }
}

@Composable
fun RoleSelector(
    selectedRole: String,
    onRoleSelected: (String) -> Unit
) {

    val roles = listOf("Peer", "Mentor", "Mentee")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        roles.forEach { role ->

            val selected = role == selectedRole

            FilterChip(
                selected = selected,
                onClick = { onRoleSelected(role) },
                label = {
                    Text(
                        text = role,
                        color = if (selected)
                            WeavyrTextSecondary
                        else
                            WeavyrTextPrimary
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = WeavyrPrimary,
                    containerColor = WeavyrSurface
                )
            )
        }
    }
}

@Composable
fun HomeTutorialOverlay(
    step: Int,
    onNext: () -> Unit,
    onDismiss: () -> Unit
) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.75f))
    ) {

        when (step) {

            1 -> SwipeHint(onNext)

            2 -> RoleHint(onDismiss)
        }
    }
}

@Composable
fun SwipeHint(
    onNext: () -> Unit
) {

    val infiniteTransition = rememberInfiniteTransition(label = "")
    val offset by infiniteTransition.animateFloat(
        initialValue = -20f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = ""
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Icon(
            imageVector = Icons.Default.ArrowForward,
            contentDescription = null,
            tint = WeavyrPrimary,
            modifier = Modifier
                .offset(x = offset.dp)
                .size(48.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        TutorialBubble(
            text = "Swipe right to connect\nSwipe left to skip",
            onClick = onNext
        )
    }
}

@Composable
fun RoleHint(
    onDismiss: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        TutorialBubble(
            text = "Peer: Collaborate\nMentor: Learn\nMentee: Guide others",
            onClick = onDismiss
        )
    }
}
@Composable
fun TutorialBubble(
    text: String,
    onClick: () -> Unit
) {

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = WeavyrSurface
        ),
        modifier = Modifier
            .padding(horizontal = 32.dp)
    ) {

        Column(
            modifier = Modifier
                .padding(20.dp)
        ) {

            Text(
                text = text,
                color = WeavyrTextPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(
                onClick = onClick
            ) {
                Text("Got it →", color = WeavyrPrimary)
            }
        }
    }
}
