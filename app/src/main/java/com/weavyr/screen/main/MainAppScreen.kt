package com.weavyr.screen.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.*
import com.weavyr.screen.components.FloatingBottomNavBar
import com.weavyr.ui.theme.WeavyrBackground
import com.weavyr.screen.main.ArticlesScreen
import com.weavyr.screen.main.HomeScreen
import com.weavyr.screen.main.UserProfileScreen
@Composable
fun MainAppScreen() {

    val navController = rememberNavController()

    Scaffold(
        containerColor = WeavyrBackground,
        bottomBar = {
            FloatingBottomNavBar(navController)
        }
    ) { padding ->

        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(padding)
        ) {

            composable("articles") { ArticlesScreen() }
            composable("home") { HomeScreen() }
            composable("profile") { UserProfileScreen() }
        }
    }
}