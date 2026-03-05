package com.weavyr.screen.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.weavyr.model.AchievementRequest
import com.weavyr.model.UpdateProfileRequest
import com.weavyr.repository.UserRepository
import com.weavyr.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

        StepProgressIndicator(currentStep, 3) // Changed to 3 steps

        Spacer(modifier = Modifier.height(32.dp))

        AnimatedContent(
            targetState = currentStep,
            transitionSpec = {
                fadeIn(tween(300)) togetherWith fadeOut(tween(300))
            },
            label = ""
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

                3 -> StepExpertise( // Step 3 is now the final step
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
                                    education = formState.education, // Matches backend
                                    field = formState.fieldOfWork,
                                    organization = formState.organization,
                                    experienceYears = formState.experienceYears.toIntOrNull() ?: 0,
                                    interests = formState.interests
                                        .split(",")
                                        .map { it.trim() }
                                        .filter { it.isNotEmpty() },
                                    numberOfPapers = formState.papersPublished.toIntOrNull() ?: 0, // Matches backend
                                    citationCount = formState.citations.toIntOrNull() ?: 0, // Matches backend
                                    achievements = formState.achievements
                                        .split(",")
                                        .map { it.trim() }
                                        .filter { it.isNotEmpty() }
                                        .map { AchievementRequest(title = it) } // Matches backend object requirement
                                )

                                val response = userRepository.updateProfile(request)

                                if (response.isSuccessful) {
                                    showSuccess = true
                                } else {
                                    errorMessage = response.errorBody()?.string() ?: "Failed to update profile."
                                }
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

    if (showSuccess) {
        SuccessAnimation {
            onFinished()
        }
    }
}

/* ---------------- SUCCESS ANIMATION ---------------- */

@Composable
fun SuccessAnimation(
    onComplete: () -> Unit
) {
    LaunchedEffect(Unit) {
        delay(1800)
        onComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WeavyrBackground),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "Welcome to Weavyr",
            style = MaterialTheme.typography.headlineMedium,
            color = WeavyrPrimary
        )
    }
}

/* ---------------- PROGRESS INDICATOR ---------------- */

@Composable
fun StepProgressIndicator(currentStep: Int, totalSteps: Int) {
    Column {
        Text("Step $currentStep of $totalSteps", color = WeavyrTextSecondary)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(totalSteps) { index ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .background(
                            if (index < currentStep)
                                WeavyrPrimary
                            else
                                WeavyrSurface
                        )
                )
            }
        }
    }
}

/* ---------------- CUSTOM TEXT FIELD ---------------- */

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
            unfocusedBorderColor = WeavyrTextSecondary,
            focusedLabelColor = WeavyrPrimary,
            unfocusedLabelColor = WeavyrTextSecondary,
            cursorColor = WeavyrPrimary
        ),
        modifier = modifier.fillMaxWidth()
    )
}

