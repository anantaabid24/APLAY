package com.example.iptvapp.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

private val DarkPrimary = Color(0xFFBB86FC)
private val DarkSecondary = Color(0xFF03DAC6)
private val DarkBackground = Color(0xFF121212)
private val DarkSurface = Color(0xFF1E1E1E)
private val DarkError = Color(0xFFCF6679)

private val LightPrimary = Color(0xFF6200EE)
private val LightSecondary = Color(0xFF03DAC6)
private val LightBackground = Color(0xFFF5F5F5)
private val LightSurface = Color(0xFFFFFFFF)
private val LightError = Color(0xFFB00020)

val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    secondary = DarkSecondary,
    background = DarkBackground,
    surface = DarkSurface,
    error = DarkError,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
    onError = Color.Black,
    primaryContainer = Color(0xFF3700B3),
    onPrimaryContainer = Color.White,
    secondaryContainer = Color(0xFF006D67),
    onSecondaryContainer = Color.White,
)

val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    secondary = LightSecondary,
    background = LightBackground,
    surface = LightSurface,
    error = LightError,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
    onError = Color.White,
    primaryContainer = Color(0xFFEADDFF),
    onPrimaryContainer = Color.Black,
    secondaryContainer = Color(0xFF006D67),
    onSecondaryContainer = Color.White,
)