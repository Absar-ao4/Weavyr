package com.weavyr.screen.main

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weavyr.model.Researcher
import com.weavyr.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LeaderboardScreen(viewModel: MainViewModel) {
    // 1. Define the leagues from Top to Bottom
    val leagues = listOf("Visionary", "Luminary", "Architect", "Innovator", "Explorer")

    val pagerState = rememberPagerState(pageCount = { leagues.size })
    val coroutineScope = rememberCoroutineScope()
    val allResearchers by viewModel.allResearchers.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {

        Spacer(modifier = Modifier.height(20.dp))

        // HEADER
        Text(
            text = "LEADERBOARD",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Text(
            text = "Top researchers in the network",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 24.dp,end= 24.dp, bottom = 16.dp)
        )

        // LEAGUE TABS
        ScrollableTabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = Color.Transparent,
            edgePadding = 24.dp,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                    color = MaterialTheme.colorScheme.primary,
                    height = 3.dp
                )
            },
            divider = {}
        ) {
            leagues.forEachIndexed { index, title ->
                val isSelected = pagerState.currentPage == index
                val badgeColors = getBadgeColors(title) // Reusing your existing function

                Tab(
                    selected = isSelected,
                    onClick = { coroutineScope.launch { pagerState.animateScrollToPage(index) } },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }
        }

        // PAGER FOR EACH LEAGUE
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val currentLeague = leagues[page]

            // Filter and sort dummy/real data:
            // 1. Get score
            // 2. Filter by league
            // 3. Sort descending
            // 4. Take top 10
            val leagueLeaders = allResearchers.map {
                it to (it.papers + (it.citations / 10)) // The formula!
            }.filter { getLeagueBadge(it.first.papers, it.first.citations) == currentLeague }
                .sortedByDescending { it.second }
                .take(10)

            AnimatedLeaderboardList(leaders = leagueLeaders, leagueName = currentLeague)
        }
    }
}

@Composable
fun AnimatedLeaderboardList(leaders: List<Pair<Researcher, Int>>, leagueName: String) {
    // This state triggers the animation every time the page becomes visible
    var startAnimation by remember { mutableStateOf(false) }

    LaunchedEffect(leagueName) {
        startAnimation = false
        delay(50) // Tiny delay to let the pager settle
        startAnimation = true
    }

    if (leaders.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No researchers in this league yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 24.dp, bottom = 100.dp, start = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(leaders) { index, (researcher, score) ->

                // Staggered animation: each item waits a bit longer to animate in
                AnimatedVisibility(
                    visible = startAnimation,
                    enter = slideInVertically(
                        initialOffsetY = { it / 2 },
                        animationSpec = tween(durationMillis = 400, delayMillis = index * 50, easing = FastOutSlowInEasing)
                    ) + fadeIn(animationSpec = tween(durationMillis = 400, delayMillis = index * 50))
                ) {
                    LeaderboardRow(rank = index + 1, researcher = researcher, score = score, leagueName = leagueName)
                }
            }
        }
    }
}

@Composable
fun LeaderboardRow(rank: Int, researcher: Researcher, score: Int, leagueName: String) {
    val isTop3 = rank <= 3
    val badgeColors = getBadgeColors(leagueName)

    Card(
        shape = RoundedCornerShape(if (isTop3) 24.dp else 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (rank == 1) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isTop3) 6.dp else 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(if (isTop3) 20.dp else 16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // RANK NUMBER
            Text(
                text = "#$rank",
                style = if (isTop3) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = if (rank == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.width(40.dp)
            )

            // AVATAR PLACEHOLDER
            Box(
                modifier = Modifier
                    .size(if (isTop3) 56.dp else 48.dp)
                    .background(Brush.linearGradient(badgeColors), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = researcher.name.first().toString(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (isTop3) 24.sp else 20.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // NAME AND FIELD
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = researcher.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = researcher.field,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // SCORE / POINTS
            Column(horizontalAlignment = Alignment.End) {
                if (rank == 1) {
                    Icon(Icons.Default.EmojiEvents, contentDescription = "Trophy", tint = Color(0xFFFFCA28))
                }
                Text(
                    text = "$score pts",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}