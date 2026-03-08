package com.weavyr.screen.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CoolTutorialOverlay(onDismiss: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "tutorial_anim")

    // Pulsing animation for the icons
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f, targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Gentle side-to-side floating for the central hand icon
    val handOffset by infiniteTransition.animateFloat(
        initialValue = -25f, targetValue = 25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "swipe"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
            .padding(24.dp)
    ) {
        // --- Center "Ghost" Card Outline ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(450.dp)
                .align(Alignment.Center)
                .border(
                    BorderStroke(2.dp, Color.White.copy(alpha = 0.3f)),
                    RoundedCornerShape(32.dp)
                )
                .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(32.dp))
        ) {
            // Hand Swipe Icon in the middle
            Icon(
                imageVector = Icons.Default.Swipe,
                contentDescription = "Swipe",
                tint = Color.White,
                modifier = Modifier
                    .size(64.dp)
                    .align(Alignment.Center)
                    .offset(x = handOffset.dp)
            )
        }

        // --- Top Left: Swipe Left (Reject) ---
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = null,
                tint = Color(0xFFFF4B4B),
                modifier = Modifier
                    .size(48.dp)
                    .scale(pulseScale)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("NOPE", color = Color(0xFFFF4B4B), fontWeight = FontWeight.Black, fontSize = 20.sp)
            Text("Swipe Left", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
        }

        // --- Top Right: Swipe Right (Connect) ---
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                tint = Color(0xFF4CAF50),
                modifier = Modifier
                    .size(48.dp)
                    .scale(pulseScale)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("CONNECT", color = Color(0xFF4CAF50), fontWeight = FontWeight.Black, fontSize = 20.sp)
            Text("Swipe Right", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
        }

        // --- Top Center: Bookmark ---
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 100.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Bookmark,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Tap to Save Profile\nfor later!",
                color = Color.White,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
        }

        // --- Bottom: Get Started Button ---
        Button(
            onClick = onDismiss,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .fillMaxWidth()
                .height(60.dp),
            shape = RoundedCornerShape(30.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Got it! Let's go \uD83D\uDE80", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color.White)
        }
    }
}