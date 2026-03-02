package com.weavyr.screen.main

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.*
import androidx.compose.ui.draw.alpha
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.border
import com.weavyr.ui.theme.*
import kotlin.math.abs
import androidx.compose.ui.graphics.Brush
import androidx.compose.material.icons.filled.KeyboardArrowDown

@Composable
fun SwipeStack(
    researchers: List<Researcher>
) {

    var cards by remember(researchers) {
        mutableStateOf(researchers)
    }

    var savedProfiles by remember { mutableStateOf(listOf<Researcher>()) }
    var rejectedProfiles by remember { mutableStateOf(listOf<Researcher>()) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 8.dp),
        contentAlignment = Alignment.TopCenter
    ) {

        if (cards.isEmpty()) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 120.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "No more profiles",
                    style = MaterialTheme.typography.titleMedium,
                    color = WeavyrTextSecondary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Try switching roles to explore more researchers.",
                    color = WeavyrTextSecondary
                )
            }

        } else {

            cards
                .take(3)
                .reversed()
                .forEachIndexed { index, researcher ->

                    val scale = 1f - (index * 0.04f)
                    val offsetY = (index * 18).dp

                    SwipeableCard(
                        researcher = researcher,
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset(y = offsetY)
                            .scale(scale),
                        onSwiped = { swipedRight ->

                            val removed = researcher
                            cards = cards.drop(1)

                            if (swipedRight) {
                                // Connected
                            } else {
                                rejectedProfiles = rejectedProfiles + removed
                            }
                        },
                        onBookmark = { profile ->
                            savedProfiles = savedProfiles + profile
                        }
                    )
                }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SwipeableCard(
    researcher: Researcher,
    modifier: Modifier = Modifier,
    onSwiped: (Boolean) -> Unit,
    onBookmark: (Researcher) -> Unit
) {

    var offsetX by remember { mutableStateOf(0f) }

    val animatedOffsetX by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = ""
    )

    val rotation = animatedOffsetX / 50f

    Card(
        shape = RoundedCornerShape(32.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        colors = CardDefaults.cardColors(
            containerColor = WeavyrSurface
        ),
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(0.92f)
            .graphicsLayer {
                translationX = animatedOffsetX
                rotationZ = rotation
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x
                    },
                    onDragEnd = {
                        if (abs(offsetX) > 300f) {

                            val isRight = offsetX > 0

                            offsetX = if (isRight) 1200f else -1200f

                            onSwiped(isRight)

                        } else {
                            offsetX = 0f
                        }
                    }
                )
            }
    ) {

        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {

            val expertise = getExpertiseTitle(
                researcher.papers,
                researcher.citations
            )

            // IMAGE HEADER
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .background(WeavyrPrimary.copy(alpha = 0.15f))
            ) {

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = researcher.name.first().toString(),
                        style = MaterialTheme.typography.displayLarge,
                        color = WeavyrPrimary
                    )
                }

                // Bookmark
                IconButton(
                    onClick = { onBookmark(researcher) },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.BookmarkBorder,
                        contentDescription = "Save Profile",
                        tint = WeavyrTextPrimary
                    )
                }

                // Expertise Badge
                val infiniteTransition = rememberInfiniteTransition(label = "")

                val animatedAlpha by infiniteTransition.animateFloat(
                    initialValue = 0.6f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2500),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = ""
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {

                    // Outer Glow Border
                    Box(
                        modifier = Modifier
                            .border(
                                width = 1.8.dp,
                                brush = Brush.horizontalGradient(
                                    listOf(
                                        WeavyrPrimary.copy(alpha = animatedAlpha),
                                        Color.White.copy(alpha = animatedAlpha * 0.6f),
                                        WeavyrPrimary.copy(alpha = animatedAlpha)
                                    )
                                ),
                                shape = RoundedCornerShape(22.dp)
                            )
                            .background(
                                color = WeavyrSurface.copy(alpha = 0.85f),
                                shape = RoundedCornerShape(22.dp)
                            )
                            .padding(horizontal = 18.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = expertise,
                            color = WeavyrTextPrimary,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }


            }

            val hintAlpha by animateFloatAsState(
                targetValue = if (scrollState.value < 10) 1f else 0f,
                animationSpec = tween(300),
                label = ""
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(hintAlpha)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Swipe up to explore more",
                    style = MaterialTheme.typography.labelSmall,
                    color = WeavyrTextSecondary.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = WeavyrTextSecondary.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {

                Text(
                    text = researcher.name,
                    style = MaterialTheme.typography.headlineSmall,
                    color = WeavyrTextPrimary
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Business, null, tint = WeavyrPrimary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(researcher.organization, color = WeavyrTextSecondary)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, tint = WeavyrPrimary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(researcher.location, color = WeavyrTextSecondary)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Psychology, null, tint = WeavyrPrimary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(researcher.field, color = WeavyrTextSecondary)
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    researcher.interests.forEach {
                        AssistChip(
                            onClick = {},
                            label = {
                                Text(
                                    text = it,
                                    maxLines = 1
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = WeavyrPrimary.copy(alpha = 0.15f)
                            )
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StatBlock("Papers", researcher.papers)
                    StatBlock("Citations", researcher.citations)
                    StatBlock("Collabs", researcher.collaborations)
                }

                Text(
                    text = "Experience: ${researcher.experienceYears} years",
                    color = WeavyrTextSecondary
                )

                Text(
                    text = "Achievements",
                    style = MaterialTheme.typography.titleMedium,
                    color = WeavyrTextPrimary
                )

                researcher.achievements.forEach {
                    Text("• $it", color = WeavyrTextSecondary)
                }

                if (researcher.linkedIn != null || researcher.scholar != null) {

                    Text(
                        text = "Links",
                        style = MaterialTheme.typography.titleMedium,
                        color = WeavyrTextPrimary
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {

                        researcher.linkedIn?.let {
                            TextButton(onClick = {}) {
                                Text("LinkedIn", color = WeavyrPrimary)
                            }
                        }

                        researcher.scholar?.let {
                            TextButton(onClick = {}) {
                                Text("Scholar", color = WeavyrPrimary)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { /* navigate to full profile later */ },
                    modifier = Modifier
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("View Full Profile")
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun StatBlock(label: String, value: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.titleMedium,
            color = WeavyrPrimary
        )
        Text(label, color = WeavyrTextSecondary)
    }
}

fun getExpertiseTitle(papers: Int, citations: Int): String {
    return when {
        papers < 5 && citations < 50 -> "Explorer"
        papers < 20 && citations < 500 -> "Innovator"
        papers < 50 && citations < 2000 -> "Architect"
        else -> "Luminary"
    }
}