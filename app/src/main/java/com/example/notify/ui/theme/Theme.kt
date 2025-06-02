package com.example.notify.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Define your app's black color scheme
private val AppCompletelyBlackColorScheme = darkColorScheme(
    primary = AccentBlue,                // Your chosen accent color
    onPrimary = AppBlack,                // Color for text/icons on primary elements
    primaryContainer = AccentBlue,       // (Could be same as primary or a variant)
    onPrimaryContainer = AppBlack,

    secondary = AccentBlue,              // Your chosen accent color for secondary elements
    onSecondary = AppBlack,
    secondaryContainer = AccentBlue,
    onSecondaryContainer = AppBlack,

    tertiary = AccentBlue,               // Your chosen accent color for tertiary elements
    onTertiary = AppBlack,
    tertiaryContainer = AccentBlue,
    onTertiaryContainer = AppBlack,

    error = Color(0xFFCF6679),           // Standard Material error color
    onError = AppBlack,
    errorContainer = Color(0xFFCF6679),
    onErrorContainer = AppBlack,

    background = AppBlack,               // <<< KEY: Main application background
    onBackground = TextOnAppBlack,       // <<< KEY: Text/icons on the main background

    surface = AppBlack,                  // <<< KEY: Surface color for Cards, Dialogs, Menus, etc.
    onSurface = TextOnAppBlack,          // <<< KEY: Text/icons on these surfaces

    surfaceVariant = Color(0xFF1A1A1A), // A very dark gray, almost black, for subtle variations if needed
    onSurfaceVariant = SubtleTextOnAppBlack,

    outline = SubtleTextOnAppBlack,      // Color for outlines

    inverseOnSurface = AppBlack,
    inverseSurface = TextOnAppBlack,     // For elements like Snackbars that might invert colors
    inversePrimary = AppBlack,
    surfaceTint = Color.Transparent,     // No tint needed for a pure black surface
    scrim = Color.Black.copy(alpha = 0.5f) // Scrim color for overlays
)

@Composable
fun DailyTheme( // e.g., NotifyTheme, MyProjectTheme
    // Force dark theme to always use your black scheme.
    // Set to false if you want to allow users to switch to a light theme (which you'd also need to define).
    useDarkTheme: Boolean = true, // <<< SET TO TRUE TO ALWAYS USE THE BLACK THEME
    // Disable dynamic color to ensure your black theme isn't overridden by system wallpaper colors.
    useDynamicColor: Boolean = false, // <<< SET TO FALSE
    content: @Composable () -> Unit
) {
    val colorScheme = if (useDarkTheme) {
        // If dynamic color is enabled AND on Android 12+, it would normally be used.
        // We are overriding this by setting useDynamicColor = false.
        AppCompletelyBlackColorScheme
    } else {
        // Define a lightColorScheme here if you want to support it.
        // For a completely black app, you might even point this to AppCompletelyBlackColorScheme.
        AppCompletelyBlackColorScheme // Fallback to black if not using dark theme explicitly (or define a light theme)
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            window?.let {
                // Set status bar and navigation bar to be black
                it.statusBarColor = AppBlack.toArgb()
                it.navigationBarColor = AppBlack.toArgb()

                // Ensure system icons on status/navigation bars are light (for contrast on black)
                val insetsController = WindowCompat.getInsetsController(it, view)
                insetsController.isAppearanceLightStatusBars = false
                insetsController.isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Make sure your Typography.kt is defined
        content = content
    )
}