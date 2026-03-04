package com.weavyr.screen.main

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.weavyr.ui.theme.*
import com.weavyr.viewmodel.MainViewModel
import com.weavyr.screen.components.LogoutButton

@Composable
fun MyProfile(
    viewModel: MainViewModel,
    navController: NavController,
    onLogout: () -> Unit
) {

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WeavyrBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(40.dp))

        // Profile Picture
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(WeavyrSurface),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile",
                tint = WeavyrTextSecondary,
                modifier = Modifier.size(60.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Name
        Text(
            text = "Absar Ali",
            style = MaterialTheme.typography.headlineSmall,
            color = WeavyrTextPrimary
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Email
        Text(
            text = "absar@weavyr.ai",
            style = MaterialTheme.typography.bodyMedium,
            color = WeavyrTextSecondary
        )

        Spacer(modifier = Modifier.height(40.dp))

        ProfileOption(Icons.Default.Edit, "Edit Profile") { }

        ProfileOption(Icons.Default.MenuBook, "My Research") { }

        ProfileOption(Icons.Default.Bookmark, "Bookmarks") { }

        ProfileOption(Icons.Default.Settings, "Settings") { }

        ProfileOption(Icons.Default.Help, "Help & Support") { }

        Spacer(modifier = Modifier.height(30.dp))

        LogoutButton(onLogout)

    }
}

@Composable
fun ProfileOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = WeavyrTextPrimary
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = WeavyrTextPrimary
        )
    }
}