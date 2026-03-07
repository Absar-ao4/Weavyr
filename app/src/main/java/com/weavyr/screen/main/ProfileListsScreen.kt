package com.weavyr.screen.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.weavyr.model.Researcher

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileListsScreen(
    profiles: List<Researcher>,
    actionIcon: ImageVector,
    actionColor: Color,
    emptyText: String,
    onActionClick: (Researcher) -> Unit,
    // ⭐ NEW: Optional secondary button for declining!
    secondaryActionIcon: ImageVector? = null,
    secondaryActionColor: Color? = null,
    onSecondaryActionClick: ((Researcher) -> Unit)? = null
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredList = profiles.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
                it.field.contains(searchQuery, ignoreCase = true)
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            placeholder = { Text("Search profiles...") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        if (filteredList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = emptyText, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)) {
                items(filteredList) { researcher ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Box(
                            modifier = Modifier.size(50.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = researcher.name.firstOrNull()?.toString() ?: "?",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = researcher.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(text = researcher.organization, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                        }

                        // ⭐ SECONDARY BUTTON (e.g. Reject/Decline)
                        if (secondaryActionIcon != null && secondaryActionColor != null && onSecondaryActionClick != null) {
                            IconButton(
                                onClick = { onSecondaryActionClick(researcher) },
                                colors = IconButtonDefaults.iconButtonColors(contentColor = secondaryActionColor)
                            ) {
                                Icon(imageVector = secondaryActionIcon, contentDescription = "Secondary Action")
                            }
                        }

                        // PRIMARY BUTTON (e.g. Accept)
                        IconButton(
                            onClick = { onActionClick(researcher) },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = actionColor.copy(alpha = 0.12f),
                                contentColor = actionColor
                            )
                        ) {
                            Icon(imageVector = actionIcon, contentDescription = "Primary Action")
                        }
                    }
                }
            }
        }
    }
}