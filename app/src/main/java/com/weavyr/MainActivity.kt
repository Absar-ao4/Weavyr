package com.weavyr

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.weavyr.screen.auth.AuthScreen
import com.weavyr.screen.main.MainAppScreen
import com.weavyr.screen.main.SplashScreen
import com.weavyr.screen.onboarding.ProfileCreationScreen
import com.weavyr.ui.theme.WeavyrTheme


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {

            WeavyrTheme {

                var showSplash by remember { mutableStateOf(true) }

                if (showSplash) {

                    SplashScreen(
                        onAnimationEnd = {
                            showSplash = false
                        }
                    )

                } else {

                    WeavyrApp()

                }

            }
        }
    }
}

@Composable
fun WeavyrApp() {

    val navController = rememberNavController()
    val context = LocalContext.current

    val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    val token = prefs.getString("token", null)
    println("TOKEN FROM STORAGE: $token")

    val startDestination = if (token != null) "main" else "auth"

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        composable("auth") {

            AuthScreen(
                onAuthSuccess = {

                    navController.navigate("main") {
                        popUpTo("auth") { inclusive = true }
                    }

                }
            )
        }



        composable("onboarding") {

            ProfileCreationScreen(
                onFinished = {

                    navController.navigate("main") {
                        popUpTo("onboarding") { inclusive = true }
                    }

                }
            )
        }

        composable("main") {
            MainAppScreen(
                onLogout = {
                    navController.navigate("auth") {
                        popUpTo("main") { inclusive = true }
                    }
                }
            )
        }
    }
}