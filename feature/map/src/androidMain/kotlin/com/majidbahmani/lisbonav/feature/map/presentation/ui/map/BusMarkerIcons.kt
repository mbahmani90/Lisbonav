package com.majidbahmani.lisbonav.feature.map.presentation.ui.map

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import androidx.core.graphics.PathParser
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.majidbahmani.lisbonav.feature.map.presentation.ui.map.BusMarkerStyle as Style

/**
 * Draws the bus marker ([BusMarkerStyle]) as a bitmap, once per heading bucket and screen density.
 * Call only while a map is shown: BitmapDescriptorFactory needs the Maps SDK to be initialized.
 */
internal object BusMarkerIcons {

    private val cache = HashMap<Pair<Int?, Float>, BitmapDescriptor>()

    fun icon(bearingBucket: Int?, density: Float): BitmapDescriptor =
        cache.getOrPut(bearingBucket to density) {
            BitmapDescriptorFactory.fromBitmap(draw(bearingBucket, density))
        }

    private fun draw(bearingBucket: Int?, density: Float): Bitmap {
        val sizePx = (Style.SIZE * density).toInt()
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.scale(density, density) // draw in dp from here on
        val center = Style.SIZE / 2

        val ink = paint(Style.INK_COLOR)

        // Pointer first: the circle drawn next hides its inner part.
        if (bearingBucket != null) {
            canvas.save()
            canvas.rotate(bearingBucket.toFloat(), center, center) // clockwise from north, like a bearing
            val pointer = Path().apply {
                moveTo(center, center - Style.POINTER_TIP)
                lineTo(center - Style.POINTER_HALF_WIDTH, center - Style.POINTER_BASE)
                lineTo(center + Style.POINTER_HALF_WIDTH, center - Style.POINTER_BASE)
                close()
            }
            canvas.drawPath(pointer, ink)
            canvas.restore()
        }

        canvas.drawCircle(center, center, Style.CIRCLE_RADIUS, paint(Style.FILL_COLOR))
        canvas.drawCircle(
            center,
            center,
            Style.CIRCLE_RADIUS,
            paint(Style.OUTLINE_COLOR).apply {
                style = Paint.Style.STROKE
                strokeWidth = Style.OUTLINE_WIDTH
            },
        )

        // The glyph stays upright; only the pointer turns.
        val glyph = PathParser.createPathFromPathData(BUS_GLYPH_PATH)
        val scale = Style.GLYPH_SIZE / GLYPH_VIEWPORT
        glyph.transform(
            Matrix().apply {
                setScale(scale, scale)
                postTranslate(center - Style.GLYPH_SIZE / 2, center - Style.GLYPH_SIZE / 2)
            },
        )
        canvas.drawPath(glyph, ink)

        return bitmap
    }

    private fun paint(argb: Long) = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = argb.toInt() }

    private const val GLYPH_VIEWPORT = 24f

    /** Material Icons "directions_bus" (Apache License 2.0), 24×24 viewport. */
    private const val BUS_GLYPH_PATH =
        "M4,16c0,0.88 0.39,1.67 1,2.22V20c0,0.55 0.45,1 1,1h1c0.55,0 1,-0.45 1,-1v-1h8v1" +
            "c0,0.55 0.45,1 1,1h1c0.55,0 1,-0.45 1,-1v-1.78c0.61,-0.55 1,-1.34 1,-2.22V6" +
            "c0,-3.5 -3.58,-4 -8,-4s-8,0.5 -8,4v10zM7.5,17c-0.83,0 -1.5,-0.67 -1.5,-1.5" +
            "S6.67,14 7.5,14s1.5,0.67 1.5,1.5S8.33,17 7.5,17zM16.5,17c-0.83,0 -1.5,-0.67 -1.5,-1.5" +
            "s0.67,-1.5 1.5,-1.5 1.5,0.67 1.5,1.5 -0.67,1.5 -1.5,1.5zM18,11H6V6h12v5z"
}
