package com.weavyr.screen.auth

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.weavyr.R
import com.weavyr.repository.AuthRepository
import kotlinx.coroutines.launch
import org.json.JSONObject
import com.weavyr.ui.theme.*

@Composable
fun AuthScreen(
    onAuthSuccess: (String) -> Unit
) {

    var isSignUp by remember { mutableStateOf(true) }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    var usernameError by remember { mutableStateOf(false) }

    // --- NEW: Loading State ---
    var isLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val authRepository = AuthRepository()
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WeavyrBackground),
        contentAlignment = Alignment.Center
    ) {

        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = WeavyrSurface
            ),
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {

            Column(
                modifier = Modifier.padding(28.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {

                Text(
                    text = if (isSignUp) "Create Account" else "Welcome Back",
                    style = MaterialTheme.typography.headlineSmall,
                    color = WeavyrTextPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (isSignUp) {
                    OutlinedTextField(
                        value = username,
                        onValueChange = {
                            username = it
                            usernameError = false
                            errorMessage = null
                        },
                        isError = usernameError,
                        placeholder = {
                            Text("Username", color = WeavyrTextSecondary)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = WeavyrTextPrimary,
                            unfocusedTextColor = WeavyrTextPrimary,
                            focusedBorderColor = if (usernameError) Color(0xFFFF5A5F) else WeavyrPrimary,
                            unfocusedBorderColor = if (usernameError) Color(0xFFFF5A5F) else WeavyrTextSecondary,
                            cursorColor = WeavyrPrimary
                        ),
                        enabled = !isLoading // Disable input while loading
                    )
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        emailError = false
                        errorMessage = null
                    },
                    isError = emailError,
                    placeholder = {
                        Text("Email", color = WeavyrTextSecondary)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = WeavyrTextPrimary,
                        unfocusedTextColor = WeavyrTextPrimary,
                        focusedBorderColor = if (emailError) Color(0xFFFF5A5F) else WeavyrPrimary,
                        unfocusedBorderColor = if (emailError) Color(0xFFFF5A5F) else WeavyrTextSecondary,
                        cursorColor = WeavyrPrimary
                    ),
                    enabled = !isLoading // Disable input while loading
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        passwordError = false
                        errorMessage = null
                    },
                    isError = passwordError,
                    placeholder = {
                        Text("Password", color = WeavyrTextSecondary)
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = WeavyrTextPrimary,
                        unfocusedTextColor = WeavyrTextPrimary,
                        focusedBorderColor = if (passwordError) Color(0xFFFF5A5F) else WeavyrPrimary,
                        unfocusedBorderColor = if (passwordError) Color(0xFFFF5A5F) else WeavyrTextSecondary,
                        cursorColor = WeavyrPrimary
                    ),
                    enabled = !isLoading // Disable input while loading
                )

                if (!isSignUp) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = rememberMe,
                            onCheckedChange = { rememberMe = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = WeavyrPrimary
                            ),
                            enabled = !isLoading // Disable input while loading
                        )

                        Text(
                            text = "Remember Me",
                            color = WeavyrTextPrimary
                        )
                    }
                }

                errorMessage?.let {
                    Text(
                        text = it,
                        color = Color(0xFFFF5A5F),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Button(
                    onClick = {
                        errorMessage = null
                        emailError = false
                        passwordError = false
                        usernameError = false

                        // EMAIL CHECKS
                        if (email.isBlank()) {
                            emailError = true
                            errorMessage = "Email cannot be empty"
                            return@Button
                        }
                        if (email != email.lowercase()) {
                            emailError = true
                            errorMessage = "Email must be lowercase"
                            return@Button
                        }
                        if (!email.endsWith("@gmail.com")) {
                            emailError = true
                            errorMessage = "Only Gmail accounts allowed"
                            return@Button
                        }

                        // PASSWORD VALIDATION LOGIC
                        if (isSignUp) {
                            if (!isValidPassword(password)) {
                                passwordError = true
                                errorMessage = "Password must be 8+ chars & include letters and numbers"
                                return@Button
                            }
                        } else {
                            if (password.isBlank()) {
                                passwordError = true
                                errorMessage = "Password cannot be empty"
                                return@Button
                            }
                        }

                        // USERNAME CHECK
                        if (isSignUp && username.isBlank()) {
                            usernameError = true
                            errorMessage = "Username required"
                            return@Button
                        }

                        // --- NEW: Start Loading ---
                        isLoading = true

                        scope.launch {
                            try {
                                if (isSignUp) {
                                    val signupResponse = authRepository.signup(username, email, password)

                                    if (signupResponse.isSuccessful) {
                                        val loginResponse = authRepository.login(email, password, false)

                                        if (loginResponse.isSuccessful) {
                                            val token = loginResponse.body()?.token

                                            if (token != null) {
                                                val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
                                                prefs.edit().putString("token", token).apply()
                                                onAuthSuccess("onboarding")
                                            } else {
                                                errorMessage = "Signup successful, but failed to retrieve token."
                                            }
                                        } else {
                                            errorMessage = "Signup successful, but auto-login failed."
                                        }
                                    } else {
                                        val errorBody = signupResponse.errorBody()?.string()
                                        errorMessage = try {
                                            JSONObject(errorBody ?: "").optString("error").replaceFirstChar { it.uppercase() }
                                        } catch (e: Exception) {
                                            "Sign up failed"
                                        }
                                    }

                                } else {
                                    val loginResponse = authRepository.login(email, password, rememberMe)

                                    if (loginResponse.isSuccessful) {
                                        val token = loginResponse.body()?.token

                                        if (token != null) {
                                            val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
                                            prefs.edit().putString("token", token).apply()
                                            onAuthSuccess("main")
                                        } else {
                                            errorMessage = "Login failed: No token received."
                                        }
                                    } else {
                                        val errorBody = loginResponse.errorBody()?.string()
                                        errorMessage = try {
                                            JSONObject(errorBody ?: "").optString("error").replaceFirstChar { it.uppercase() }
                                        } catch (e: Exception) {
                                            "Login failed"
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                errorMessage = "Network error. Please try again."
                            } finally {
                                // --- NEW: Stop Loading when done (success or error) ---
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp), // Fixed height so it doesn't jump when loading icon appears
                    shape = RoundedCornerShape(18.dp),
                    enabled = !isLoading // --- NEW: Disable button to prevent double-clicks ---
                ) {
                    // --- NEW: Show Spinner if loading, otherwise show Text ---
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = WeavyrBackground,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(if (isSignUp) "Sign Up" else "Sign In")
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Divider(
                        modifier = Modifier.weight(1f),
                        color = WeavyrTextSecondary.copy(alpha = 0.3f)
                    )

                    Text(
                        text = "  or  ",
                        color = WeavyrTextSecondary
                    )

                    Divider(
                        modifier = Modifier.weight(1f),
                        color = WeavyrTextSecondary.copy(alpha = 0.3f)
                    )
                }

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = CircleShape,
                        color = WeavyrSurface,
                        tonalElevation = 4.dp,
                        modifier = Modifier
                            .size(56.dp)
                            .clickable(enabled = !isLoading) { // Don't let them click Google while loading
                                onAuthSuccess("main")
                            }
                    ) {
                        Box(
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_google),
                                contentDescription = "Google Sign In",
                                tint = Color.Unspecified,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isSignUp)
                            "Already have an account? "
                        else
                            "Don't have an account? ",
                        color = WeavyrTextSecondary
                    )

                    Text(
                        text = if (isSignUp) "Sign In" else "Sign Up",
                        color = if (isLoading) WeavyrTextSecondary else WeavyrPrimary, // Dim when loading
                        modifier = Modifier.clickable(enabled = !isLoading) {
                            isSignUp = !isSignUp
                            errorMessage = null // Clear errors on switch
                        }
                    )
                }
            }
        }
    }
}

// Helper Function
private fun isValidPassword(password: String): Boolean {
    if (password.length < 8) return false
    val hasLetter = password.any { it.isLetter() }
    val hasNumber = password.any { it.isDigit() }
    return hasLetter && hasNumber
}