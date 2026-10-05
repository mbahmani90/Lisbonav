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

    /** Mid grey behind controls floating on the map (search bar). */
    val SurfaceDim = Color(0xFFAAAAAA)
}

/** The same neutral palette for dark mode: off-black surfaces (not pure black) and off-white text. */
internal object LisbonavDarkColors {
    val Surface = Color(0xFF121212)
    val SurfaceContainerLow = Color(0xFF1A1A1A)
    val SurfaceContainer = Color(0xFF1F1F1F)
    val SurfaceContainerHigh = Color(0xFF262626)
    val SurfaceVariant = Color(0xFF2E2E2E)
    val OnSurface = Color(0xFFECECEC)
    val OnSurfaceVariant = Color(0xFFB8B8B8)
    val SurfaceDim = Color(0xFF3A3A3A)
}
