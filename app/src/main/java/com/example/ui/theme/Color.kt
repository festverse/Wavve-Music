package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val StaticBackgroundWavve = Color(0xFFFAFAF7)
val StaticSurfaceWavve = Color(0xFFFFFFFF)
val StaticPrimaryText = Color(0xFF0A0A0A)
val StaticSecondaryText = Color(0xFF6B6B6B)
val StaticAccentColor = Color(0xFFFF5B2E)
val StaticDividerColor = Color(0x0F0A0A0A)

val DarkBackgroundWavve = Color(0xFF121212)
val DarkSurfaceWavve = Color(0xFF1E1E1E)
val DarkPrimaryText = Color(0xFFFFFFFF)
val DarkSecondaryText = Color(0xFFAAAAAA)
val DarkDividerColor = Color(0xFF333333)

val BackgroundWavve: Color @Composable get() = MaterialTheme.colorScheme.background
val SurfaceWavve: Color @Composable get() = MaterialTheme.colorScheme.surface
val PrimaryText: Color @Composable get() = MaterialTheme.colorScheme.onBackground
val SecondaryText: Color @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
val AccentColor: Color @Composable get() = MaterialTheme.colorScheme.primary
val DividerColor: Color @Composable get() = MaterialTheme.colorScheme.outlineVariant

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)
