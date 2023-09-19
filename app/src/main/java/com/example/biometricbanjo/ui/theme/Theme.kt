package com.example.biometricbanjo.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat


val Primary10 = Color(0xFF2B3648)
val Primary20 = Color(0xFF195E77)
val Primary30 = Color(0xFF008A93)
val Primary90 = Color(0xFFD5DBFA)
val Primary95 = Color(0xFFE6E9FC)

val Grey10 = Color(0xFF72767E)
val Grey20 = Color(0xFFDBDDE1)
val Grey60 = Color(0xFFE9EAED)
val Grey90 = Color(0xFF272A30)

val Orange80 = Color(0xFFFBF4DD)

val Green = Color(0xFF078362)

val Red10 = Color(0xFFFF3742)
val Red90 = Color(0xFFF5D0D9)

val Neutral10 = Color(0xFF14161F)
val Neutral90 = Color(0xFFD1D2D4)

val Black = Color(0xFF000000)

val BVlack = Color(0xFF191D24)

val White = Color(0xFFFFFFFF)

val Disabled1 = Color(0xFF3A3C3F)
val Disabled2 = Color(0xFFDFE2E6)

private val LightColorScheme = lightColorScheme(
    primary = Primary10,
    onPrimary = White,
    primaryContainer = White,
    onPrimaryContainer = Primary10,
    secondary = Primary10,
    onSecondary = White,
    tertiary = Pink40,
    surface = White,
    onSurface = Neutral10

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)


private val DarkColorScheme = darkColorScheme(
    primary = Primary90,
    onPrimary = Primary10,
    primaryContainer = Primary10,
    onPrimaryContainer = Primary95,
    secondary = Primary10,
    onSecondary = White,
    tertiary = Pink80,
    surface = Neutral10,
    onSurface = Neutral90
)

@Composable
fun BiometricBanjoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = shapes,
        content = content
    )
}