package com.majidbahmani.lisbonav.systemdesign.theme

import androidx.compose.ui.graphics.Color

/** Brand colours, the same as the bus marker and the app icon. */
internal object LisbonavColors {
    /** Carris Metropolitana yellow: accents, not text (too light for contrast on white). */
    val Yellow = Color(0xFFFFDD00)

    /** Near-black ink: primary colour, text and icons on yellow. */
    val Ink = Color(0xFF1C1C1C)

    /** Neutral greys, so surfaces don't get Material's default lavender tint. */
    val SurfaceContainer = Color(0xFFF2F2F2)
    val SurfaceVariant = Color(0xFFE6E6E6)
    val OnSurfaceVariant = Color(0xFF4A4A4A)
}
