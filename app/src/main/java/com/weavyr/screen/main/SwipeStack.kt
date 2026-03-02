package com.weavyr.screen.main

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.weavyr.ui.theme.WeavyrPrimary
import com.weavyr.ui.theme.WeavyrSurface
import com.weavyr.ui.theme.WeavyrTextPrimary
import com.weavyr.ui.theme.WeavyrTextSecondary
import kotlinx.coroutines.launch
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
        animationSpec = spring(
            stiffness = Spring.StiffnessMediumLow
        ),
        label = ""
    )

    val rotation = animatedOffsetX / 60f

    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = WeavyrSurface
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(600.dp)
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
                        if (kotlin.math.abs(offsetX) > 300f) {
                            offsetX = if (offsetX > 0) 1000f else -1000f
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
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            Column {

                Text(
                    text = researcher.name,
                    style = MaterialTheme.typography.headlineSmall,
                    color = WeavyrTextPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = researcher.field,
                    color = WeavyrTextSecondary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Papers: ${researcher.papers} • Citations: ${researcher.citations}",
                    color = WeavyrTextSecondary
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                researcher.interests.forEach {
                    AssistChip(
                        onClick = {},
                        label = { Text(it) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = WeavyrPrimary.copy(alpha = 0.2f)
                        )
                    )
                }
            }
        }
    }
}