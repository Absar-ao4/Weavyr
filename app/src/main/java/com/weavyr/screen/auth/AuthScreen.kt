package com.weavyr.screen.auth

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.weavyr.repository.AuthRepository
import kotlinx.coroutines.launch
import org.json.JSONObject

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

    var isLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val authRepository = AuthRepository()
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {

        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
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
                    color = MaterialTheme.colorScheme.onSurface
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
                            Text("Username", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedBorderColor = if (usernameError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = if (usernameError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                            cursorColor = MaterialTheme.colorScheme.primary
                        ),
                        enabled = !isLoading
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
                        Text("Email", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = if (emailError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = if (emailError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                        cursorColor = MaterialTheme.colorScheme.primary
                    ),
                    enabled = !isLoading
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
                        Text("Password", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = if (passwordError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = if (passwordError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                        cursorColor = MaterialTheme.colorScheme.primary
                    ),
                    enabled = !isLoading
                )

                // ⭐ NEW: Adjusted Remember Me & Added Forgot Password side-by-side
                if (!isSignUp) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween // Pushes items to opposite sides
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = rememberMe,
                                onCheckedChange = { rememberMe = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = MaterialTheme.colorScheme.primary
                                ),
                                enabled = !isLoading
                            )
                            Text(
                                text = "Remember Me",
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        // Forgot Password Text
                        Text(
                            text = "Forgot Password?",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable(enabled = !isLoading) {
                                // TODO: Handle Forgot Password click here
                            }
                        )
                    }
                }

                errorMessage?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Button(
                    onClick = {
                        errorMessage = null
                        emailError = false
                        passwordError = false
                        usernameError = false

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

                        if (isSignUp && username.isBlank()) {
                            usernameError = true
                            errorMessage = "Username required"
                            return@Button
                        }

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
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(18.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(if (isSignUp) "Sign Up" else "Sign In")
                    }
                }

                // Switch between Sign Up / Sign In
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isSignUp) "Already have an account? " else "Don't have an account? ",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = if (isSignUp) "Sign In" else "Sign Up",
                        color = if (isLoading) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold, // Added bold to make it pop slightly
                        modifier = Modifier.clickable(enabled = !isLoading) {
                            isSignUp = !isSignUp
                            errorMessage = null
                        }
                    )
                }
            }
        }
    }
}

private fun isValidPassword(password: String): Boolean {
    if (password.length < 8) return false
    val hasLetter = password.any { it.isLetter() }
    val hasNumber = password.any { it.isDigit() }
    return hasLetter && hasNumber
}