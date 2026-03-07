package com.weavyr.screen.main

import android.widget.Toast
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weavyr.R
import com.weavyr.model.Researcher
import com.weavyr.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun SwipeStack(
    researchers: List<Researcher>,
    viewModel: MainViewModel,
    onViewProfile: (Researcher) -> Unit
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        val currentResearcher = researchers.firstOrNull()

        if (currentResearcher == null) {
            EmptyDeckState(
                onRefresh = { viewModel.fetchDiscoverDeck() }
            )
        } else {
            val isBookmarked = viewModel.bookmarkedProfiles.any { it.id == currentResearcher.id }

            key(currentResearcher.id) {
                SwipeableCard(
                    researcher = currentResearcher,
                    isBookmarked = isBookmarked,
                    modifier = Modifier.fillMaxWidth(),
                    onSwiped = { swipedRight ->
                        if (swipedRight) viewModel.addConnectionRequest(currentResearcher)
                        else viewModel.addRejected(currentResearcher)
                    },
                    onBookmark = { profile ->
                        if (isBookmarked) {
                            viewModel.removeBookmark(profile)
                            Toast.makeText(context, "Bookmark removed", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.addBookmark(profile)
                            Toast.makeText(context, "Profile Saved!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onViewProfile = onViewProfile
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SwipeableCard(
    researcher: Researcher,
    isBookmarked: Boolean,
    modifier: Modifier = Modifier,
    onSwiped: (Boolean) -> Unit,
    onBookmark: (Researcher) -> Unit,
    onViewProfile: (Researcher) -> Unit
) {
    var offsetX by remember { mutableStateOf(0f) }
    val coroutineScope = rememberCoroutineScope()
    var localBookmarked by remember { mutableStateOf(isBookmarked) }

    val animatedOffsetX by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "swipe_offset"
    )

    val rotation = animatedOffsetX / 40f

    val overlayColor = when {
        offsetX > 0 -> Color(0xFF00C853) // Green for right
        offsetX < 0 -> Color(0xFFD50000) // Red for left
        else -> Color.Transparent
    }

    val overlayAlpha = if (abs(offsetX) > 100f) {
        ((abs(offsetX) - 100f) / 600f).coerceIn(0f, 0.4f)
    } else {
        0f
    }

    val bookmarkScale by animateFloatAsState(
        targetValue = if (localBookmarked) 1.2f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "bookmark_scale"
    )

    Card(
        shape = RoundedCornerShape(32.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                        if (abs(offsetX) > 500f) {
                            onSwiped(offsetX > 0)
                        } else {
                            offsetX = 0f
                        }
                    }
                )
            }
    ) {
        val scrollState = rememberScrollState()

        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {

                val expertise = getLeagueBadge(researcher.papers, researcher.citations)
                val badgeColors = getBadgeColors(expertise)

                // HEADER (Image + Overlay Badges)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.cardprofileimage),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.65f))
                                )
                            )
                    )

                    // Bookmark Action
                    IconButton(
                        onClick = {
                            if (!localBookmarked) {
                                localBookmarked = true
                                coroutineScope.launch {
                                    delay(350)
                                    onBookmark(researcher)
                                }
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(16.dp)
                            .graphicsLayer {
                                scaleX = bookmarkScale
                                scaleY = bookmarkScale
                            }
                    ) {
                        Icon(
                            imageVector = if (localBookmarked) Icons.Filled.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (localBookmarked) MaterialTheme.colorScheme.primary else Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // Dynamic League Badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                            .background(
                                Brush.horizontalGradient(colors = badgeColors),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.WorkspacePremium,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = expertise,
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = researcher.name,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    )
                }

                // CONTENT SECTION
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = researcher.name,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Business, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(researcher.organization, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Psychology, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(researcher.field, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    // Interests FlowRow
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        researcher.interests.forEach { interest ->
                            AssistChip(
                                onClick = {},
                                label = { Text(interest, maxLines = 1) },
                                colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.surface)
                            )
                        }
                    }

                    // Stats Row
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        StatBlock("Papers", researcher.papers)
                        StatBlock("Citations", researcher.citations)
                    }

                    Text(
                        text = "Experience: ${researcher.experienceYears} years",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    if (researcher.achievements.isNotEmpty()) {
                        Text(
                            text = "Achievements",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                        researcher.achievements.forEach { achievement ->
                            Row(verticalAlignment = Alignment.Top) {
                                Text("•", color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(end = 8.dp))
                                Text(achievement, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 🌟 NEW: VIEW FULL PROFILE BUTTON 🌟
                    OutlinedButton(
                        onClick = { onViewProfile(researcher) },
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("View Full Profile", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }

            // Swipe Overlay Feedback (Border and Gradient)
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
fun EmptyDeckState(onRefresh: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(MaterialTheme.colorScheme.surface, shape = CircleShape)
                .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Explore,
                contentDescription = "Empty Deck",
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "You're all caught up!",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "We're exploring the network for more amazing researchers. Check back soon!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onRefresh,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(50),
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(50.dp)
        ) {
            Text("Refresh Deck", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun StatBlock(label: String, value: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

fun getLeagueBadge(papers: Int, citations: Int): String {
    val score = papers + (citations / 10)
    return when {
        score < 10 -> "Explorer"
        score < 35 -> "Innovator"
        score < 80 -> "Architect"
        score < 200 -> "Luminary"
        else -> "Visionary"
    }
}

fun getBadgeColors(expertise: String): List<Color> {
    return when (expertise) {
        "Explorer" -> listOf(Color(0xFF8D6E63), Color(0xFF5D4037))
        "Innovator" -> listOf(Color(0xFF42A5F5), Color(0xFF1565C0))
        "Architect" -> listOf(Color(0xFFFFCA28), Color(0xFFFF8F00))
        "Luminary" -> listOf(Color(0xFFAB47BC), Color(0xFF4A148C))
        "Visionary" -> listOf(Color(0xFFFF416C), Color(0xFFFF4B2B))
        else -> listOf(Color(0xFF9E9E9E), Color(0xFF616161))
    }
}