package com.weavyr.screen.main

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(user.username, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onCollaborateClick) {
                        Icon(imageVector = Icons.Default.PersonAdd, contentDescription = "Collaborate", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // --- HEADER ---
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                if (user.profilePhoto.isNullOrEmpty()) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (user.name?.take(1) ?: "U").uppercase(),
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    AsyncImage(
                        model = user.profilePhoto,
                        contentDescription = "Profile Picture",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(text = user.name ?: "Unknown", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = user.organization ?: "Independent", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(user.numberOfPapers?.toString() ?: "0", "PAPERS")

                    VerticalDivider(modifier = Modifier.height(40.dp), color = MaterialTheme.colorScheme.outline)

                    StatItem(user.totalCitations?.toString() ?: "0", "CITATIONS")

                    if (user.experienceYears != null) {
                        VerticalDivider(modifier = Modifier.height(40.dp), color = MaterialTheme.colorScheme.outline)
                        StatItem("${user.experienceYears}+", "YEARS EXP")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // --- TABS ---
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("Overview", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("Publications", fontWeight = FontWeight.Bold) }
                )
            }

            // --- TAB CONTENT ---
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    if (selectedTabIndex == 0) {
                        UserOverviewTabContent(user = user)
                    } else {
                        UserPublicationsTabContent(user = user)
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UserOverviewTabContent(user: User) {
    val uriHandler = LocalUriHandler.current

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {

        // ⭐ BADGE & ROLES SECTION ⭐
        val expertise = getLeagueBadge(user.numberOfPapers ?: 0, user.totalCitations ?: 0)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            LeagueBadge(expertise = expertise)
        }

        if (!user.roles.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                user.roles?.forEach { role ->
                    RoleChip(role = role)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ⭐ SOCIAL LINKS SECTION ⭐
        if (!user.linkedin.isNullOrBlank() || !user.googlescholar.isNullOrBlank()) {
            Text("Connect", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                if (!user.linkedin.isNullOrBlank()) {
                    SocialLinkChip(
                        icon = Icons.Default.Link,
                        text = "LinkedIn",
                        onClick = {
                            try { user.linkedin?.let { uriHandler.openUri(it) } } catch (_: Exception) {}
                        }
                    )
                }
                if (!user.googlescholar.isNullOrBlank()) {
                    SocialLinkChip(
                        icon = Icons.Default.School,
                        text = "Google Scholar",
                        onClick = {
                            try { user.googlescholar?.let { uriHandler.openUri(it) } } catch (_: Exception) {}
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // --- INTERESTS ---
        if (!user.interests.isNullOrEmpty()) {
            Text(text = "Research Interests", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                user.interests?.forEach { interest ->
                    AssistChip(
                        onClick = { },
                        label = { Text(interest) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // --- ACHIEVEMENTS ---
        if (!user.achievements.isNullOrEmpty()) {
            Text(text = "Achievements", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            user.achievements?.forEach { achievement ->
                Row(modifier = Modifier.padding(vertical = 4.dp)) {
                    Icon(imageVector = Icons.Default.EmojiEvents, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(text = achievement.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        if (!achievement.description.isNullOrBlank()) {
                            Text(text = achievement.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (achievement.year != null) {
                            Text(text = achievement.year.toString(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserPublicationsTabContent(user: User) {
    val uriHandler = LocalUriHandler.current

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        if (user.papersAuthored.isNullOrEmpty()) {
            Text(text = "Total Publications: ${user.numberOfPapers ?: 0}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Detailed publication list not available.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            user.papersAuthored?.forEach { paper ->
                var expanded by remember { mutableStateOf(false) }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable { expanded = !expanded }
                        .animateContentSize(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = paper.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(text = paper.journal ?: "Unknown Journal", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                            if (paper.publicationYear != null) {
                                Text(text = paper.publicationYear.toString(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        if (expanded) {
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
                            Spacer(modifier = Modifier.height(12.dp))

                            if (!paper.abstract.isNullOrBlank()) {
                                Text(text = paper.abstract, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 20.sp)
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            if (!paper.paperUrl.isNullOrBlank()) {
                                OutlinedButton(
                                    onClick = {
                                        try { paper.paperUrl?.let { uriHandler.openUri(it) } } catch (_: Exception) {}
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(imageVector = Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Read Full Paper")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}