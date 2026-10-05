package com.majidbahmani.lisbonav.systemdesign.theme

import androidx.compose.material3.MaterialTheme
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
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = LisbonavColors.SurfaceContainer,
    surfaceContainer = LisbonavColors.SurfaceContainer,
    surfaceContainerHigh = LisbonavColors.SurfaceContainer,
    surfaceContainerHighest = LisbonavColors.SurfaceVariant,
)

/**
 * The app theme: Material 3 with the Lisbonav brand colours. Wraps the whole UI in `app`.
 *
 * Light only for now: the map's search bar uses a fixed light-grey background, so a dark scheme
 * (light text) needs that component adapted first.
 */
@Composable
fun LisbonavTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        content = content,
    )
}
