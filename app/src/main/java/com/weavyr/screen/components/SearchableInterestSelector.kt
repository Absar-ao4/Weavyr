package com.weavyr.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.weavyr.data.allResearchInterests
import com.weavyr.ui.theme.*

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SearchableInterestSelector(
    selectedInterests: List<String>,
    onInterestAdded: (String) -> Unit,
    onInterestRemoved: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    // Filter the massive list based on search query (limit to top 5 results for UI cleanliness)
    val filteredInterests = remember(searchQuery) {
        if (searchQuery.isBlank()) emptyList()
        else allResearchInterests
            .filter { it.contains(searchQuery, ignoreCase = true) }
            .filter { !selectedInterests.contains(it) }
            .take(5)
    }

    Column(modifier = Modifier.fillMaxWidth()) {

        // 1. Display Selected Chips (Using the Custom Bulletproof Row)
        if (selectedInterests.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                selectedInterests.forEach { interest ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .background(WeavyrSurface, RoundedCornerShape(8.dp))
                            .border(1.dp, WeavyrPrimary, RoundedCornerShape(8.dp)) // Cyan border
                            .clickable { onInterestRemoved(interest) }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = interest,
                            color = WeavyrTextPrimary,
                            style = MaterialTheme.typography.labelMedium
                        )
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove $interest",
                            tint = WeavyrPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // 2. Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search 1000+ interests...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = WeavyrTextSecondary) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = WeavyrSurface,
                unfocusedContainerColor = WeavyrSurface,
                focusedBorderColor = WeavyrPrimary, // Cyan focus
                unfocusedBorderColor = WeavyrDivider,
                focusedTextColor = WeavyrTextPrimary,
                unfocusedTextColor = WeavyrTextPrimary
            ),
            shape = RoundedCornerShape(12.dp)
        )

        // 3. Dropdown Results
        if (filteredInterests.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = WeavyrSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, WeavyrDivider, RoundedCornerShape(12.dp))
            ) {
                LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                    items(filteredInterests) { interest ->
                        Text(
                            text = interest,
                            color = WeavyrTextPrimary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onInterestAdded(interest)
                                    searchQuery = "" // Clear search after adding
                                }
                                .padding(16.dp)
                        )
                        HorizontalDivider(color = WeavyrDivider)
                    }
                }
            }
        }
    }
}