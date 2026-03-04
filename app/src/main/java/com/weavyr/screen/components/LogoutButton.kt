package com.weavyr.screen.components

import android.content.Context
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController

@Composable
fun LogoutButton(onLogout: () -> Unit) {

    val context = LocalContext.current

    Button(
        onClick = {

            val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
            prefs.edit().remove("token").apply()

            onLogout()

        }
    ) {
        Text("Logout")
    }
}