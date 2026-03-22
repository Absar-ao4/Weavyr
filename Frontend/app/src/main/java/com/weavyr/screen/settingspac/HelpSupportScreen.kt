package com.weavyr.screen.settingspac

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.weavyr.screen.main.SettingsItem
import com.weavyr.screen.main.SettingsSectionHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpSupportScreen(navController: NavController) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Scaffold(
        modifier = Modifier.systemBarsPadding(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Help & Support",
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

            SettingsSectionHeader("Contact Us")

            SettingsItem(
                icon = Icons.Default.BugReport,
                title = "Report a Problem",
                onClick = {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:we.are.wevyr@gmail.com")
                        putExtra(Intent.EXTRA_SUBJECT, "Weavyr App Bug Report")
                        putExtra(Intent.EXTRA_TEXT, "Please describe the issue you are facing:\n\n")
                    }
                    context.startActivity(intent)
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            SettingsSectionHeader("Frequently Asked Questions")
            Spacer(modifier = Modifier.height(8.dp))

            FaqItem(
                question = "What are profile badges?",
                answer = "Profile badges reflect your research league! They are automatically calculated based on a combination of your total published papers and citations. As your research impact grows, you'll level up through leagues like Explorer, Innovator, Architect, Luminary, and Visionary."
            )

            FaqItem(
                question = "How do I get more matches?",
                answer = "Make sure your profile is 100% complete! Upload a clear photo, add relevant research interests, define your collaboration roles (Mentor/Mentee/Peer), and actively swipe on the Discover deck."
            )

            FaqItem(
                question = "How do I edit my profile details?",
                answer = "Navigate to the Profile tab on the bottom navigation bar and click on the pencil icon present at the top of the My Profile screen. From there, you can update your roles, publications, achievements, and other details."
            )

            FaqItem(
                question = "Why was a profile hidden?",
                answer = "If you tap 'Hide' or swipe left on a profile, they are moved to your Hidden Profiles list so you won't see them in your main feed. You can unhide them anytime from Settings > Hidden Profiles."
            )

            FaqItem(
                question = "Is my data secure?",
                answer = "Absolutely. Weavyr uses industry-standard encryption for your data. You can completely control what information is public vs. private in your Privacy settings."
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun FaqItem(question: String, answer: String) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .clickable { expanded = !expanded }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = question,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = if (expanded) "Collapse" else "Expand",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(animationSpec = tween(300)),
            exit = shrinkVertically(animationSpec = tween(300))
        ) {
            Column {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = answer,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 20.sp
                )
            }
        }
    }
}