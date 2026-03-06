package com.weavyr.screen.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.weavyr.components.SearchableInterestSelector
import com.weavyr.model.AchievementRequest
import com.weavyr.model.UpdateProfileRequest
import com.weavyr.viewmodel.MainViewModel
import com.weavyr.ui.theme.*

// These lists standardize your data for your ML clustering model!
val educationOptions = listOf(
    "High School",
    "Undergraduate (Bachelor's)",
    "Postgraduate (Master's)",
    "Doctorate (PhD)",
    "Post-Doctorate",
    "Professor / Faculty",
    "Industry Professional / Researcher"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    viewModel: MainViewModel,
    navController: NavController
) {
    val user by viewModel.userProfile.collectAsState()
    val isUpdating by viewModel.isUpdating.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // Basic Text Fields
    var name by remember(user) { mutableStateOf(user?.name ?: "") }
    var field by remember(user) { mutableStateOf(user?.field ?: "") }
    var organization by remember(user) { mutableStateOf(user?.organization ?: "") }

    // Numbers
    var experienceYears by remember(user) { mutableStateOf(user?.experienceYears?.toString() ?: "") }
    var numberOfPapers by remember(user) { mutableStateOf(user?.numberOfPapers?.toString() ?: "") }
    var totalCitations by remember(user) { mutableStateOf(user?.totalCitations?.toString() ?: "") }

    // Dropdown State for Education
    var education by remember(user) { mutableStateOf(user?.education ?: educationOptions[1]) }
    var educationExpanded by remember { mutableStateOf(false) }

    // --- FIXED: Multi-Select State for Interests using toMutableStateList() ---
    val selectedInterests = remember(user) {
        val initialList = user?.interests?.map { it.name } ?: emptyList()
        initialList.toMutableStateList()
    }

    // Validation State
    var validationError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WeavyrBackground)
    ) {
        // TOP APP BAR
        TopAppBar(
            title = { Text("Edit Profile", fontWeight = FontWeight.Bold, color = WeavyrTextPrimary) },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = WeavyrTextPrimary)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = WeavyrBackground)
        )

        // FORM
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            WeavyrTextField(value = name, onValueChange = { name = it }, label = "Full Name")

            // --- 1. EDUCATION DROPDOWN ---
            ExposedDropdownMenuBox(
                expanded = educationExpanded,
                onExpandedChange = { educationExpanded = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                OutlinedTextField(
                    value = education,
                    onValueChange = {}, // Read-only, so empty
                    readOnly = true,
                    label = { Text("Education / Degree", color = WeavyrTextSecondary) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = educationExpanded) },
                    colors = OutlinedTextFieldDefaults.colors( // M3 Compatible
                        focusedBorderColor = WeavyrPrimary,
                        unfocusedBorderColor = WeavyrDivider,
                        focusedTextColor = WeavyrTextPrimary,
                        unfocusedTextColor = WeavyrTextPrimary
                    ),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = educationExpanded,
                    onDismissRequest = { educationExpanded = false },
                    modifier = Modifier.background(WeavyrSurface)
                ) {
                    educationOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option, color = WeavyrTextPrimary) },
                            onClick = {
                                education = option
                                educationExpanded = false
                            }
                        )
                    }
                }
            }

            WeavyrTextField(value = field, onValueChange = { field = it }, label = "Field of Research")
            WeavyrTextField(value = organization, onValueChange = { organization = it }, label = "Current Organization")

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                WeavyrTextField(
                    value = experienceYears,
                    onValueChange = { experienceYears = it },
                    label = "Years Exp.",
                    modifier = Modifier.weight(1f),
                    keyboardType = KeyboardType.Number
                )
                WeavyrTextField(
                    value = numberOfPapers,
                    onValueChange = { numberOfPapers = it },
                    label = "Papers",
                    modifier = Modifier.weight(1f),
                    keyboardType = KeyboardType.Number
                )
            }

            WeavyrTextField(
                value = totalCitations,
                onValueChange = { totalCitations = it },
                label = "Total Citations",
                keyboardType = KeyboardType.Number
            )

            // --- 2. MULTI-SELECT SEARCHABLE CHIPS FOR INTERESTS ---
            Text(
                text = "Research Interests (Select at least 1)",
                style = MaterialTheme.typography.labelLarge,
                color = WeavyrTextSecondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            SearchableInterestSelector(
                selectedInterests = selectedInterests,
                onInterestAdded = { newInterest ->
                    if (!selectedInterests.contains(newInterest)) {
                        selectedInterests.add(newInterest)
                        validationError = null // Clear error when they interact
                    }
                },
                onInterestRemoved = { removedInterest ->
                    selectedInterests.remove(removedInterest)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Display either our local frontend error, or the backend error
            val displayError = validationError ?: errorMessage
            displayError?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // SAVE BUTTON
            Button(
                onClick = {
                    // FRONTEND VALIDATION
                    if (selectedInterests.isEmpty()) {
                        validationError = "Please select at least one research interest for better matchmaking."
                        return@Button
                    }

                    val request = UpdateProfileRequest(
                        name = name,
                        education = education,
                        field = field,
                        organization = organization,
                        experienceYears = experienceYears.toIntOrNull(),
                        numberOfPapers = numberOfPapers.toIntOrNull(),
                        citationCount = totalCitations.toIntOrNull(),
                        interests = selectedInterests.toList(), // Pass the selected chips!
                        achievements = user?.achievements?.map {
                            AchievementRequest(title = it.title, description = it.description, year = it.year)
                        }
                    )

                    viewModel.updateProfileData(request) {
                        navController.popBackStack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = WeavyrPrimary),
                enabled = !isUpdating
            ) {
                if (isUpdating) {
                    CircularProgressIndicator(color = WeavyrBackground, modifier = Modifier.size(24.dp))
                } else {
                    Text("Save Changes", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = WeavyrBackground)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// Reusable Custom TextField (Fixed for M3 compatibility)
@Composable
fun WeavyrTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = WeavyrTextSecondary) },
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = WeavyrPrimary,
            unfocusedBorderColor = WeavyrDivider,
            cursorColor = WeavyrPrimary,
            focusedTextColor = WeavyrTextPrimary,
            unfocusedTextColor = WeavyrTextPrimary
        ),
        shape = MaterialTheme.shapes.medium
    )
}