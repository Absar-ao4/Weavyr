package com.weavyr.screen.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.weavyr.model.AchievementRequest
import com.weavyr.model.UpdateProfileRequest
import com.weavyr.repository.UserRepository
import com.weavyr.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/* ---------------- GLOBAL CONSTANTS (Used in Edit & Onboarding) ---------------- */

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

/* ---------------- DATA STATE ---------------- */

data class ProfileFormState(
    val fullName: String = "",
    val education: String = "",
    val fieldOfWork: String = "",
    val organization: String = "",
    val experienceYears: String = "",
    val interests: String = "",
    val papersPublished: String = "",
    val citations: String = "",
    val achievements: String = ""
)

/* ---------------- MAIN SCREEN ---------------- */

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ProfileCreationScreen(
    userRepository: UserRepository,
    onFinished: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var currentStep by remember { mutableStateOf(1) }
    var formState by remember { mutableStateOf(ProfileFormState()) }
    var showSuccess by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WeavyrBackground)
            .padding(24.dp)
    ) {
        StepProgressIndicator(currentStep, 3)
        Spacer(modifier = Modifier.height(32.dp))

        AnimatedContent(
            targetState = currentStep,
            transitionSpec = {
                fadeIn(tween(300)) togetherWith fadeOut(tween(300))
            },
            label = "StepTransition"
        ) { step ->
            when (step) {
                1 -> StepBasicInfo(
                    formState = formState,
                    onFormChange = { formState = it },
                    onNext = { currentStep++ }
                )
                2 -> StepProfessionalInfo(
                    formState = formState,
                    onFormChange = { formState = it },
                    onNext = { currentStep++ },
                    onBack = { currentStep-- }
                )
                3 -> StepExpertise(
                    formState = formState,
                    errorMessage = errorMessage,
                    isSubmitting = isSubmitting,
                    onFormChange = { formState = it },
                    onBack = { currentStep-- },
                    onFinish = {
                        scope.launch {
                            isSubmitting = true
                            errorMessage = null
                            try {
                                val request = UpdateProfileRequest(
                                    name = formState.fullName,
                                    education = formState.education,
                                    field = formState.fieldOfWork,
                                    organization = formState.organization,
                                    experienceYears = formState.experienceYears.toIntOrNull() ?: 0,
                                    interests = formState.interests.split(", ").filter { it.isNotEmpty() },
                                    numberOfPapers = formState.papersPublished.toIntOrNull() ?: 0,
                                    citationCount = formState.citations.toIntOrNull() ?: 0,
                                    achievements = formState.achievements
                                        .split(",")
                                        .map { it.trim() }
                                        .filter { it.isNotEmpty() }
                                        .map { AchievementRequest(title = it) }
                                )
                                val response = userRepository.updateProfile(request)
                                if (response.isSuccessful) showSuccess = true
                                else errorMessage = "Update failed. Please check your inputs."
                            } catch (e: Exception) {
                                errorMessage = "Network Error: Please check your connection."
                            } finally {
                                isSubmitting = false
                            }
                        }
                    }
                )
            }
        }
    }

    if (showSuccess) SuccessAnimation { onFinished() }
}

