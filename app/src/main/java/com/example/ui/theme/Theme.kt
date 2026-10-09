package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val KheloBDDarkColorScheme = darkColorScheme(
  primary = CyberOrange,
  onPrimary = Color.Black,
  primaryContainer = Color(0xFF3D1E00),
  onPrimaryContainer = CyberOrangeGlow,
  secondary = CyberCyan,
  onSecondary = Color.Black,
  secondaryContainer = Color(0xFF003844),
  onSecondaryContainer = Color(0xFF80F3FF),
  tertiary = CyberGreen,
  onTertiary = Color.Black,
  tertiaryContainer = Color(0xFF003D1B),
  onTertiaryContainer = CyberGreenGlow,
  background = GamingDarkBackground,
  onBackground = TextPrimary,
  surface = GamingDarkSurface,
  onSurface = TextPrimary,
  surfaceVariant = GamingDarkSurfaceVariant,
  onSurfaceVariant = TextSecondary,
  outline = GamingCardBorder,
  error = CyberRed,
  onError = Color.White
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = KheloBDDarkColorScheme,
    typography = Typography,
    content = content
  )
}

