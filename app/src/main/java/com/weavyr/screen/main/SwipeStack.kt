package com.weavyr.screen.main

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.weavyr.ui.theme.*
import kotlin.math.abs

@Composable
fun SwipeStack(
    researchers: List<Researcher>
) {

    var cards by remember(researchers) {
        mutableStateOf(researchers)
    }

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
                        onSwiped = {
                            cards = cards.drop(1)
                        }
                    )
                }
        }
    }
}

@Composable
fun SwipeableCard(
    researcher: Researcher,
    modifier: Modifier = Modifier,
    onSwiped: () -> Unit
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
            .fillMaxHeight(0.85f)
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
                            offsetX = if (offsetX > 0) 1200f else -1200f
                            onSwiped()
                        } else {
                            offsetX = 0f
                        }
                    }
                )
            }
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {

            // 🔥 IMAGE HEADER
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .background(WeavyrPrimary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = researcher.name.first().toString(),
                    style = MaterialTheme.typography.displayLarge,
                    color = WeavyrPrimary
                )
            }

            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {

                // Name
                Text(
                    text = researcher.name,
                    style = MaterialTheme.typography.headlineSmall,
                    color = WeavyrTextPrimary
                )

                // Organization + Location
                Text(
                    text = "${researcher.organization} • ${researcher.location}",
                    color = WeavyrTextSecondary
                )

                // Interests
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    researcher.interests.take(3).forEach {
                        AssistChip(
                            onClick = {},
                            label = { Text(it) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = WeavyrPrimary.copy(alpha = 0.15f)
                            )
                        )
                    }
                }

                // Stats
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Column {
                        Text(
                            text = researcher.papers.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            color = WeavyrPrimary
                        )
                        Text("Papers", color = WeavyrTextSecondary)
                    }

                    Column {
                        Text(
                            text = researcher.citations.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            color = WeavyrPrimary
                        )
                        Text("Citations", color = WeavyrTextSecondary)
                    }
                }

                // Links
                if (researcher.linkedIn != null || researcher.scholar != null) {

                    Text(
                        text = "Links",
                        style = MaterialTheme.typography.titleMedium,
                        color = WeavyrTextPrimary
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {

                        researcher.linkedIn?.let {
                            TextButton(onClick = { }) {
                                Text("LinkedIn", color = WeavyrPrimary)
                            }
                        }

                        researcher.scholar?.let {
                            TextButton(onClick = { }) {
                                Text("Scholar", color = WeavyrPrimary)
                            }
                        }
                    }
                }
            }
        }
    }
}