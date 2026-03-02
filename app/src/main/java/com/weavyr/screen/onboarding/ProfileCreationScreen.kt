package com.weavyr.screen.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.weavyr.ui.theme.*
import kotlinx.coroutines.delay

/* ---------------- DATA STATE ---------------- */

data class ProfileFormState(
    val fullName: String = "",
    val education: String = "",
    val fieldOfWork: String = "",
    val interests: String = "",
    val papersPublished: String = "",
    val citations: String = "",
    val isVerified: Boolean = false
)

/* ---------------- MAIN SCREEN ---------------- */

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ProfileCreationScreen(
    onFinished: () -> Unit
) {

    var currentStep by remember { mutableStateOf(1) }
    var formState by remember { mutableStateOf(ProfileFormState()) }
    var showSuccess by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WeavyrBackground)
            .padding(24.dp)
    ) {

        StepProgressIndicator(currentStep, 4)

        Spacer(modifier = Modifier.height(32.dp))

        AnimatedContent(
            targetState = currentStep,
            transitionSpec = {
                fadeIn(tween(300)) with fadeOut(tween(300))
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

                3 -> StepExpertise(
                    formState = formState,
                    onFormChange = { formState = it },
                    onNext = { currentStep++ },
                    onBack = { currentStep-- }
                )

                4 -> StepVerification(
                    formState = formState,
                    onFormChange = { formState = it },
                    onBack = { currentStep-- },
                    onFinish = { showSuccess = true }
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
    readOnly: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        readOnly = readOnly,
        keyboardOptions = keyboardOptions,
        colors = TextFieldDefaults.colors(
            focusedTextColor = WeavyrTextPrimary,
            unfocusedTextColor = WeavyrTextPrimary,
            focusedContainerColor = WeavyrSurface,
            unfocusedContainerColor = WeavyrSurface,
            focusedIndicatorColor = WeavyrPrimary,
            unfocusedIndicatorColor = WeavyrTextSecondary,
            focusedLabelColor = WeavyrPrimary,
            unfocusedLabelColor = WeavyrTextSecondary,
            cursorColor = WeavyrPrimary
        ),
        modifier = modifier.fillMaxWidth()
    )
}
/* ---------------- STEP 1 ---------------- */

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
            formState.fieldOfWork.isNotBlank()

    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxSize()
    ) {

        Column {

            Text("Basic Information", color = WeavyrTextPrimary)
            Spacer(modifier = Modifier.height(24.dp))

            CustomColoredTextField(
                value = formState.fullName,
                onValueChange = {
                    onFormChange(formState.copy(fullName = it))
                },
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

/* ---------------- STEP 2 ---------------- */

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StepProfessionalInfo(
    formState: ProfileFormState,
    onFormChange: (ProfileFormState) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {

    val popularInterests = listOf("AI", "Biotech", "Physics", "Economics", "Medicine", "Mathematics")

    val selectedInterests = formState.interests
        .split(",")
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .toMutableList()

    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxSize()
    ) {

        Column {
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
        }

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            TextButton(onClick = onBack) { Text("Back") }

            Button(
                onClick = onNext,
                enabled = selectedInterests.isNotEmpty()
            ) {
                Text("Continue")
            }
        }
    }
}

/* ---------------- STEP 3 ---------------- */

@Composable
fun StepExpertise(
    formState: ProfileFormState,
    onFormChange: (ProfileFormState) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {

    val isValid =
        formState.papersPublished.isNotBlank() &&
                formState.citations.isNotBlank()

    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxSize()
    ) {

        Column {

            Text("Expertise Level", color = WeavyrTextPrimary)
            Spacer(modifier = Modifier.height(24.dp))

            CustomColoredTextField(
                value = formState.papersPublished,
                onValueChange = {
                    onFormChange(formState.copy(papersPublished = it))
                },
                label = "Papers Published",
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            CustomColoredTextField(
                value = formState.citations,
                onValueChange = {
                    onFormChange(formState.copy(citations = it))
                },
                label = "Citations Received",
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                )
            )
        }

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            TextButton(onClick = onBack) { Text("Back") }
            Button(onClick = onNext, enabled = isValid) {
                Text("Continue")
            }
        }
    }
}

/* ---------------- STEP 4 ---------------- */

@Composable
fun StepVerification(
    formState: ProfileFormState,
    onFormChange: (ProfileFormState) -> Unit,
    onBack: () -> Unit,
    onFinish: () -> Unit
) {

    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxSize()
    ) {

        Column {

            Text("Verification", color = WeavyrTextPrimary)
            Spacer(modifier = Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {

                Checkbox(
                    checked = formState.isVerified,
                    onCheckedChange = {
                        onFormChange(formState.copy(isVerified = it))
                    },
                    colors = CheckboxDefaults.colors(
                        checkedColor = WeavyrPrimary
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    "Apply for Blue Tick Verification",
                    color = WeavyrTextPrimary
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            TextButton(onClick = onBack) { Text("Back") }
            Button(onClick = onFinish) {
                Text("Finish")
            }
        }
    }
}