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


@Composable
fun HomeScreen() {

    var selectedRole by remember { mutableStateOf("Peer") }
    var tutorialStep by remember { mutableStateOf(1) }
    val showTutorial = tutorialStep <= 2

    val researchers = remember {
        listOf(

            Researcher(
                name = "Dr. Aisha Khan",
                organization = "MIT Media Lab",
                location = "Boston, USA",
                field = "Artificial Intelligence",
                role = "Mentor",
                interests = listOf("Machine Learning", "NLP", "AI Ethics"),
                papers = 42,
                citations = 3200,
                collaborations = 18,
                experienceYears = 12,
                achievements = listOf(
                    "Best AI Research Award 2022",
                    "Keynote Speaker at NeurIPS",
                    "Published in Nature AI"
                ),
                linkedIn = "linkedin.com/aisha-khan",
                scholar = "scholar.google.com/aisha"
            ),

            Researcher(
                name = "Rohan Mehta",
                organization = "IISc Bangalore",
                location = "Bangalore, India",
                field = "Quantum Physics",
                role = "Peer",
                interests = listOf("Quantum Mechanics", "Astrophysics", "Particle Theory"),
                papers = 12,
                citations = 180,
                collaborations = 6,
                experienceYears = 5,
                achievements = listOf(
                    "Young Scientist Fellowship",
                    "Published in PRL"
                ),
                linkedIn = "linkedin.com/rohan-mehta"
            ),

            Researcher(
                name = "Neha Sharma",
                organization = "Oxford Biotech Lab",
                location = "Oxford, UK",
                field = "Genomics",
                role = "Mentee",
                interests = listOf("CRISPR", "Gene Editing", "Bioinformatics"),
                papers = 3,
                citations = 20,
                collaborations = 2,
                experienceYears = 2,
                achievements = listOf(
                    "Graduate Research Excellence Award"
                ),
                scholar = "scholar.google.com/neha"
            )

        )
    }

    val filtered = researchers.filter { it.role == selectedRole }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WeavyrBackground)
            .padding(24.dp)
    ) {

        Column(
            modifier = Modifier.fillMaxSize()
        ) {

            RoleSelector(
                selectedRole = selectedRole,
                onRoleSelected = { selectedRole = it }
            )

            Spacer(modifier = Modifier.height(32.dp))

            SwipeStack(
                researchers = filtered
            )
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
                            WeavyrBackground
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
