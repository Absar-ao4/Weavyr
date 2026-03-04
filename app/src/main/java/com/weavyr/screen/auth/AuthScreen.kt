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
import com.weavyr.ui.theme.*

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit
) {

    var isSignUp by remember { mutableStateOf(true) }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }

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
                        onValueChange = { username = it },
                        placeholder = {
                            Text("Username", color = WeavyrTextSecondary)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = WeavyrTextPrimary,
                            unfocusedTextColor = WeavyrTextPrimary,
                            focusedBorderColor = WeavyrPrimary,
                            unfocusedBorderColor = WeavyrTextSecondary,
                            cursorColor = WeavyrPrimary
                        )
                    )
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = {
                        Text("Email", color = WeavyrTextSecondary)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = WeavyrTextPrimary,
                        unfocusedTextColor = WeavyrTextPrimary,
                        focusedBorderColor = WeavyrPrimary,
                        unfocusedBorderColor = WeavyrTextSecondary,
                        cursorColor = WeavyrPrimary
                    )
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = {
                        Text("Password", color = WeavyrTextSecondary)
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = WeavyrTextPrimary,
                        unfocusedTextColor = WeavyrTextPrimary,
                        focusedBorderColor = WeavyrPrimary,
                        unfocusedBorderColor = WeavyrTextSecondary,
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

                Button(
                    onClick = {

                        scope.launch {

                            try {

                                val response = if (isSignUp) {
                                    authRepository.signup(username, email, password)
                                } else {
                                    authRepository.login(email, password)
                                }

                                if (response.isSuccessful) {

                                    val token = response.body()?.jwt
                                    println("TOKEN RECEIVED: $token")

                                    if (token != null) {

                                        val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)

                                        val editor = prefs.edit()
                                        editor.putString("token", token)
                                        editor.commit()   // commit ensures it saves immediately
                                    println("TOKEN SAVED: $token")

                                    }


                                    onAuthSuccess()

                                } else {

                                    println("Auth failed: ${response.errorBody()?.string()}")

                                }

                            } catch (e: Exception) {

                                println("Network error: ${e.message}")

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
                                onAuthSuccess()
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