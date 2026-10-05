package com.majidbahmani.lisbonav.presentation.ui.map

/**
 * The bus marker, shared by the Android and iOS maps so both look the same:
 * a yellow circle with an upright bus glyph, and a pointer on its edge showing the heading.
 *
 * Sizes are in dp (Android) / points (iOS), measured on a square canvas of [SIZE].
 */
internal object BusMarkerStyle {
    const val SIZE = 44f
    const val CIRCLE_RADIUS = 14f
    const val OUTLINE_WIDTH = 2f
    const val GLYPH_SIZE = 16f

    /** Pointer: a triangle from inside the circle (hidden under it) to [POINTER_TIP] from the center. */
    const val POINTER_TIP = 21f
    const val POINTER_BASE = 11f
    const val POINTER_HALF_WIDTH = 6f

    /** ARGB colors. Yellow as in Carris Metropolitana's branding; dark ink for contrast. */
    const val FILL_COLOR = 0xFFFFDD00
    const val INK_COLOR = 0xFF1C1C1C
    const val OUTLINE_COLOR = 0xFFFFFFFF

    /** Headings are rounded to this step, so only a few icons are ever drawn and cached. */
    const val BEARING_STEP = 15

    /** The heading rounded to [BEARING_STEP] (0..345), or null when the bus reports no heading. */
    fun bearingBucket(bearingDegrees: Int?): Int? =
        bearingDegrees?.let { ((it + BEARING_STEP / 2) / BEARING_STEP * BEARING_STEP) % 360 }
}
