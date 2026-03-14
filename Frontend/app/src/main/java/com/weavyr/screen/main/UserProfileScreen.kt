package com.weavyr.screen.main

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                actions = {
                    IconButton(onClick = onCollaborateClick) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = "Connect",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                item {
                    UserProfileHeader(user = user)
                    Spacer(modifier = Modifier.height(24.dp))
                }

                item {
                    UserProfileStats(user = user)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    TabRow(
                        selectedTabIndex = selectedTabIndex,
                        containerColor = MaterialTheme.colorScheme.background,
                        contentColor = MaterialTheme.colorScheme.primary,
                        indicator = { tabPositions ->
                            TabRowDefaults.Indicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    ) {
                        val tabs = listOf("Overview", "Publications")
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTabIndex == index,
                                onClick = { selectedTabIndex = index },
                                text = {
                                    Text(
                                        title,
                                        fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedTabIndex == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
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
}

@Composable
fun UserProfileHeader(user: User) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center
        ) {
            val initials = user.name?.split(" ")?.take(2)?.joinToString("") { it.take(1) }?.uppercase() ?: "?"
            Text(
                text = initials,
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = user.name ?: "Unknown Researcher",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
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
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(
                    text = listOfNotNull(user.field, user.organization).joinToString(" • "),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    textAlign = TextAlign.Center
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
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            UserStatItem(user.numberOfPapers?.toString() ?: "0", "PAPERS")
            Divider(modifier = Modifier.height(40.dp).width(1.dp), color = MaterialTheme.colorScheme.outlineVariant)
            UserStatItem(user.totalCitations?.toString() ?: "0", "CITATIONS")
            if (user.experienceYears != null) {
                Divider(modifier = Modifier.height(40.dp).width(1.dp), color = MaterialTheme.colorScheme.outlineVariant)
                UserStatItem("${user.experienceYears}+", "YEARS EXP")
            }
        }
    }
}

@Composable
fun UserStatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UserOverviewTabContent(user: User) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {

        // Education
        if (!user.education.isNullOrBlank()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.School, contentDescription = "Education", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = user.education, color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Interests Section
        if (!user.interests.isNullOrEmpty()) {
            Text("Research Interests", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                user.interests.forEach { interest ->
                    AssistChip(
                        onClick = { },
                        label = { Text(interest) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Achievements Section
        if (!user.achievements.isNullOrEmpty()) {
            Text("Key Achievements", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(user.achievements) { achievement ->
                    Card(
                        modifier = Modifier.width(200.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(achievement.title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
                            achievement.year?.let { Text(it.toString(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary) }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(achievement.description ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
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
                text = "No publications available.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 16.dp)
            )
        } else {
            user.papersAuthored.forEach { paper ->
                UserPaperCard(
                    title = paper.title,
                    journal = paper.journal,
                    year = paper.publicationYear?.toString(),
                    abstract = paper.abstract,
                    paperUrl = paper.paperUrl
                )
            }
        }
    }
}

@Composable
fun UserPaperCard(
    title: String,
    journal: String?,
    year: String?,
    abstract: String?,
    paperUrl: String?
) {
    var expanded by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .clickable { expanded = !expanded }
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "${journal ?: "Pre-print"} • ${year ?: "N/A"}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))

                if (!abstract.isNullOrBlank()) {
                    Text(
                        text = abstract,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (!paperUrl.isNullOrBlank()) {
                    OutlinedButton(
                        onClick = {
                            try {
                                uriHandler.openUri(paperUrl)
                            } catch (e: Exception) {}
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(imageVector = Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Read Full Paper")
                    }
                }
            }
        }
    }
}