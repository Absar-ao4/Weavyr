package com.weavyr.screen.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.weavyr.viewmodel.MainViewModel
import com.weavyr.ui.theme.*

@Composable
fun MyProfile(
    viewModel: MainViewModel,
    navController: NavController
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WeavyrBackground)
            .padding(top=16.dp)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = "PROFILE",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp,
                color = WeavyrTextPrimary
            )

            IconButton(
                onClick = { navController.navigate("settings") }
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = WeavyrTextPrimary
                )
            }
        }

        ProfileHeader()

        Spacer(modifier = Modifier.height(24.dp))

        ResearchStats()

        Spacer(modifier = Modifier.height(24.dp))

        ProfileActions(navController)

        Spacer(modifier = Modifier.height(24.dp))

        Divider(color = WeavyrDivider)

        Spacer(modifier = Modifier.height(24.dp))

        CollaboratorsSection()
    }
}

@Composable
fun ProfileHeader() {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {

        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(WeavyrSurface)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "@absar",
            style = MaterialTheme.typography.titleMedium,
            color = WeavyrTextPrimary
        )

        Text(
            text = "Absar Ali",
            style = MaterialTheme.typography.bodyMedium,
            color = WeavyrTextSecondary
        )
    }
}

@Composable
fun ResearchStats() {

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {

        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Text(
                "12",
                style = MaterialTheme.typography.titleLarge,
                color = WeavyrTextPrimary
            )

            Text(
                "Papers",
                color = WeavyrTextSecondary
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Text(
                "340",
                style = MaterialTheme.typography.titleLarge,
                color = WeavyrTextPrimary
            )

            Text(
                "Citations",
                color = WeavyrTextSecondary
            )
        }
    }
}

@Composable
fun ProfileActions(navController: NavController) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {

        IconButton(
            onClick = { navController.navigate("bookmarks") }
        ) {
            Icon(Icons.Default.Bookmark, null, tint = WeavyrPrimary)
        }

        IconButton(
            onClick = { navController.navigate("rejected") }
        ) {
            Icon(Icons.Default.Close, null, tint = WeavyrTextSecondary)
        }

        IconButton(
            onClick = { navController.navigate("requests") }
        ) {
            Icon(Icons.Default.People, null, tint = WeavyrPrimary)
        }
    }
}

@Composable
fun CollaboratorsSection() {

    Column(modifier = Modifier.padding(16.dp)){

        Text(
            text = "Matched Collaborators",
            style = MaterialTheme.typography.titleMedium,
            color = WeavyrTextPrimary
        )

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = WeavyrCard
            )
        ) {

            Column(modifier = Modifier.padding(16.dp)) {

                Text(
                    text = "Dr. John Smith",
                    color = WeavyrTextPrimary,
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "AI Researcher - MIT",
                    color = WeavyrTextSecondary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WeavyrPrimary
                    )
                ) {

                    Text(
                        "Collaborate",
                        color = WeavyrTextPrimary
                    )
                }
            }
        }
    }
}