package com.weavyr.screen.settingspac

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.weavyr.screen.main.SettingsSectionHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PushNotificationsScreen(navController: NavController) {
    val scrollState = rememberScrollState()

    var messagesEnabled by remember { mutableStateOf(true) }
    var matchesEnabled by remember { mutableStateOf(true) }
    var profileViewsEnabled by remember { mutableStateOf(false) }
    var appUpdatesEnabled by remember { mutableStateOf(true) }

    Scaffold(
        modifier = Modifier.systemBarsPadding(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Push Notifications",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            SettingsSectionHeader("Interactions")

            SettingsSwitchItem(
                title = "Messages",
                description = "Get notified when someone sends you a message",
                checked = messagesEnabled,
                onCheckedChange = { messagesEnabled = it }
            )

            SettingsSwitchItem(
                title = "New Matches",
                description = "Get notified when you get a new connection",
                checked = matchesEnabled,
                onCheckedChange = { matchesEnabled = it }
            )

            SettingsSwitchItem(
                title = "Profile Views",
                description = "Find out when someone views your profile",
                checked = profileViewsEnabled,
                onCheckedChange = { profileViewsEnabled = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            SettingsSectionHeader("System & Updates")

            SettingsSwitchItem(
                title = "App Updates",
                description = "Receive updates about new features and fixes",
                checked = appUpdatesEnabled,
                onCheckedChange = { appUpdatesEnabled = it }
            )
        }
    }
}

@Composable
fun SettingsSwitchItem(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 12.dp, horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 18.sp
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}