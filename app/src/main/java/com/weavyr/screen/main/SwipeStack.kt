package com.weavyr.screen.main

import androidx.compose.animation.core.Spring
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
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
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.weavyr.R
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.weavyr.viewmodel.MainViewModel

@Composable
fun SwipeStack(
    researchers: List<Researcher>,
    viewModel: MainViewModel
) {

    var currentIndex by remember(researchers) { mutableStateOf(0) }

    val remaining = researchers.drop(currentIndex)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 8.dp),
        contentAlignment = Alignment.Center
    ) {

        if (currentIndex >= researchers.size) {

            Text(
                text = "No more profiles",
                color = WeavyrTextSecondary
            )

        } else {

            remaining
                .take(3)
                .reversed()
                .forEachIndexed { index, researcher ->

                    key(researcher.name) {   // use unique key

                        val scale = 1f - (index * 0.04f)
                        val offsetY = (index * 18).dp

                        SwipeableCard(
                            researcher = researcher,
                            modifier = Modifier
                                .fillMaxWidth()
                                .offset(y = offsetY)
                                .scale(scale),
                            onSwiped = { swipedRight ->

                                if (swipedRight) {
                                    viewModel.addConnectionRequest(researcher)
                                } else {
                                    viewModel.addRejected(researcher)
                                }

                                currentIndex++
                            },
                            onBookmark = { profile ->
                                viewModel.addBookmark(profile)
                            }
                        )
                    }
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

    var offsetX by remember(researcher) { mutableStateOf(0f) }

    val animatedOffsetX by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = ""
    )

    val rotation = animatedOffsetX / 50f

    // Swipe progress
    val swipeProgress = (offsetX / 600f).coerceIn(-1f, 1f)

    val overlayColor = when {
        swipeProgress > 0 -> Color(0xFF00C853)
        swipeProgress < 0 -> Color(0xFFD50000)
        else -> Color.Transparent
    }

    val overlayAlpha = abs(swipeProgress).coerceIn(0f, 0.35f)

    val scope = rememberCoroutineScope()

    Card(
        shape = RoundedCornerShape(32.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        colors = CardDefaults.cardColors(containerColor = WeavyrSurface),
        modifier = modifier
            .clip(RoundedCornerShape(32.dp))
            .fillMaxWidth()
            .fillMaxHeight()
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
                            onSwiped(isRight)
                        } else {
                            offsetX = 0f
                        }
                    }
                )
            }
    ) {

        val scrollState = rememberScrollState()

        Box(modifier = Modifier.fillMaxSize()) {

            // Scrollable Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {

                val expertise = getExpertiseTitle(
                    researcher.papers,
                    researcher.citations
                )

                // HEADER
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                ) {

                    // Profile Image
                    Image(
                        painter = painterResource(id = R.drawable.cardprofileimage),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Dark Gradient Overlay (for readability)
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.65f)
                                    )
                                )
                            )
                    )

                    // Bookmark Icon
                    IconButton(
                        onClick = { onBookmark(researcher) },
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.BookmarkBorder,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                    // Premium / Expertise Badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                            .background(
                                color = Color.Black.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .border(
                                width = 1.5.dp,
                                brush = Brush.horizontalGradient(
                                    listOf(
                                        WeavyrPrimary.copy(alpha = 0.9f),
                                        Color.White.copy(alpha = 0.6f),
                                        WeavyrPrimary.copy(alpha = 0.9f)
                                    )
                                ),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = expertise,
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }

                    // Name at bottom over image
                    Text(
                        text = researcher.name,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    )
                }

                // Scroll Hint
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

                // CONTENT
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
                        Icon(Icons.Default.Business, null, tint = WeavyrTextSecondary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(researcher.organization, color = WeavyrTextSecondary)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, null, tint = WeavyrTextSecondary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(researcher.location, color = WeavyrTextSecondary)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Psychology, null, tint = WeavyrTextSecondary)
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
                                label = { Text(it, maxLines = 1) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = WeavyrSurface
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

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = WeavyrPrimary,
                            contentColor = Color.White
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 8.dp,
                            pressedElevation = 12.dp
                        )
                    ) {
                        Text("View Full Profile")
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }

            // Swipe Overlay Layer
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                overlayColor.copy(alpha = overlayAlpha * 0.3f),
                                Color.Transparent,
                                overlayColor.copy(alpha = overlayAlpha * 0.3f)
                            )
                        )
                    )
                    .border(
                        width = if (overlayAlpha > 0f) 2.dp else 0.dp,
                        color = overlayColor.copy(alpha = overlayAlpha * 0.6f),
                        shape = RoundedCornerShape(32.dp)
                    )
            )
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