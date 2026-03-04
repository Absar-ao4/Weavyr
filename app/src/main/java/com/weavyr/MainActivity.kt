package com.weavyr

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import com.weavyr.screen.onboarding.ProfileCreationScreen
import com.weavyr.ui.theme.WeavyrTheme
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.weavyr.screen.auth.AuthScreen
import com.weavyr.screen.main.MainAppScreen
import androidx.compose.runtime.*
import com.weavyr.screen.main.SplashScreen

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

    NavHost(
        navController = navController,
        startDestination = "auth"
    ) {
        composable("auth") {
            AuthScreen(
                onAuthSuccess = {
                    navController.navigate("onboarding") {
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
            MainAppScreen()
        }
    }
}