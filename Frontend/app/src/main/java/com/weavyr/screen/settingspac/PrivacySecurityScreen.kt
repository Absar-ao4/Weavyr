package com.weavyr.screen.settingspac

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.weavyr.screen.main.SettingsItem
import com.weavyr.screen.main.SettingsSectionHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacySecurityScreen(navController: NavController) {
    val scrollState = rememberScrollState()

    var privateProfile by remember { mutableStateOf(false) }
    var showOnlineStatus by remember { mutableStateOf(true) }
    var biometricLogin by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.systemBarsPadding(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Privacy & Security",
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

            SettingsSectionHeader("Privacy")

            SettingsSwitchItem(
                title = "Private Profile",
                description = "Only approved connections can see your full profile details",
                checked = privateProfile,
                onCheckedChange = { privateProfile = it }
            )

            SettingsSwitchItem(
                title = "Show Online Status",
                description = "Let others see when you are active on Weavyr",
                checked = showOnlineStatus,
                onCheckedChange = { showOnlineStatus = it }
            )

            SettingsItem(
                icon = Icons.Default.Block,
                title = "Blocked Users",
                onClick = { /* TODO: Navigate to Blocked Users list */ }
            )

            Spacer(modifier = Modifier.height(16.dp))

            SettingsSectionHeader("Security")

            SettingsItem(
                icon = Icons.Default.LockReset,
                title = "Change Password",
                onClick = { /* TODO: Navigate to Change Password screen */ }
            )

            SettingsItem(
                icon = Icons.Default.Security,
                title = "Two-Factor Authentication",
                onClick = { /* TODO: Navigate to 2FA setup */ }
            )

            SettingsSwitchItem(
                title = "Biometric Login",
                description = "Use fingerprint or face unlock to open the app",
                checked = biometricLogin,
                onCheckedChange = { biometricLogin = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            SettingsSectionHeader("Data & Account")

            SettingsItem(
                icon = Icons.Default.Download,
                title = "Download My Data",
                onClick = { /* TODO: Trigger data export API */ }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { /* TODO: Show confirmation dialog for account deletion */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteForever,
                    contentDescription = "Delete Account"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Delete Account", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}