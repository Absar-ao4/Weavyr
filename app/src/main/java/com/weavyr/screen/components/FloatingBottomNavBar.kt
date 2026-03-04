package com.weavyr.screen.components


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.weavyr.ui.theme.*

@Composable
fun FloatingBottomNavBar(
    navController: NavController
) {

    val items = listOf("articles", "home", "myprofile")

    val currentRoute =
        navController.currentBackStackEntryAsState().value?.destination?.route

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
        contentAlignment = Alignment.BottomCenter
    ) {

        Surface(
            shape = RoundedCornerShape(50.dp),
            color = WeavyrSurface.copy(alpha = 0.85f),
            tonalElevation = 8.dp,
            shadowElevation = 18.dp,
            modifier = Modifier
                .clip(RoundedCornerShape(50.dp))
        ) {

            Row(
                modifier = Modifier
                    .padding(horizontal = 40.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(56.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                items.forEach { route ->

                    val selected = currentRoute == route

                    IconButton(
                        onClick = {
                            navController.navigate(route) {
                                popUpTo("home")
                                launchSingleTop = true
                            }
                        }
                    ) {

                        Icon(
                            imageVector = when (route) {
                                "articles" -> Icons.Default.MenuBook
                                "home" -> Icons.Default.Home
                                else -> Icons.Default.Person
                            },
                            contentDescription = null,
                            tint = if (selected)
                                WeavyrPrimary
                            else
                                WeavyrTextSecondary
                        )
                    }
                }
            }
        }
    }
}