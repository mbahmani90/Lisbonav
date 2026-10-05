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
