package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val SomTrevelLightScheme = lightColorScheme(
    primary = DeepNavy,
    onPrimary = PureWhite,
    primaryContainer = RoyalBlue,
    onPrimaryContainer = PureWhite,
    secondary = LuxuryGold,
    onSecondary = DeepNavy,
    secondaryContainer = LuxuryGoldSoft,
    onSecondaryContainer = DeepNavy,
    tertiary = RoyalBlueLight,
    onTertiary = PureWhite,
    background = SoftGray,
    onBackground = DarkText,
    surface = PureWhite,
    onSurface = DarkText,
    surfaceVariant = ColorTokens.LightSurfaceVariant,
    onSurfaceVariant = MutedSlate,
    outline = SlateBorder,
    error = CrimsonCancelled,
    onError = PureWhite
)

private val SomTrevelDarkScheme = darkColorScheme(
    primary = LuxuryGold,
    onPrimary = DeepNavyDarker,
    primaryContainer = RoyalBlue,
    onPrimaryContainer = PureWhite,
    secondary = LuxuryGold,
    onSecondary = DeepNavyDarker,
    secondaryContainer = DeepNavyElevated,
    onSecondaryContainer = LuxuryGoldSoft,
    tertiary = RoyalBlueLight,
    onTertiary = PureWhite,
    background = DeepNavyDarker,
    onBackground = PureWhite,
    surface = DeepNavy,
    onSurface = PureWhite,
    surfaceVariant = DeepNavyElevated,
    onSurfaceVariant = ColorTokens.DarkMutedText,
    outline = ColorTokens.DarkOutline,
    error = ColorTokens.DarkError,
    onError = DeepNavyDarker
)

private object ColorTokens {
    val LightSurfaceVariant = androidx.compose.ui.graphics.Color(0xFFE9EEF5)
    val DarkMutedText = androidx.compose.ui.graphics.Color(0xFFB4C2D6)
    val DarkOutline = androidx.compose.ui.graphics.Color(0xFF234066)
    val DarkError = androidx.compose.ui.graphics.Color(0xFFFF897D)
}

val SomTrevelShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun SomTrevelTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) SomTrevelDarkScheme else SomTrevelLightScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = SomTrevelTypography,
        shapes = SomTrevelShapes,
        content = content
    )
}
