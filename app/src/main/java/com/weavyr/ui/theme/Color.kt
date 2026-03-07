package com.weavyr.ui.theme

import androidx.compose.ui.graphics.Color

// Default Material colors (You can keep these if Material3 requires them elsewhere)
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)


// ==========================================
// ☀️ LIGHT THEME COLORS
// ==========================================

val LightBackground = Color(0xFFE4F4F6)
val LightSurface = Color(0xFFFFFFFF)
val LightCard = Color(0xFFFFFFFF)

val LightPrimary = Color(0xFF1E5A62)
val LightSecondary = Color(0xFF82D3DE)
val LightAccent = Color(0xFF2EB3C2)

val LightTextPrimary = Color(0xFF091516)
val LightTextSecondary = Color(0xFF5A787B)

val LightDivider = Color(0xFFC4E0E3)


// ==========================================
// 🌙 DARK THEME COLORS
// ==========================================

// Backgrounds: Using your light mode text color as the deep, rich background
val DarkBackground = Color(0xFF091516)
// Elevated slightly so cards don't melt into the background
val DarkSurface = Color(0xFF142729)
val DarkCard = Color(0xFF142729)

// Brand Colors: Flipped! The lighter cyan becomes primary so it pops and is readable
val DarkPrimary = Color(0xFF82D3DE)
val DarkSecondary = Color(0xFF1E5A62)
val DarkAccent = Color(0xFF2EB3C2) // Accent stays vibrant

// Typography: Using your light mode background color for crisp, light text
val DarkTextPrimary = Color(0xFFE4F4F6)
// Muted grayish-teal for secondary text to reduce eye strain
val DarkTextSecondary = Color(0xFF82A0A3)

// Dividers: Subtle lines just slightly brighter than the surface
val DarkDivider = Color(0xFF1F3B3E)