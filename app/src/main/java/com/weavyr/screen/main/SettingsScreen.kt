package com.weavyr.screen.main

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    onLogout: () -> Unit
) {

    val context = LocalContext.current

    Scaffold(

        topBar = {

            TopAppBar(

                title = { Text("Settings") },

                navigationIcon = {

                    IconButton(onClick = { navController.popBackStack() }) {

                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )

                    }
                }
            )
        }

    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {

            SettingsItem(
                icon = Icons.Default.Person,
                title = "Edit Profile"
            )

            SettingsItem(
                icon = Icons.Default.Notifications,
                title = "Notifications"
            )

            SettingsItem(
                icon = Icons.Default.PrivacyTip,
                title = "Privacy"
            )

            Spacer(modifier = Modifier.height(24.dp))

            Divider()

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {

                    val prefs =
                        context.getSharedPreferences("auth", Context.MODE_PRIVATE)

                    prefs.edit().remove("token").apply()

                    onLogout()

                },
                modifier = Modifier.fillMaxWidth()
            ) {

                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = "Logout"
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text("Logout")

            }

        }

    }

}

@Composable
fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.Start
    ) {

        Icon(icon, contentDescription = title)

        Spacer(modifier = Modifier.width(16.dp))

        Text(title)

    }

}