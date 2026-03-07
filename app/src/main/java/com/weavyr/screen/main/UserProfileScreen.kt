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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weavyr.model.User
import com.weavyr.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    user: User,
    onBackClick: () -> Unit
) {
    // State for the Tabs (Only 2 tabs now: Overview & Publications)
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WeavyrBackground)
    ) {
        // TOP APP BAR
        TopAppBar(
            title = { },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = WeavyrTextPrimary)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = WeavyrBackground)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // 1. Header Section
            item {
                UserProfileHeader(user = user)
                Spacer(modifier = Modifier.height(24.dp))
            }

            // 2. Stats Box (No action buttons)
            item {
                UserProfileStats(user = user)
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 3. Tabs Row
            item {
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = WeavyrBackground,
                    contentColor = WeavyrPrimary,
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = WeavyrPrimary
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
                                    color = if (selectedTabIndex == index) WeavyrPrimary else WeavyrTextSecondary
                                )
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 4. Tab Content
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
        modifier = Modifier.fillMaxWidth()
    ) {
        // Smart Avatar
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(WeavyrSurface),
            contentAlignment = Alignment.Center
        ) {
            val initials = user.name?.split(" ")?.take(2)?.joinToString("") { it.take(1) }?.uppercase() ?: "?"
            Text(
                text = initials,
                style = MaterialTheme.typography.headlineLarge,
                color = WeavyrPrimary,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = user.name ?: "Unknown Researcher",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = WeavyrTextPrimary
        )

        Text(
            text = "@${user.username}",
            style = MaterialTheme.typography.bodyMedium,
            color = WeavyrTextSecondary
        )

        if (!user.organization.isNullOrBlank() || !user.field.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = WeavyrPrimary.copy(alpha = 0.1f),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(
                    text = listOfNotNull(user.field, user.organization).joinToString(" • "),
                    style = MaterialTheme.typography.labelLarge,
                    color = WeavyrPrimary,
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
        colors = CardDefaults.cardColors(containerColor = WeavyrCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            UserStatItem(user.numberOfPapers?.toString() ?: "0", "PAPERS")
            Divider(modifier = Modifier.height(40.dp).width(1.dp), color = WeavyrDivider)
            UserStatItem(user.totalCitations?.toString() ?: "0", "CITATIONS")
            if (user.experienceYears != null) {
                Divider(modifier = Modifier.height(40.dp).width(1.dp), color = WeavyrDivider)
                UserStatItem("${user.experienceYears}+", "YEARS EXP")
            }
        }
    }
}

@Composable
fun UserStatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = WeavyrTextPrimary)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = WeavyrTextSecondary, letterSpacing = 1.sp)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UserOverviewTabContent(user: User) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {

        // Education
        if (!user.education.isNullOrBlank()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.School, contentDescription = "Education", tint = WeavyrTextSecondary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = user.education, color = WeavyrTextPrimary, style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Interests Section
        if (!user.interests.isNullOrEmpty()) {
            Text("Research Interests", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = WeavyrTextPrimary)
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                user.interests.forEach { interest ->
                    AssistChip(
                        onClick = { },
                        label = { Text(interest.name) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = WeavyrSurface)
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Achievements Section
        if (!user.achievements.isNullOrEmpty()) {
            Text("Key Achievements", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = WeavyrTextPrimary)
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(user.achievements) { achievement ->
                    Card(
                        modifier = Modifier.width(200.dp),
                        colors = CardDefaults.cardColors(containerColor = WeavyrSurface)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(achievement.title, fontWeight = FontWeight.Bold, color = WeavyrTextPrimary, maxLines = 1)
                            achievement.year?.let { Text(it.toString(), style = MaterialTheme.typography.labelSmall, color = WeavyrPrimary) }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(achievement.description ?: "", style = MaterialTheme.typography.bodySmall, color = WeavyrTextSecondary, maxLines = 2)
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
                color = WeavyrTextSecondary,
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

// Reusing the same PaperCard visual, just renamed to prevent conflicts if kept in same file,
// though you can extract this to a common UI components file later!
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
        colors = CardDefaults.cardColors(containerColor = WeavyrSurface),
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
                        .background(WeavyrPrimary.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = null,
                        tint = WeavyrPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = WeavyrTextPrimary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "${journal ?: "Pre-print"} • ${year ?: "N/A"}",
                        style = MaterialTheme.typography.labelMedium,
                        color = WeavyrPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = WeavyrDivider, thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))

                if (!abstract.isNullOrBlank()) {
                    Text(
                        text = abstract,
                        style = MaterialTheme.typography.bodySmall,
                        color = WeavyrTextSecondary,
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
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = WeavyrPrimary),
                        border = BorderStroke(1.dp, WeavyrPrimary)
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