package com.weavyr.screen.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.weavyr.R
import com.weavyr.model.Researcher
import com.weavyr.ui.theme.WeavyrPrimary
import com.weavyr.ui.theme.WeavyrSurface
import com.weavyr.ui.theme.WeavyrTextPrimary
import com.weavyr.ui.theme.WeavyrTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileListsScreen(
    profiles: List<Researcher>, // Using your old parameter name
    actionIcon: ImageVector,
    actionColor: Color,
    emptyText: String,
    onActionClick: (Researcher) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredList = profiles.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
                it.organization.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // --- SEARCH BAR ---
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(12.dp)),
            placeholder = { Text("Search by name or org...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = null)
                    }
                }
            },
            colors = TextFieldDefaults.textFieldColors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                containerColor = WeavyrSurface,
                focusedTextColor = WeavyrTextPrimary,
                unfocusedTextColor = WeavyrTextPrimary
            ),
            singleLine = true
        )

        // --- THE LIST ---
        if (filteredList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = if (searchQuery.isEmpty()) emptyText else "No results found",
                    color = WeavyrTextSecondary
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(filteredList) { researcher ->
                    ResearcherListCard(
                        researcher = researcher,
                        actionIcon = actionIcon,
                        actionColor = actionColor,
                        onActionClick = { onActionClick(researcher) }
                    )
                }
            }
        }
    }
}

@Composable
fun ResearcherListCard(
    researcher: Researcher,
    actionIcon: ImageVector,
    actionColor: Color,
    onActionClick: () -> Unit
) {
    // Because this file is in the same package (com.weavyr.screen.main) as SwipeStack.kt,
    // it can directly use getLeagueBadge and getBadgeColors!
    val expertise = getLeagueBadge(researcher.papers, researcher.citations)
    val badgeColors = getBadgeColors(expertise)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = WeavyrSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.cardprofileimage),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(10.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = researcher.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = WeavyrTextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = researcher.organization,
                    style = MaterialTheme.typography.bodySmall,
                    color = WeavyrTextSecondary,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(4.dp))

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = badgeColors.first().copy(alpha = 0.1f)
                ) {
                    Text(
                        text = expertise.uppercase(),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = badgeColors.first(),
                        fontWeight = FontWeight.Black
                    )
                }
            }

            IconButton(
                onClick = onActionClick,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = actionColor.copy(alpha = 0.12f),
                    contentColor = actionColor
                )
            ) {
                Icon(
                    imageVector = actionIcon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}