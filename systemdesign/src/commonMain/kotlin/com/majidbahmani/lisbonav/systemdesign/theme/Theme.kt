package com.majidbahmani.lisbonav.systemdesign.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = LisbonavColors.Ink,
    onPrimary = Color.White,
    secondary = LisbonavColors.Yellow,
    onSecondary = LisbonavColors.Ink,
    // Selected item indicator (e.g. the bottom bar pill): yellow with an ink icon.
    secondaryContainer = LisbonavColors.Yellow,
    onSecondaryContainer = LisbonavColors.Ink,
    // Neutral surfaces instead of the default lavender-tinted ones.
    background = Color.White,
    onBackground = LisbonavColors.Ink,
    surface = Color.White,
    onSurface = LisbonavColors.Ink,
    surfaceVariant = LisbonavColors.SurfaceVariant,
    onSurfaceVariant = LisbonavColors.OnSurfaceVariant,
    surfaceDim = LisbonavColors.SurfaceDim,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = LisbonavColors.SurfaceContainer,
    surfaceContainer = LisbonavColors.SurfaceContainer,
    surfaceContainerHigh = LisbonavColors.SurfaceContainer,
    surfaceContainerHighest = LisbonavColors.SurfaceVariant,
)

private val DarkColors = darkColorScheme(
    // Off-white instead of ink: buttons, focus borders and progress stay visible on dark surfaces.
    primary = LisbonavDarkColors.OnSurface,
    onPrimary = LisbonavColors.Ink,
    // The brand yellow works on dark too, still with ink on top.
    secondary = LisbonavColors.Yellow,
    onSecondary = LisbonavColors.Ink,
    secondaryContainer = LisbonavColors.Yellow,
    onSecondaryContainer = LisbonavColors.Ink,
    background = LisbonavDarkColors.Surface,
    onBackground = LisbonavDarkColors.OnSurface,
    surface = LisbonavDarkColors.Surface,
    onSurface = LisbonavDarkColors.OnSurface,
    surfaceVariant = LisbonavDarkColors.SurfaceVariant,
    onSurfaceVariant = LisbonavDarkColors.OnSurfaceVariant,
    surfaceDim = LisbonavDarkColors.SurfaceDim,
    surfaceContainerLowest = LisbonavDarkColors.Surface,
    surfaceContainerLow = LisbonavDarkColors.SurfaceContainerLow,
    surfaceContainer = LisbonavDarkColors.SurfaceContainer,
    surfaceContainerHigh = LisbonavDarkColors.SurfaceContainerHigh,
    surfaceContainerHighest = LisbonavDarkColors.SurfaceVariant,
)

/**
 * The app theme: Material 3 with the Lisbonav brand colours. Wraps the whole UI in `app`.
 * Follows the system's dark mode; screens use only theme colours, so they switch with it.
 */
@Composable
fun LisbonavTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
