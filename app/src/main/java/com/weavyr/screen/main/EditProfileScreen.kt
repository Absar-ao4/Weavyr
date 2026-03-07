package com.weavyr.screen.main

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.weavyr.components.SearchableInterestSelector
import com.weavyr.model.AchievementRequest
import com.weavyr.model.PaperRequest
import com.weavyr.model.UpdateProfileRequest
import com.weavyr.viewmodel.MainViewModel
import com.weavyr.ui.theme.*

val educationOptions = listOf(
    "High School",
    "Undergraduate (Bachelor's)",
    "Postgraduate (Master's)",
    "Doctorate (PhD)",
    "Post-Doctorate",
    "Professor / Faculty",
    "Industry Professional / Researcher"
)

// Data class to hold publication state before saving
data class EditablePaper(
    val title: String,
    val journal: String,
    val publicationYear: String,
    val abstract: String,
    val paperUrl: String
)

// Data class to hold achievement state before saving
data class EditableAchievement(
    val title: String,
    val description: String,
    val year: String
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

    // Scroll State
    val scrollState = rememberScrollState()
    // REMOVED: The LaunchedEffect that was forcefully scrolling to the bottom.

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

    // Multi-Select State for Interests
    val selectedInterests = remember(user) {
        val initialList = user?.interests?.map { it.name } ?: emptyList()
        initialList.toMutableStateList()
    }

    // State for Publications
    val papersList = remember(user) {
        val initialPapers = user?.papersAuthored?.map {
            EditablePaper(
                title = it.title,
                journal = it.journal ?: "",
                publicationYear = it.publicationYear?.toString() ?: "",
                abstract = it.abstract ?: "",
                paperUrl = it.paperUrl ?: ""
            )
        } ?: emptyList()
        initialPapers.toMutableStateList()
    }

    var showPaperDialog by remember { mutableStateOf(false) }
    var paperBeingEdited by remember { mutableStateOf<EditablePaper?>(null) }
    var paperEditIndex by remember { mutableIntStateOf(-1) }

    // State for Achievements
    val achievementsList = remember(user) {
        val initialAchievements = user?.achievements?.map {
            EditableAchievement(
                title = it.title,
                description = it.description ?: "",
                year = it.year?.toString() ?: ""
            )
        } ?: emptyList()
        initialAchievements.toMutableStateList()
    }

    var showAchievementDialog by remember { mutableStateOf(false) }
    var achievementBeingEdited by remember { mutableStateOf<EditableAchievement?>(null) }
    var achievementEditIndex by remember { mutableIntStateOf(-1) }

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
                IconButton(onClick = { navController.popBackStack() }, enabled = !isUpdating) {
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
                .verticalScroll(scrollState)
                .imePadding() // This handles native keyboard padding without forcing a jump
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            WeavyrTextField(value = name, onValueChange = { name = it }, label = "Full Name", enabled = !isUpdating)

            // EDUCATION DROPDOWN
            ExposedDropdownMenuBox(
                expanded = educationExpanded,
                onExpandedChange = { if (!isUpdating) educationExpanded = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                OutlinedTextField(
                    value = education,
                    onValueChange = {},
                    readOnly = true,
                    enabled = !isUpdating,
                    label = { Text("Education / Degree", color = WeavyrTextSecondary) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = educationExpanded) },
                    colors = OutlinedTextFieldDefaults.colors(
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

            WeavyrTextField(value = field, onValueChange = { field = it }, label = "Field of Research", enabled = !isUpdating)
            WeavyrTextField(value = organization, onValueChange = { organization = it }, label = "Current Organization", enabled = !isUpdating)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                WeavyrTextField(
                    value = experienceYears,
                    onValueChange = { if (it.isEmpty() || (it.all { char -> char.isDigit() } && it.length <= 2)) experienceYears = it },
                    label = "Years Exp.",
                    modifier = Modifier.weight(1f),
                    keyboardType = KeyboardType.Number,
                    enabled = !isUpdating
                )
                WeavyrTextField(
                    value = numberOfPapers,
                    onValueChange = { if (it.isEmpty() || (it.all { char -> char.isDigit() } && it.length <= 5)) numberOfPapers = it },
                    label = "Papers",
                    modifier = Modifier.weight(1f),
                    keyboardType = KeyboardType.Number,
                    enabled = !isUpdating
                )
            }

            WeavyrTextField(
                value = totalCitations,
                onValueChange = { if (it.isEmpty() || (it.all { char -> char.isDigit() } && it.length <= 7)) totalCitations = it },
                label = "Total Citations",
                keyboardType = KeyboardType.Number,
                enabled = !isUpdating
            )

            // --- ACHIEVEMENTS SECTION ---
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Achievements & Awards",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = WeavyrTextPrimary
                )
                TextButton(
                    onClick = {
                        achievementBeingEdited = null
                        achievementEditIndex = -1
                        showAchievementDialog = true
                    },
                    enabled = !isUpdating
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add")
                }
            }

            if (achievementsList.isEmpty()) {
                Text(
                    text = "No achievements added yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = WeavyrTextSecondary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            } else {
                achievementsList.forEachIndexed { index, achievement ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = WeavyrSurface),
                        border = BorderStroke(1.dp, WeavyrDivider)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(achievement.title, fontWeight = FontWeight.Bold, color = WeavyrTextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                if (achievement.year.isNotEmpty()) {
                                    Text(achievement.year, style = MaterialTheme.typography.labelMedium, color = WeavyrTextSecondary)
                                }
                            }
                            IconButton(onClick = {
                                achievementBeingEdited = achievement
                                achievementEditIndex = index
                                showAchievementDialog = true
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = WeavyrPrimary, modifier = Modifier.size(20.dp))
                            }
                            IconButton(onClick = { achievementsList.removeAt(index) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }

            // --- PUBLICATIONS SECTION ---
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Publications",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = WeavyrTextPrimary
                )
                TextButton(
                    onClick = {
                        paperBeingEdited = null
                        paperEditIndex = -1
                        showPaperDialog = true
                    },
                    enabled = !isUpdating
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add")
                }
            }

            if (papersList.isEmpty()) {
                Text(
                    text = "No publications added yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = WeavyrTextSecondary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            } else {
                papersList.forEachIndexed { index, paper ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = WeavyrSurface),
                        border = BorderStroke(1.dp, WeavyrDivider)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(paper.title, fontWeight = FontWeight.Bold, color = WeavyrTextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text("${paper.journal.ifEmpty { "Pre-print" }} • ${paper.publicationYear}", style = MaterialTheme.typography.labelMedium, color = WeavyrTextSecondary)
                            }
                            IconButton(onClick = {
                                paperBeingEdited = paper
                                paperEditIndex = index
                                showPaperDialog = true
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = WeavyrPrimary, modifier = Modifier.size(20.dp))
                            }
                            IconButton(onClick = { papersList.removeAt(index) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // MULTI-SELECT SEARCHABLE CHIPS FOR INTERESTS
            Text(
                text = "Research Interests (Select at least 1)",
                style = MaterialTheme.typography.labelLarge,
                color = WeavyrTextSecondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            SearchableInterestSelector(
                selectedInterests = selectedInterests,
                onInterestAdded = { newInterest ->
                    if (!isUpdating && !selectedInterests.contains(newInterest)) {
                        selectedInterests.add(newInterest)
                        validationError = null
                    }
                },
                onInterestRemoved = { removedInterest ->
                    if (!isUpdating) {
                        selectedInterests.remove(removedInterest)
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            val displayError = validationError ?: errorMessage
            displayError?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // SAVE BUTTON
            Button(
                onClick = {
                    if (selectedInterests.isEmpty()) {
                        validationError = "Please select at least one research interest for better matchmaking."
                        return@Button
                    }

                    val request = UpdateProfileRequest(
                        name = name,
                        education = education,
                        field = field, // Sent as "" if empty, Zod accepts strings
                        organization = organization,
                        experienceYears = experienceYears.toIntOrNull() ?: 0,
                        numberOfPapers = numberOfPapers.toIntOrNull() ?: 0,
                        citationCount = totalCitations.toIntOrNull() ?: 0,

                        // Zod requires tags to be at least 2 characters long: z.string().min(2)
                        interests = selectedInterests.filter { it.length >= 2 }.toList(),

                        achievements = achievementsList.map {
                            AchievementRequest(
                                title = it.title,
                                description = it.description, // Removed .ifEmpty { null }
                                year = it.year.toIntOrNull() ?: 0 // Fallback to 0 instead of null
                            )
                        },

                        papersAuthored = papersList.mapIndexed { index, p ->
                            PaperRequest(
                                authorOrder = index + 1,
                                title = p.title,
                                abstract = p.abstract, // Removed .ifEmpty { null }
                                journal = p.journal,   // Removed .ifEmpty { null }
                                publicationYear = p.publicationYear.toIntOrNull() ?: 0, // Fallback to 0
                                paperUrl = p.paperUrl  // Sent as "" if empty
                            )
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

    // --- The Dialog for Adding/Editing a Publication ---
    if (showPaperDialog) {
        PublicationDialog(
            initialPaper = paperBeingEdited,
            onDismiss = { showPaperDialog = false },
            onSave = { updatedPaper ->
                if (paperEditIndex >= 0) {
                    papersList[paperEditIndex] = updatedPaper
                } else {
                    papersList.add(updatedPaper)
                }
                showPaperDialog = false
            }
        )
    }

    // --- The Dialog for Adding/Editing an Achievement ---
    if (showAchievementDialog) {
        AchievementDialog(
            initialAchievement = achievementBeingEdited,
            onDismiss = { showAchievementDialog = false },
            onSave = { updatedAchievement ->
                if (achievementEditIndex >= 0) {
                    achievementsList[achievementEditIndex] = updatedAchievement
                } else {
                    achievementsList.add(updatedAchievement)
                }
                showAchievementDialog = false
            }
        )
    }
}

// Reusable Custom TextField
@Composable
fun WeavyrTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    minLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = WeavyrTextSecondary) },
        enabled = enabled,
        singleLine = singleLine,
        minLines = minLines,
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

// --- Publication Dialog Composable ---
@Composable
fun PublicationDialog(
    initialPaper: EditablePaper?,
    onDismiss: () -> Unit,
    onSave: (EditablePaper) -> Unit
) {
    var title by remember { mutableStateOf(initialPaper?.title ?: "") }
    var journal by remember { mutableStateOf(initialPaper?.journal ?: "") }
    var year by remember { mutableStateOf(initialPaper?.publicationYear ?: "") }
    var abstractText by remember { mutableStateOf(initialPaper?.abstract ?: "") }
    var url by remember { mutableStateOf(initialPaper?.paperUrl ?: "") }

    var showError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = WeavyrBackground,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = if (initialPaper == null) "Add Publication" else "Edit Publication",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = WeavyrTextPrimary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                WeavyrTextField(value = title, onValueChange = { title = it; showError = false }, label = "Paper Title (Required)")
                WeavyrTextField(value = journal, onValueChange = { journal = it }, label = "Journal / Conference")

                WeavyrTextField(
                    value = year,
                    onValueChange = { if (it.isEmpty() || (it.all { char -> char.isDigit() } && it.length <= 4)) year = it },
                    label = "Publication Year",
                    keyboardType = KeyboardType.Number
                )

                WeavyrTextField(value = url, onValueChange = { url = it }, label = "Paper URL (Required)", keyboardType = KeyboardType.Uri)

                WeavyrTextField(
                    value = abstractText,
                    onValueChange = { abstractText = it },
                    label = "Abstract",
                    singleLine = false,
                    minLines = 3
                )

                if (showError) {
                    Text("Title and URL are required fields.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(bottom = 8.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = WeavyrTextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isBlank() || url.isBlank()) {
                                showError = true
                            } else {
                                onSave(EditablePaper(title, journal, year, abstractText, url))
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = WeavyrPrimary)
                    ) {
                        Text("Save", color = WeavyrBackground)
                    }
                }
            }
        }
    }
}

// --- Achievement Dialog Composable ---
@Composable
fun AchievementDialog(
    initialAchievement: EditableAchievement?,
    onDismiss: () -> Unit,
    onSave: (EditableAchievement) -> Unit
) {
    var title by remember { mutableStateOf(initialAchievement?.title ?: "") }
    var description by remember { mutableStateOf(initialAchievement?.description ?: "") }
    var year by remember { mutableStateOf(initialAchievement?.year ?: "") }

    var showError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = WeavyrBackground,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = if (initialAchievement == null) "Add Achievement" else "Edit Achievement",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = WeavyrTextPrimary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                WeavyrTextField(
                    value = title,
                    onValueChange = { title = it; showError = false },
                    label = "Title (Required)"
                )

                WeavyrTextField(
                    value = year,
                    onValueChange = { if (it.isEmpty() || (it.all { char -> char.isDigit() } && it.length <= 4)) year = it },
                    label = "Year",
                    keyboardType = KeyboardType.Number
                )

                WeavyrTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = "Description",
                    singleLine = false,
                    minLines = 3
                )

                if (showError) {
                    Text("Title is required.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(bottom = 8.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = WeavyrTextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isBlank()) {
                                showError = true
                            } else {
                                onSave(EditableAchievement(title, description, year))
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = WeavyrPrimary)
                    ) {
                        Text("Save", color = WeavyrBackground)
                    }
                }
            }
        }
    }
}