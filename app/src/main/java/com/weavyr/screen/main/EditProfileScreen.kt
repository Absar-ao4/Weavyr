package com.weavyr.screen.main

import android.net.Uri
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
import androidx.compose.ui.platform.LocalContext
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
import com.weavyr.screen.components.ProfilePicturePicker
import com.weavyr.viewmodel.MainViewModel

val educationOptions = listOf(
    "High School",
    "Undergraduate (Bachelor's)",
    "Postgraduate (Master's)",
    "Doctorate (PhD)",
    "Post-Doctorate",
    "Professor / Faculty",
    "Industry Professional / Researcher"
)

// --- UPDATED: Added authorOrder ---
data class EditablePaper(
    val title: String,
    val journal: String,
    val publicationYear: String,
    val abstract: String,
    val paperUrl: String,
    val authorOrder: String
)

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
    val context = LocalContext.current

    val user by viewModel.userProfile.collectAsState()
    val isUpdating by viewModel.isUpdating.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val scrollState = rememberScrollState()

    // --- NEW: Profile Picture URI State ---
    var selectedImageUri by remember(user) {
        mutableStateOf<Uri?>(user?.profilePhoto?.let { Uri.parse(it) })
    }

    var name by remember(user) { mutableStateOf(user?.name ?: "") }
    var field by remember(user) { mutableStateOf(user?.field ?: "") }
    var organization by remember(user) { mutableStateOf(user?.organization ?: "") }

    var experienceYears by remember(user) { mutableStateOf(user?.experienceYears?.toString() ?: "") }
    var numberOfPapers by remember(user) { mutableStateOf(user?.numberOfPapers?.toString() ?: "") }
    var totalCitations by remember(user) { mutableStateOf(user?.totalCitations?.toString() ?: "") }

    var education by remember(user) { mutableStateOf(user?.education ?: educationOptions[1]) }
    var educationExpanded by remember { mutableStateOf(false) }

    val selectedInterests = remember(user) {
        val initialList = user?.interests ?: emptyList()
        initialList.toMutableStateList()
    }

    // --- UPDATED: Mapping includes authorOrder ---
    val papersList = remember(user) {
        val initialPapers = user?.papersAuthored?.map {
            EditablePaper(
                title = it.title,
                journal = it.journal ?: "",
                publicationYear = it.publicationYear?.toString() ?: "",
                abstract = it.abstract ?: "",
                paperUrl = it.paperUrl ?: "",
                authorOrder = it.authorOrder?.toString() ?: "1" // Default to 1st author
            )
        } ?: emptyList()
        initialPapers.toMutableStateList()
    }

    var showPaperDialog by remember { mutableStateOf(false) }
    var paperBeingEdited by remember { mutableStateOf<EditablePaper?>(null) }
    var paperEditIndex by remember { mutableIntStateOf(-1) }

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

    var validationError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = { Text("Edit Profile", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground) },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }, enabled = !isUpdating) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState)
                .imePadding()
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // --- PHOTO PICKER COMPONENT ---
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                ProfilePicturePicker(
                    currentPhotoUri = selectedImageUri,
                    onPhotoSelected = { newUri ->
                        selectedImageUri = newUri
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            WeavyrTextField(value = name, onValueChange = { name = it }, label = "Full Name", enabled = !isUpdating)

            ExposedDropdownMenuBox(
                expanded = educationExpanded,
                onExpandedChange = { if (!isUpdating) educationExpanded = it },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                OutlinedTextField(
                    value = education,
                    onValueChange = {},
                    readOnly = true,
                    enabled = !isUpdating,
                    label = { Text("Education / Degree", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = educationExpanded) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                    ),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = educationExpanded,
                    onDismissRequest = { educationExpanded = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                ) {
                    educationOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option, color = MaterialTheme.colorScheme.onBackground) },
                            onClick = { education = option; educationExpanded = false }
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

            // ACHIEVEMENTS
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Achievements & Awards", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                TextButton(onClick = { achievementBeingEdited = null; achievementEditIndex = -1; showAchievementDialog = true }, enabled = !isUpdating) {
                    Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add")
                }
            }

            if (achievementsList.isEmpty()) {
                Text("No achievements added yet.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 16.dp))
            } else {
                achievementsList.forEachIndexed { index, achievement ->
                    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)) {
                        Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(achievement.title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                if (achievement.year.isNotEmpty()) Text(achievement.year, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = { achievementBeingEdited = achievement; achievementEditIndex = index; showAchievementDialog = true }) { Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp)) }
                            IconButton(onClick = { achievementsList.removeAt(index) }) { Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp)) }
                        }
                    }
                }
            }

            // PUBLICATIONS
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Publications", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                TextButton(onClick = { paperBeingEdited = null; paperEditIndex = -1; showPaperDialog = true }, enabled = !isUpdating) {
                    Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add")
                }
            }

            if (papersList.isEmpty()) {
                Text("No publications added yet.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 16.dp))
            } else {
                papersList.forEachIndexed { index, paper ->
                    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)) {
                        Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(paper.title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text("${paper.journal.ifEmpty { "Pre-print" }} • ${paper.publicationYear}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = { paperBeingEdited = paper; paperEditIndex = index; showPaperDialog = true }) { Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp)) }
                            IconButton(onClick = { papersList.removeAt(index) }) { Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp)) }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Text("Research Interests (Select at least 1)", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 8.dp))
            SearchableInterestSelector(
                selectedInterests = selectedInterests,
                onInterestAdded = { newInterest -> if (!isUpdating && !selectedInterests.contains(newInterest)) { selectedInterests.add(newInterest); validationError = null } },
                onInterestRemoved = { removedInterest -> if (!isUpdating) { selectedInterests.remove(removedInterest) } }
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
                        field = field.ifBlank { null },
                        organization = organization.ifBlank { null },
                        experienceYears = experienceYears.toIntOrNull(),
                        numberOfPapers = numberOfPapers.toIntOrNull(),
                        citationCount = totalCitations.toIntOrNull(),
                        interests = selectedInterests.filter { it.length >= 2 }.toList(),

                        achievements = achievementsList.map {
                            AchievementRequest(
                                title = it.title,
                                description = it.description.ifBlank { null },
                                year = it.year.toIntOrNull()
                            )
                        },

                        papersAuthored = papersList.map { p ->
                            PaperRequest(
                                title = p.title,
                                paperUrl = p.paperUrl,
                                authorOrder = p.authorOrder.toIntOrNull() ?: 1,
                                abstract = p.abstract.ifBlank { null },
                                journal = p.journal.ifBlank { null },
                                publicationYear = p.publicationYear.toIntOrNull()
                            )
                        }
                    )

                    // Pass the context and selectedImageUri to the ViewModel!
                    viewModel.updateProfileData(context, selectedImageUri, request) {
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                enabled = !isUpdating
            ) {
                if (isUpdating) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.background, modifier = Modifier.size(24.dp))
                } else {
                    Text("Save Changes", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.background)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

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
        label = { Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant) },
        enabled = enabled,
        singleLine = singleLine,
        minLines = minLines,
        modifier = modifier.fillMaxWidth().padding(bottom = 16.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedTextColor = MaterialTheme.colorScheme.onBackground,
            unfocusedTextColor = MaterialTheme.colorScheme.onBackground
        ),
        shape = MaterialTheme.shapes.medium
    )
}

