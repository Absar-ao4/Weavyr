package com.weavyr.screen.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.weavyr.viewmodel.MainViewModel
import com.weavyr.ui.theme.*

@Composable
fun UserProfileScreen(viewModel: MainViewModel) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        // 🔍 Search Bar
        OutlinedTextField(
            value = "",
            onValueChange = {},
            placeholder = { Text("Search profiles...") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        ProfileSection(
            title = "⭐ Bookmarked",
            profiles = viewModel.bookmarkedProfiles
        )

        ProfileSection(
            title = "🤝 Connection Requests",
            profiles = viewModel.connectionRequests
        )

        ProfileSection(
            title = "❌ Rejected",
            profiles = viewModel.rejectedProfiles
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "✨ Suggested Profiles",
            style = MaterialTheme.typography.titleMedium,
            color = WeavyrTextPrimary
        )

        Spacer(modifier = Modifier.height(12.dp))

        viewModel.bookmarkedProfiles.forEach { profile ->
            SimpleProfileCard(profile)
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun ProfileSection(
    title: String,
    profiles: List<Researcher>
) {

    var expanded by remember { mutableStateOf(false) }

    if (profiles.isEmpty()) return

    Spacer(modifier = Modifier.height(16.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$title (${profiles.size})",
            style = MaterialTheme.typography.titleMedium,
            color = WeavyrTextPrimary
        )

        TextButton(onClick = { expanded = !expanded }) {
            Text(if (expanded) "Show Less" else "View All")
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    if (!expanded) {
        LazyRow {
            items(profiles.take(5)) {
                SimpleProfileCard(it)
                Spacer(modifier = Modifier.width(12.dp))
            }
        }
    } else {
        Column {
            profiles.forEach {
                SimpleProfileCard(it)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun SimpleProfileCard(profile: Researcher) {

    Card(
        modifier = Modifier
            .width(260.dp),
        colors = CardDefaults.cardColors(
            containerColor = WeavyrSurface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = profile.name,
                style = MaterialTheme.typography.titleMedium,
                color = WeavyrTextPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = profile.organization,
                color = WeavyrTextSecondary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = profile.field,
                color = WeavyrTextSecondary
            )
        }
    }
}