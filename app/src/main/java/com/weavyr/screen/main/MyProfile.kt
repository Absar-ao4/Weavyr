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
import androidx.navigation.NavController
import com.weavyr.model.User
import com.weavyr.viewmodel.MainViewModel
import com.weavyr.ui.theme.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MyProfile(
    viewModel: MainViewModel,
    navController: NavController
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // State for the Tabs
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WeavyrBackground)
    ) {
        // TOP BAR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "PROFILE",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                color = WeavyrTextPrimary
            )

            Row {
                IconButton(onClick = { navController.navigate("edit_profile") }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = WeavyrTextPrimary)
                }
                IconButton(onClick = { navController.navigate("settings") }) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = WeavyrTextPrimary)
                }
            }
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = WeavyrPrimary)
            }
        } else {
            userProfile?.let { user ->
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    // 1. Header Section
                    item {
                        ProfileHeader(user = user)
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // 2. Stats & Actions Box
                    item {
                        ProfileStatsAndActions(user = user, navController = navController)
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
                            val tabs = listOf("Overview", "Publications", "Network")
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
                            0 -> OverviewTabContent(user, navController)
                            1 -> PublicationsTabContent(user, navController)
                            2 -> NetworkTabContent()
                        }
                    }
                }
            } ?: run {
                // Fallback if user profile fails to load but isn't loading
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Could not load profile data.", color = WeavyrTextSecondary)
                }
            }
        }
    }
}

@Composable
fun ProfileHeader(user: User) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Smart Avatar: Shows Initials if no photo exists
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
fun ProfileStatsAndActions(user: User, navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = WeavyrCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(user.numberOfPapers?.toString() ?: "0", "PAPERS")
                Divider(modifier = Modifier.height(40.dp).width(1.dp), color = WeavyrDivider)
                StatItem(user.totalCitations?.toString() ?: "0", "CITATIONS")
                if (user.experienceYears != null) {
                    Divider(modifier = Modifier.height(40.dp).width(1.dp), color = WeavyrDivider)
                    StatItem("${user.experienceYears}+", "YEARS EXP")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Divider(color = WeavyrDivider)
            Spacer(modifier = Modifier.height(16.dp))

            // Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ActionItem(Icons.Default.Bookmark, "Saved", WeavyrPrimary) { navController.navigate("bookmarks") }
                ActionItem(Icons.Default.Close, "Passed", WeavyrTextSecondary) { navController.navigate("rejected") }
                ActionItem(Icons.Default.People, "Requests", WeavyrPrimary) { navController.navigate("requests") }
            }
        }
    }
}

@Composable
fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = WeavyrTextPrimary)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = WeavyrTextSecondary, letterSpacing = 1.sp)
    }
}

@Composable
fun ActionItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, tint: androidx.compose.ui.graphics.Color, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(28.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = WeavyrTextSecondary)
    }
}

// --- TAB CONTENTS ---

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OverviewTabContent(user: User, navController: NavController) {
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
        Text("Research Interests", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = WeavyrTextPrimary)
        Spacer(modifier = Modifier.height(8.dp))

        if (user.interests.isNullOrEmpty()) {
            EmptyStateCTA("Add your research interests to match with better collaborators.", "Add Interests") {
                navController.navigate("edit_profile")
            }
        } else {
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
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Achievements Section
        Text("Key Achievements", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = WeavyrTextPrimary)
        Spacer(modifier = Modifier.height(8.dp))

        if (user.achievements.isNullOrEmpty()) {
            EmptyStateCTA("Highlight awards, grants, or milestones.", "Add Achievements") {
                navController.navigate("edit_profile")
            }
        } else {
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
fun PublicationsTabContent(user: User, navController: NavController) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {

        // Persistent Add Button so you can always add a publication
        OutlinedButton(
            onClick = { navController.navigate("edit_profile") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = WeavyrPrimary),
            border = BorderStroke(1.dp, WeavyrPrimary)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add",
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Add Publication",
                fontWeight = FontWeight.Bold
            )
        }

        if (user.papersAuthored.isNullOrEmpty()) {
            EmptyStateCTA("No publications added yet. Showcase your work!", "Add Publication") {
                navController.navigate("edit_profile")
            }
        } else {
            // Real Data fetched from your Hono backend!
            user.papersAuthored.forEach { paper ->
                PaperCard(
                    title = paper.title, // Removed the redundant ?: "Untitled Paper"
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
fun NetworkTabContent() {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text("Matched Collaborators", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = WeavyrTextPrimary)
        Spacer(modifier = Modifier.height(12.dp))

        // Example Matched Collaborator (You can connect this to real data later)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = WeavyrCard),
            border = BorderStroke(1.dp, WeavyrDivider)
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(50.dp).clip(CircleShape).background(WeavyrSurface), contentAlignment = Alignment.Center) {
                    Text("JS", color = WeavyrPrimary, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Dr. John Smith", color = WeavyrTextPrimary, fontWeight = FontWeight.Bold)
                    Text("AI Researcher - MIT", color = WeavyrTextSecondary, style = MaterialTheme.typography.bodySmall)
                }
                IconButton(onClick = { }) {
                    Icon(Icons.Default.Send, contentDescription = "Message", tint = WeavyrPrimary)
                }
            }
        }
    }
}

// --- HELPER COMPOSABLES ---

@Composable
fun EmptyStateCTA(message: String, buttonText: String, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, WeavyrDivider),
        color = WeavyrBackground,
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(message, color = WeavyrTextSecondary, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(12.dp))
            Text(buttonText, color = WeavyrPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun PaperCard(
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
                } else {
                    Text(
                        text = "No abstract available.",
                        style = MaterialTheme.typography.bodySmall,
                        color = WeavyrTextSecondary,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (!paperUrl.isNullOrBlank()) {
                    OutlinedButton(
                        onClick = {
                            try {
                                uriHandler.openUri(paperUrl)
                            } catch (e: Exception) {
                                // Ignore or handle failed URI
                            }
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
            } else if (!abstract.isNullOrBlank() && abstract.length > 80) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${abstract.take(80)}...",
                    style = MaterialTheme.typography.bodySmall,
                    color = WeavyrTextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}