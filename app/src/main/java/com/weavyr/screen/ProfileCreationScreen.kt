package com.weavyr.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.with
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.weavyr.ui.theme.*

data class ProfileFormState(
    val fullName: String = "",
    val education: String = "",
    val fieldOfWork: String = "",
    val interests: String = "",
    val papersPublished: String = "",
    val citations: String = "",
    val isVerified: Boolean = false
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ProfileCreationScreen() {

    var currentStep by remember { mutableStateOf(1) }
    var formState by remember { mutableStateOf(ProfileFormState()) }

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
                fadeIn(animationSpec = tween(300)) with
                        fadeOut(animationSpec = tween(300))
            },
            label = ""
        ) { step ->

            when (step) {

                1 -> StepBasicInfo(
                    formState,
                    onFormChange = { formState = it },
                    onNext = { currentStep++ }
                )

                2 -> StepProfessionalInfo(
                    formState,
                    onFormChange = { formState = it },
                    onNext = { currentStep++ },
                    onBack = { currentStep-- }
                )

                3 -> StepExpertise(
                    formState,
                    onFormChange = { formState = it },
                    onNext = { currentStep++ },
                    onBack = { currentStep-- }
                )

                4 -> StepVerification(
                    formState,
                    onFormChange = { formState = it },
                    onBack = { currentStep-- }
                )
            }
        }
    }
}

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

    val isValid = formState.fullName.isNotBlank()
            && formState.education.isNotBlank()
            && formState.fieldOfWork.isNotBlank()

    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxSize()
    ) {

        Column {

            Text("Basic Information", color = WeavyrTextPrimary)

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = formState.fullName,
                onValueChange = { onFormChange(formState.copy(fullName = it)) },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            ExposedDropdownMenuBox(
                expanded = eduExpanded,
                onExpandedChange = { eduExpanded = !eduExpanded }
            ) {
                OutlinedTextField(
                    readOnly = true,
                    value = formState.education,
                    onValueChange = {},
                    label = { Text("Education") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(eduExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
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
                OutlinedTextField(
                    readOnly = true,
                    value = formState.fieldOfWork,
                    onValueChange = {},
                    label = { Text("Field of Work") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(fieldExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
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

@Composable
fun StepProfessionalInfo(
    formState: ProfileFormState,
    onFormChange: (ProfileFormState) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {

    val isValid = formState.interests.isNotBlank()

    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxSize()
    ) {

        Column {

            Text("Professional Interests", color = WeavyrTextPrimary)
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = formState.interests,
                onValueChange = { onFormChange(formState.copy(interests = it)) },
                label = { Text("Research Interests (comma separated)") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {

            TextButton(onClick = onBack) { Text("Back") }

            Button(
                onClick = onNext,
                enabled = isValid
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

    val isValid = formState.papersPublished.isNotBlank()
            && formState.citations.isNotBlank()

    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxSize()
    ) {

        Column {

            Text("Expertise Level", color = WeavyrTextPrimary)
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = formState.papersPublished,
                onValueChange = { onFormChange(formState.copy(papersPublished = it)) },
                label = { Text("Papers Published") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = formState.citations,
                onValueChange = { onFormChange(formState.copy(citations = it)) },
                label = { Text("Citations Received") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            TextButton(onClick = onBack) { Text("Back") }
            Button(onClick = onNext, enabled = isValid) { Text("Continue") }
        }
    }
}

/* ---------------- STEP 4 ---------------- */

@Composable
fun StepVerification(
    formState: ProfileFormState,
    onFormChange: (ProfileFormState) -> Unit,
    onBack: () -> Unit
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
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Apply for Blue Tick Verification")
            }
        }

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            TextButton(onClick = onBack) { Text("Back") }
            Button(onClick = { /* Submit to backend later */ }) {
                Text("Finish")
            }
        }
    }
}