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
import com.weavyr.model.AchievementRequest
import com.weavyr.model.UpdateProfileRequest
import com.weavyr.viewmodel.MainViewModel
import com.weavyr.ui.theme.*

// These lists standardize your data for your ML clustering model!
// COPY THESE LISTS to your ProfileCreationScreen too!
val educationOptions = listOf(
    "High School",
    "Undergraduate (Bachelor's)",
    "Postgraduate (Master's)",
    "Doctorate (PhD)",
    "Post-Doctorate",
    "Professor / Faculty",
    "Industry Professional / Researcher"
)

val availableInterests = listOf(
    "Artificial Intelligence", "Machine Learning", "Data Science", "Computer Vision",
    "Natural Language Processing", "Robotics", "Cybersecurity", "Blockchain",
    "Quantum Computing", "Bioinformatics", "Neuroscience", "Genetics",
    "Nanotechnology", "Renewable Energy", "Climate Science", "Astrophysics",
    "Materials Science", "Psychology", "Sociology", "Economics",
    "Mathematics", "Physics", "Chemistry", "Public Health", "Biomedical Engineering"
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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

    // --- NEW: Dropdown State for Education ---
    var education by remember(user) { mutableStateOf(user?.education ?: educationOptions[1]) }
    var educationExpanded by remember { mutableStateOf(false) }

    // --- NEW: Multi-Select State for Interests ---
    // We use a mutableStateListOf to track which chips are selected
    val selectedInterests = remember(user) {
        val initialList = user?.interests?.map { it.name } ?: emptyList()
        mutableStateListOf<String>().apply { addAll(initialList) }
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
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                OutlinedTextField(
                    value = education,
                    onValueChange = {}, // Read-only, so empty
                    readOnly = true,
                    label = { Text("Education / Degree", color = WeavyrTextSecondary) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = educationExpanded) },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = WeavyrPrimary,
                        unfocusedBorderColor = WeavyrDivider,
                        focusedTextColor = WeavyrTextPrimary,
                        unfocusedTextColor = WeavyrTextPrimary
                    ),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.menuAnchor().fillMaxWidth()
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

            // --- 2. MULTI-SELECT CHIPS FOR INTERESTS ---
            Text(
                text = "Research Interests (Select at least 1)",
                style = MaterialTheme.typography.labelLarge,
                color = WeavyrTextSecondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                availableInterests.forEach { interest ->
                    val isSelected = selectedInterests.contains(interest)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            if (isSelected) selectedInterests.remove(interest)
                            else selectedInterests.add(interest)
                            validationError = null // Clear error when they interact
                        },
                        label = { Text(interest) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = WeavyrPrimary.copy(alpha = 0.2f),
                            selectedLabelColor = WeavyrPrimary,
                            containerColor = WeavyrBackground,
                            labelColor = WeavyrTextPrimary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) WeavyrPrimary else WeavyrDivider,
                            borderWidth = 1.dp
                        )
                    )
                }
            }

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
                        education = education, // Now strictly from the dropdown!
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

// Reusable Custom TextField
@OptIn(ExperimentalMaterial3Api::class)
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
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = WeavyrPrimary,
            unfocusedBorderColor = WeavyrDivider,
            cursorColor = WeavyrPrimary,
            focusedTextColor = WeavyrTextPrimary,
            unfocusedTextColor = WeavyrTextPrimary
        ),
        shape = MaterialTheme.shapes.medium
    )
}