/* ---------------- STEP 1: Basic Info ---------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StepBasicInfo(
    formState: ProfileFormState,
    onFormChange: (ProfileFormState) -> Unit,
    onNext: () -> Unit
) {

    val educationOptions = listOf("Undergraduate", "Postgraduate", "PhD", "PostDoc")
    val fieldOptions = listOf("AI", "Biotech", "Physics", "Economics", "Medicine")

    var eduExpanded by remember { mutableStateOf(false) }
    var fieldExpanded by remember { mutableStateOf(false) }

    val isValid = formState.fullName.isNotBlank() &&
            formState.education.isNotBlank() &&
            formState.fieldOfWork.isNotBlank() &&
            formState.organization.isNotBlank()

    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxSize()
    ) {

        Column(modifier = Modifier.verticalScroll(rememberScrollState()).weight(1f, false)) {

            Text("Basic Information", color = WeavyrTextPrimary)
            Spacer(modifier = Modifier.height(24.dp))

            CustomColoredTextField(
                value = formState.fullName,
                onValueChange = { onFormChange(formState.copy(fullName = it)) },
                label = "Full Name"
            )

            Spacer(modifier = Modifier.height(16.dp))

            ExposedDropdownMenuBox(
                expanded = eduExpanded,
                onExpandedChange = { eduExpanded = !eduExpanded }
            ) {
                CustomColoredTextField(
                    value = formState.education,
                    onValueChange = {},
                    label = "Education",
                    readOnly = true,
                    modifier = Modifier.menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = eduExpanded,
                    onDismissRequest = { eduExpanded = false }
                ) {
                    educationOptions.forEach {
                        DropdownMenuItem(
                            text = { Text(it) },
                            onClick = {
                                onFormChange(formState.copy(education = it))
                                eduExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            ExposedDropdownMenuBox(
                expanded = fieldExpanded,
                onExpandedChange = { fieldExpanded = !fieldExpanded }
            ) {
                CustomColoredTextField(
                    value = formState.fieldOfWork,
                    onValueChange = {},
                    label = "Field of Work",
                    readOnly = true,
                    modifier = Modifier.menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = fieldExpanded,
                    onDismissRequest = { fieldExpanded = false }
                ) {
                    fieldOptions.forEach {
                        DropdownMenuItem(
                            text = { Text(it) },
                            onClick = {
                                onFormChange(formState.copy(fieldOfWork = it))
                                fieldExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            CustomColoredTextField(
                value = formState.organization,
                onValueChange = { onFormChange(formState.copy(organization = it)) },
                label = "Current Organization / University"
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        Button(
            onClick = onNext,
            enabled = isValid,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Continue")
        }
    }
}

/* ---------------- STEP 2: Professional Info ---------------- */

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StepProfessionalInfo(
    formState: ProfileFormState,
    onFormChange: (ProfileFormState) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {

    val popularInterests = listOf("AI", "Biotech", "Physics", "Economics", "Medicine", "Mathematics", "Robotics", "Genetics")

    val selectedInterests = formState.interests
        .split(",")
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .toMutableList()

    val isValid = formState.experienceYears.isNotBlank() && selectedInterests.isNotEmpty()

    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxSize()
    ) {

        Column(modifier = Modifier.verticalScroll(rememberScrollState()).weight(1f, false)) {

            Text("Professional Details", color = WeavyrTextPrimary)
            Spacer(modifier = Modifier.height(24.dp))

            CustomColoredTextField(
                value = formState.experienceYears,
                onValueChange = { onFormChange(formState.copy(experienceYears = it)) },
                label = "Years of Experience",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text("Select Research Interests", color = WeavyrTextPrimary)
            Spacer(modifier = Modifier.height(16.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                popularInterests.forEach { interest ->

                    val selected = selectedInterests.contains(interest)

                    AssistChip(
                        onClick = {
                            val updated = selectedInterests.toMutableList()
                            if (selected) updated.remove(interest)
                            else updated.add(interest)

                            onFormChange(
                                formState.copy(
                                    interests = updated.joinToString(", ")
                                )
                            )
                        },
                        label = {
                            Text(
                                interest,
                                color = if (selected) Color.White else WeavyrTextPrimary
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor =
                                if (selected) WeavyrPrimary else WeavyrSurface
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            TextButton(onClick = onBack) { Text("Back", color = WeavyrTextPrimary) }

            Button(
                onClick = onNext,
                enabled = isValid
            ) {
                Text("Continue")
            }
        }
    }
}

/* ---------------- STEP 3: Expertise (Final Step) ---------------- */

@Composable
fun StepExpertise(
    formState: ProfileFormState,
    errorMessage: String?,
    isSubmitting: Boolean,
    onFormChange: (ProfileFormState) -> Unit,
    onBack: () -> Unit,
    onFinish: () -> Unit
) {

    val isValid =
        formState.papersPublished.isNotBlank() &&
                formState.citations.isNotBlank() &&
                !isSubmitting

    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxSize()
    ) {

        Column(modifier = Modifier.verticalScroll(rememberScrollState()).weight(1f, false)) {

            Text("Expertise Level", color = WeavyrTextPrimary)
            Spacer(modifier = Modifier.height(24.dp))

            CustomColoredTextField(
                value = formState.papersPublished,
                onValueChange = { onFormChange(formState.copy(papersPublished = it)) },
                label = "Total Papers Authored",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(16.dp))

            CustomColoredTextField(
                value = formState.citations,
                onValueChange = { onFormChange(formState.copy(citations = it)) },
                label = "Total Citations Received",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(16.dp))

            CustomColoredTextField(
                value = formState.achievements,
                onValueChange = { onFormChange(formState.copy(achievements = it)) },
                label = "Achievements (Comma separated)",
                singleLine = false,
                modifier = Modifier.height(100.dp)
            )

            Text(
                "e.g. Best Paper Award 2023, IEEE Member",
                style = MaterialTheme.typography.bodySmall,
                color = WeavyrTextSecondary,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )

            // Show backend errors directly on the final screen
            errorMessage?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = it,
                    color = Color(0xFFFF5A5F),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack, enabled = !isSubmitting) {
                Text("Back", color = WeavyrTextPrimary)
            }

            Button(
                onClick = onFinish,
                enabled = isValid
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Finish")
                }
            }
        }
    }
}