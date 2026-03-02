package com.weavyr

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.weavyr.screen.ProfileCreationScreen
import com.weavyr.ui.theme.WeavyrTheme
import com.weavyr.ui.theme.WeavyrBackground

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            WeavyrTheme {
                WeavyrApp()
            }
        }
    }
}

@Composable
fun WeavyrApp() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WeavyrBackground)
    ) {
        ProfileCreationScreen()
    }
}