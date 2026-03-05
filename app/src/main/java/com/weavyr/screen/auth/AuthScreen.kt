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
                        )
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
                    )
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
                    )
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
                            )
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


                        // EMAIL EMPTY
                        if (email.isBlank()) {
                            emailError = true
                            errorMessage = "Email cannot be empty"
                            return@Button
                        }

                        // EMAIL MUST BE LOWERCASE
                        if (email != email.lowercase()) {
                            emailError = true
                            errorMessage = "Email must be lowercase"
                            return@Button
                        }

                        // EMAIL MUST BE GMAIL
                        if (!email.endsWith("@gmail.com")) {
                            emailError = true
                            errorMessage = "Only Gmail accounts allowed"
                            return@Button
                        }

                        // PASSWORD LENGTH
                        if (password.length < 6) {
                            passwordError = true
                            errorMessage = "Password must be at least 6 characters"
                            return@Button
                        }

                        // USERNAME CHECK
                        if (isSignUp && username.isBlank()) {
                            usernameError = true
                            errorMessage = "Username required"
                            return@Button
                        }

                        scope.launch {

                            try {
                                if (isSignUp) {
                                    // 1. Create the account
                                    val signupResponse = authRepository.signup(username, email, password)

                                    if (signupResponse.isSuccessful) {
                                        // 2. Account created! Now AUTO-LOGIN to get the token
                                        val loginResponse = authRepository.login(email, password)

                                        if (loginResponse.isSuccessful) {
                                            val token = loginResponse.body()?.token

                                            if (token != null) {
                                                val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
                                                prefs.edit().putString("token", token).apply()

                                                // 3. Move to onboarding with the token safely stored!
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
                                    // NORMAL LOGIN FLOW
                                    val loginResponse = authRepository.login(email, password)

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

                            }

                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text(if (isSignUp) "Sign Up" else "Sign In")
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
                            .clickable {
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
                        color = WeavyrPrimary,
                        modifier = Modifier.clickable {
                            isSignUp = !isSignUp
                        }
                    )
                }
            }
        }
    }
}