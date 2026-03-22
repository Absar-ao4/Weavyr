package com.weavyr.screen.settingspac

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsOfServiceScreen(navController: NavController) {
    val scrollState = rememberScrollState()

    Scaffold(
        modifier = Modifier.systemBarsPadding(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Terms of Service",
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
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Last Updated: October 2023",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            TermsSection(
                title = "1. Acceptance of Terms",
                content = "By accessing or using the Weavyr application, you agree to be bound by these Terms of Service. If you do not agree to these terms, please do not use our services."
            )

            TermsSection(
                title = "2. Professional Conduct",
                content = "Weavyr is a professional networking platform for researchers and academics. Users are expected to maintain a respectful, professional tone. Harassment, spamming, and the distribution of inappropriate or non-academic promotional material will result in immediate account termination."
            )

            TermsSection(
                title = "3. User Accounts and Data",
                content = "You are responsible for maintaining the accuracy of your profile information, including your publications and citations. Weavyr uses this data to calculate your League Badges. Falsifying research credentials is a violation of these terms."
            )

            TermsSection(
                title = "4. Privacy and Content Ownership",
                content = "You retain all rights to the intellectual property you share on Weavyr. However, by uploading content (such as profile pictures or bios), you grant Weavyr a license to display this information to other users within the app to facilitate networking."
            )

            TermsSection(
                title = "5. Automated Matching and Badges",
                content = "Weavyr's matching algorithms and League Badges (Explorer, Innovator, etc.) are provided 'as is'. We do not guarantee specific networking outcomes, job offers, or research collaborations."
            )

            TermsSection(
                title = "6. Termination",
                content = "We reserve the right to suspend or terminate your account at any time, without notice, for conduct that we believe violates these Terms of Service or is harmful to other users of Weavyr, or for any other reason."
            )

            TermsSection(
                title = "7. Contact Us",
                content = "If you have any questions about these Terms, please contact us at we.are.wevyr@gmail.com."
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun TermsSection(title: String, content: String) {
    Column(modifier = Modifier.padding(bottom = 20.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.2f
        )
    }
}