// --- UPDATED: Publication Dialog now asks for Author Order ---
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

    // NEW: Author Order Field State
    var authorOrder by remember { mutableStateOf(initialPaper?.authorOrder ?: "") }

    var showError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = if (initialPaper == null) "Add Publication" else "Edit Publication",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // REQUIRED FIELDS
                WeavyrTextField(value = title, onValueChange = { title = it; showError = false }, label = "Paper Title (Required)")
                WeavyrTextField(value = url, onValueChange = { url = it; showError = false }, label = "Paper URL (Required)", keyboardType = KeyboardType.Uri)

                // OPTIONAL FIELDS
                WeavyrTextField(
                    value = authorOrder,
                    onValueChange = { if (it.isEmpty() || it.all { char -> char.isDigit() }) authorOrder = it },
                    label = "Author Order (e.g. 1 for First Author)",
                    keyboardType = KeyboardType.Number
                )

                WeavyrTextField(value = journal, onValueChange = { journal = it }, label = "Journal / Conference (Optional)")

                WeavyrTextField(
                    value = year,
                    onValueChange = { if (it.isEmpty() || (it.all { char -> char.isDigit() } && it.length <= 4)) year = it },
                    label = "Publication Year (Optional)",
                    keyboardType = KeyboardType.Number
                )

                WeavyrTextField(
                    value = abstractText,
                    onValueChange = { abstractText = it },
                    label = "Abstract (Optional)",
                    singleLine = false,
                    minLines = 3
                )

                if (showError) {
                    Text("Title and URL are STRICTLY required.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(bottom = 8.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isBlank() || url.isBlank()) {
                                showError = true
                            } else {
                                onSave(EditablePaper(title, journal, year, abstractText, url, authorOrder))
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Save", color = MaterialTheme.colorScheme.background)
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
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = if (initialAchievement == null) "Add Achievement" else "Edit Achievement",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
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
                        Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Save", color = MaterialTheme.colorScheme.background)
                    }
                }
            }
        }
    }
}