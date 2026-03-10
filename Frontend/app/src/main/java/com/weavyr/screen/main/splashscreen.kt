package com.weavyr.screen.main

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.Text
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.graphicsLayer
import com.airbnb.lottie.compose.*
import com.weavyr.R
import androidx.compose.foundation.layout.Row
import kotlinx.coroutines.delay

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SplashScreen(onAnimationEnd: () -> Unit) {

    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.weavyr_intro1)
    )

    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = 1
    )

    val word = when {
        progress < 0.25f -> "Connect"
        progress < 0.50f -> "Collaborate"
        progress < 0.75f -> "Explore"
        else -> "Elevate"
    }

    if (progress >= 0.99f) {
        LaunchedEffect(Unit) {
            onAnimationEnd()
        }
    }

    // subtle floating animation
    val infiniteTransition = rememberInfiniteTransition(label = "")

    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = ""
    )

    val breathingScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = ""
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {

        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.fillMaxSize(0.6f)
        )

        AnimatedContent(
            targetState = word,
            transitionSpec = {

                val enter = fadeIn(
                    animationSpec = tween(300)
                ) +
                        slideInVertically(
                            initialOffsetY = { it / 2 },
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        ) +
                        scaleIn(
                            initialScale = 0.8f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        )

                val exit = fadeOut(
                    animationSpec = tween(250)
                ) +
                        slideOutVertically(
                            targetOffsetY = { -it / 2 },
                            animationSpec = tween(250)
                        ) +
                        scaleOut(
                            targetScale = 0.9f,
                            animationSpec = tween(250)
                        )

                enter togetherWith exit
            }
        ) { text ->

            if (text == "Connect") {

                val letters = "Connect".toCharArray()
                var glowIndex by remember { mutableStateOf(0) }

                LaunchedEffect(Unit) {
                    while (true) {
                        delay(120)
                        glowIndex = (glowIndex + 1) % letters.size
                    }
                }

                Row {

                    letters.forEachIndexed { index, char ->

                        val scale by animateFloatAsState(
                            targetValue = if (index == glowIndex) 1.2f else 1f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy
                            )
                        )

                        Text(
                            text = char.toString(),
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (index == glowIndex)
                                Color(0xFF8A7BFF)  // Weavyr purple glow
                            else
                                Color.White,
                            modifier = Modifier.graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
                        )
                    }
                }

            } else {

                Text(
                    text = text,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}