/* ---------------- STEP 1: Basic Info (Updated with Standard Education) ---------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StepBasicInfo(
    formState: ProfileFormState,
    onFormChange: (ProfileFormState) -> Unit,
    onNext: () -> Unit
) {
    var eduExpanded by remember { mutableStateOf(false) }
    val isValid = formState.fullName.isNotBlank() &&
            formState.education.isNotBlank() &&
            formState.fieldOfWork.isNotBlank() &&
            formState.organization.isNotBlank()

    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.verticalScroll(rememberScrollState()).weight(1f)) {
            Text("Basic Information", style = MaterialTheme.typography.titleLarge, color = WeavyrTextPrimary)
            Spacer(modifier = Modifier.height(24.dp))

            CustomColoredTextField(
                value = formState.fullName,
                onValueChange = { onFormChange(formState.copy(fullName = it)) },
                label = "Full Name"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Standardized Dropdown for Education
            ExposedDropdownMenuBox(
                expanded = eduExpanded,
                onExpandedChange = { eduExpanded = !eduExpanded }
            ) {
                CustomColoredTextField(
                    value = formState.education,
                    onValueChange = {},
                    label = "Education Level",
                    readOnly = true,
                    modifier = Modifier.menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = eduExpanded,
                    onDismissRequest = { eduExpanded = false }
                ) {
                    educationOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                onFormChange(formState.copy(education = option))
                                eduExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            CustomColoredTextField(
                value = formState.fieldOfWork,
                onValueChange = { onFormChange(formState.copy(fieldOfWork = it)) },
                label = "Primary Field of Research (e.g. Physics)"
            )

            Spacer(modifier = Modifier.height(16.dp))

            CustomColoredTextField(
                value = formState.organization,
                onValueChange = { onFormChange(formState.copy(organization = it)) },
                label = "University / Organization"
            )
        }

        Button(onClick = onNext, enabled = isValid, modifier = Modifier.fillMaxWidth()) {
            Text("Continue")
        }
    }
}

/* ---------------- STEP 2: Professional Info (Updated with Chips) ---------------- */

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun StepProfessionalInfo(
    formState: ProfileFormState,
    onFormChange: (ProfileFormState) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val selectedInterests = remember(formState.interests) {
        formState.interests.split(", ").filter { it.isNotEmpty() }.toMutableList()
    }

    val isValid = formState.experienceYears.isNotBlank() && selectedInterests.isNotEmpty()

    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.verticalScroll(rememberScrollState()).weight(1f)) {
            Text("Expertise & Interests", style = MaterialTheme.typography.titleLarge, color = WeavyrTextPrimary)
            Spacer(modifier = Modifier.height(24.dp))

            CustomColoredTextField(
                value = formState.experienceYears,
                onValueChange = { onFormChange(formState.copy(experienceYears = it)) },
                label = "Years of Research Experience",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text("Research Interests", color = WeavyrTextPrimary, fontWeight = FontWeight.Bold)
            Text("Select at least one for ML matching", style = MaterialTheme.typography.bodySmall, color = WeavyrTextSecondary)
            Spacer(modifier = Modifier.height(16.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                availableInterests.forEach { interest ->
                    val selected = selectedInterests.contains(interest)
                    FilterChip(
                        selected = selected,
                        onClick = {
                            val updatedList = selectedInterests.toMutableList()
                            if (selected) updatedList.remove(interest) else updatedList.add(interest)
                            onFormChange(formState.copy(interests = updatedList.joinToString(", ")))
                        },
                        label = { Text(interest) },
                        leadingIcon = if (selected) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = WeavyrPrimary,
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
            TextButton(onClick = onBack, modifier = Modifier.weight(1f)) { Text("Back", color = WeavyrTextPrimary) }
            Button(onClick = onNext, enabled = isValid, modifier = Modifier.weight(1f)) { Text("Continue") }
        }
    }
}

/* ---------------- STEP 3: Expertise (Final) ---------------- */

@Composable
fun StepExpertise(
    formState: ProfileFormState,
    errorMessage: String?,
    isSubmitting: Boolean,
    onFormChange: (ProfileFormState) -> Unit,
    onBack: () -> Unit,
    onFinish: () -> Unit
) {
    val isValid = formState.papersPublished.isNotBlank() && formState.citations.isNotBlank() && !isSubmitting

    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.verticalScroll(rememberScrollState()).weight(1f)) {
            Text("Publication Impact", style = MaterialTheme.typography.titleLarge, color = WeavyrTextPrimary)
            Spacer(modifier = Modifier.height(24.dp))

            CustomColoredTextField(
                value = formState.papersPublished,
                onValueChange = { onFormChange(formState.copy(papersPublished = it)) },
                label = "Total Papers Published",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(16.dp))

            CustomColoredTextField(
                value = formState.citations,
                onValueChange = { onFormChange(formState.copy(citations = it)) },
                label = "Total Citations",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(16.dp))

            CustomColoredTextField(
                value = formState.achievements,
                onValueChange = { onFormChange(formState.copy(achievements = it)) },
                label = "Key Achievements (Awards, etc.)",
                singleLine = false,
                modifier = Modifier.height(120.dp)
            )

            errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
            TextButton(onClick = onBack, enabled = !isSubmitting, modifier = Modifier.weight(1f)) { Text("Back", color = WeavyrTextPrimary) }
            Button(onClick = onFinish, enabled = isValid, modifier = Modifier.weight(1f)) {
                if (isSubmitting) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                else Text("Finish")
            }
        }
    }
}

/* ---------------- HELPERS (Indicators, TextFields, Success) ---------------- */

@Composable
fun StepProgressIndicator(currentStep: Int, totalSteps: Int) {
    Column {
        Text("Profile Setup", style = MaterialTheme.typography.labelMedium, color = WeavyrTextSecondary)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(totalSteps) { index ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .background(if (index < currentStep) WeavyrPrimary else WeavyrSurface, MaterialTheme.shapes.small)
                )
            }
        }
    }
}

@Composable
fun CustomColoredTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    readOnly: Boolean = false,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        readOnly = readOnly,
        singleLine = singleLine,
        keyboardOptions = keyboardOptions,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = WeavyrTextPrimary,
            unfocusedTextColor = WeavyrTextPrimary,
            focusedContainerColor = WeavyrSurface,
            unfocusedContainerColor = WeavyrSurface,
            focusedBorderColor = WeavyrPrimary,
            unfocusedBorderColor = WeavyrDivider,
            focusedLabelColor = WeavyrPrimary,
            unfocusedLabelColor = WeavyrTextSecondary
        ),
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium
    )
}

@Composable
fun SuccessAnimation(onComplete: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(1800)
        onComplete()
    }
    Box(modifier = Modifier.fillMaxSize().background(WeavyrBackground), contentAlignment = Alignment.Center) {
        Text("Profile Created!", style = MaterialTheme.typography.headlineMedium, color = WeavyrPrimary)
    }
}