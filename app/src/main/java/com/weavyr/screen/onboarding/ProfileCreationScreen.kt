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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.weavyr.components.SearchableInterestSelector
import com.weavyr.model.AchievementRequest
import com.weavyr.model.UpdateProfileRequest
import com.weavyr.repository.UserRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/* ---------------- GLOBAL CONSTANTS ---------------- */

val educationOptions = listOf(
    "High School",
    "Undergraduate (Bachelor's)",
    "Postgraduate (Master's)",
    "Doctorate (PhD)",
    "Post-Doctorate",
    "Professor / Faculty",
    "Industry Professional / Researcher"
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
    var currentStep by remember { mutableIntStateOf(1) }
    var formState by remember { mutableStateOf(ProfileFormState()) }
    var showSuccess by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
            .systemBarsPadding()
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
                // ⭐ STEP 1: Mandatory (Name + Interests)
                1 -> StepEssentials(
                    formState = formState,
                    onFormChange = { formState = it },
                    onNext = { if (currentStep == 1) currentStep = 2 }
                )
                // ⭐ STEP 2: Optional (Edu, Field, Org, Exp)
                2 -> StepProfessionalDetails(
                    formState = formState,
                    onFormChange = { formState = it },
                    onNext = { if (currentStep == 2) currentStep = 3 },
                    onBack = { if (currentStep == 2) currentStep = 1 }
                )
                // ⭐ STEP 3: Optional (Impact)
                3 -> StepImpact(
                    formState = formState,
                    errorMessage = errorMessage,
                    isSubmitting = isSubmitting,
                    onFormChange = { formState = it },
                    onBack = { if (currentStep == 3) currentStep = 2 },
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
                                    interests = formState.interests.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                                    numberOfPapers = formState.papersPublished.toIntOrNull() ?: 0,
                                    citationCount = formState.citations.toIntOrNull() ?: 0,
                                    achievements = formState.achievements
                                        .split(",")
                                        .map { it.trim() }
                                        .filter { it.isNotEmpty() }
                                        .map { AchievementRequest(title = it, description = null, year = null) },
                                    papersAuthored = emptyList()
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

/* ---------------- STEP 1: Essentials (Mandatory) ---------------- */

@Composable
fun StepEssentials(
    formState: ProfileFormState,
    onFormChange: (ProfileFormState) -> Unit,
    onNext: () -> Unit
) {
    val selectedInterests = remember(formState.interests) {
        formState.interests.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }

    // ⭐ Validation: Must have a name AND at least 1 interest
    val isValid = formState.fullName.isNotBlank() && selectedInterests.isNotEmpty()

    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.verticalScroll(rememberScrollState()).weight(1f)) {
            Text("Essential Information", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
            Text("Let's get the basics so we can personalize your feed.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(24.dp))

            CustomColoredTextField(
                value = formState.fullName,
                onValueChange = { onFormChange(formState.copy(fullName = it)) },
                label = "Full Name"
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text("Research Interests", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold)
            Text("Search and select at least one tag.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(16.dp))

            SearchableInterestSelector(
                selectedInterests = selectedInterests,
                onInterestAdded = { newInterest ->
                    val updatedList = selectedInterests.toMutableList().apply { add(newInterest) }
                    onFormChange(formState.copy(interests = updatedList.joinToString(",")))
                },
                onInterestRemoved = { removedInterest ->
                    val updatedList = selectedInterests.toMutableList().apply { remove(removedInterest) }
                    onFormChange(formState.copy(interests = updatedList.joinToString(",")))
                }
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        Button(onClick = onNext, enabled = isValid, modifier = Modifier.fillMaxWidth()) {
            Text("Continue")
        }
    }
}

/* ---------------- STEP 2: Professional Details (Skippable) ---------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StepProfessionalDetails(
    formState: ProfileFormState,
    onFormChange: (ProfileFormState) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    var eduExpanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.verticalScroll(rememberScrollState()).weight(1f)) {
            Text("Professional Details", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
            Text("Feel free to skip this and fill it out later.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(24.dp))

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
                    onDismissRequest = { eduExpanded = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                ) {
                    educationOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option, color = MaterialTheme.colorScheme.onSurface) },
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

            Spacer(modifier = Modifier.height(16.dp))

            CustomColoredTextField(
                value = formState.experienceYears,
                onValueChange = { newValue ->
                    if (newValue.isEmpty() || (newValue.all { it.isDigit() } && newValue.length <= 2)) {
                        onFormChange(formState.copy(experienceYears = newValue))
                    }
                },
                label = "Years of Research Experience",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        // ⭐ Bottom Action Bar with Skip Option
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) {
                Text("Back", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onNext) { // Acts as a skip
                    Text("Skip", color = MaterialTheme.colorScheme.primary)
                }
                Button(onClick = onNext) {
                    Text("Next")
                }
            }
        }
    }
}

/* ---------------- STEP 3: Impact (Skippable) ---------------- */

@Composable
fun StepImpact(
    formState: ProfileFormState,
    errorMessage: String?,
    isSubmitting: Boolean,
    onFormChange: (ProfileFormState) -> Unit,
    onBack: () -> Unit,
    onFinish: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.verticalScroll(rememberScrollState()).weight(1f)) {
            Text("Publication Impact", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
            Text("Highlight your academic achievements (Optional).", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(24.dp))

            CustomColoredTextField(
                value = formState.papersPublished,
                onValueChange = { newValue ->
                    if (newValue.isEmpty() || (newValue.all { it.isDigit() } && newValue.length <= 5)) {
                        onFormChange(formState.copy(papersPublished = newValue))
                    }
                },
                label = "Total Papers Published",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                enabled = !isSubmitting
            )

            Spacer(modifier = Modifier.height(16.dp))

            CustomColoredTextField(
                value = formState.citations,
                onValueChange = { newValue ->
                    if (newValue.isEmpty() || (newValue.all { it.isDigit() } && newValue.length <= 7)) {
                        onFormChange(formState.copy(citations = newValue))
                    }
                },
                label = "Total Citations",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                enabled = !isSubmitting
            )

            Spacer(modifier = Modifier.height(16.dp))

            CustomColoredTextField(
                value = formState.achievements,
                onValueChange = { onFormChange(formState.copy(achievements = it)) },
                label = "Key Achievements (Awards, etc.)",
                singleLine = false,
                modifier = Modifier.height(120.dp),
                enabled = !isSubmitting
            )

            errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
            }
        }

        // ⭐ Bottom Action Bar (Fixed: Removed Skip button)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack, enabled = !isSubmitting) {
                Text("Back", color = if (isSubmitting) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Button(onClick = onFinish, enabled = !isSubmitting) {
                if (isSubmitting) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                else Text("Finish")
            }
        }
    }
}
/* ---------------- HELPERS ---------------- */

@Composable
fun StepProgressIndicator(currentStep: Int, totalSteps: Int) {
    Column {
        Text("Profile Setup", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(totalSteps) { index ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .background(if (index < currentStep) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.small)
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
    singleLine: Boolean = true,
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        readOnly = readOnly,
        singleLine = singleLine,
        enabled = enabled,
        keyboardOptions = keyboardOptions,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
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
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
        Text("Profile Created!", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
    }
}