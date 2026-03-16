package com.weavyr.screen.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.weavyr.model.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    user: User,
    onBackClick: () -> Unit,
    onCollaborateClick: () -> Unit
) {

    var selectedTabIndex by remember { mutableIntStateOf(0) }

    Scaffold(
        modifier = Modifier.systemBarsPadding(),
        topBar = {

            TopAppBar(
                title = { Text(user.name ?: "Researcher") },

                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },

                actions = {
                    IconButton(onClick = onCollaborateClick) {
                        Icon(
                            Icons.Default.PersonAdd,
                            "Collaborate",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            item {

                UserProfileHeader(user)

                Spacer(modifier = Modifier.height(24.dp))
            }

            item {

                UserProfileStats(user)

                Spacer(modifier = Modifier.height(16.dp))
            }

            item {

                val tabs = listOf("Overview", "Publications")

                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    indicator = {
                        TabRowDefaults.Indicator(
                            Modifier.tabIndicatorOffset(it[selectedTabIndex])
                        )
                    }
                ) {

                    tabs.forEachIndexed { index, title ->

                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(title) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            item {

                when (selectedTabIndex) {

                    0 -> UserOverviewTabContent(user)

                    1 -> UserPublicationsTabContent(user)
                }
            }
        }
    }
}

@Composable
fun UserProfileHeader(user: User) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {

        Box(
            modifier = Modifier
                .size(110.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center
        ) {

            if (!user.profilePhoto.isNullOrBlank()) {

                AsyncImage(
                    model = user.profilePhoto,
                    contentDescription = "Profile Photo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

            } else {

                val initials =
                    user.name
                        ?.split(" ")
                        ?.take(2)
                        ?.joinToString("") { it.take(1) }
                        ?.uppercase() ?: "?"

                Text(
                    text = initials,
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = user.name ?: "Researcher",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "@${user.username}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (!user.organization.isNullOrBlank() || !user.field.isNullOrBlank()) {

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ) {

                Text(
                    text = listOfNotNull(user.field, user.organization)
                        .joinToString(" • "),
                    modifier = Modifier.padding(10.dp)
                )
            }
        }
    }
}

@Composable
fun UserProfileStats(user: User) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {

            UserStatItem(
                user.numberOfPapers?.toString() ?: "0",
                "PAPERS"
            )

            UserStatItem(
                user.totalCitations?.toString() ?: "0",
                "CITATIONS"
            )

            UserStatItem(
                "${user.experienceYears ?: 0}+",
                "YEARS"
            )
        }
    }
}

@Composable
fun UserStatItem(value: String, label: String) {

    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UserOverviewTabContent(user: User) {

    Column(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {

        if (!user.interests.isNullOrEmpty()) {

            Text(
                "Research Interests",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow {

                user.interests.forEach {

                    AssistChip(
                        onClick = {},
                        label = { Text(it) }
                    )

                    Spacer(modifier = Modifier.width(8.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (!user.achievements.isNullOrEmpty()) {

            Text(
                "Achievements",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow {

                items(user.achievements.orEmpty()) { achievement ->

                    Card(
                        modifier = Modifier
                            .width(200.dp)
                            .padding(end = 12.dp)
                    ) {

                        Column(modifier = Modifier.padding(12.dp)) {

                            Text(
                                achievement.title,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                achievement.year?.toString() ?: "",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserPublicationsTabContent(user: User) {

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {

        if (user.papersAuthored.isNullOrEmpty()) {

            Text(
                text = "Total Publications: ${user.numberOfPapers ?: 0}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Detailed publication list not available.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

        } else {

            user.papersAuthored.forEach { paper ->

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {

                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {

                        Text(
                            paper.title,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}