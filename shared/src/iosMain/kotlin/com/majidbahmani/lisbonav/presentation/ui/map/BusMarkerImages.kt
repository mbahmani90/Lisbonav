package com.majidbahmani.lisbonav.presentation.ui.map

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGAffineTransformMakeRotation
import platform.CoreGraphics.CGAffineTransformMakeTranslation
import platform.CoreGraphics.CGPointMake
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.UIKit.UIBezierPath
import platform.UIKit.UIColor
import platform.UIKit.UIGraphicsImageRenderer
import platform.UIKit.UIImage
import platform.UIKit.UIImageRenderingMode
import kotlin.math.PI
import kotlin.math.min
import com.majidbahmani.lisbonav.presentation.ui.map.BusMarkerStyle as Style

/** Draws the bus marker ([BusMarkerStyle]) as a UIImage, once per heading bucket. */
@OptIn(ExperimentalForeignApi::class)
internal object BusMarkerImages {

    private val cache = mutableMapOf<Int?, UIImage>()

    fun image(bearingBucket: Int?): UIImage = cache.getOrPut(bearingBucket) { draw(bearingBucket) }

    private fun draw(bearingBucket: Int?): UIImage {
        val size = Style.SIZE.toDouble()
        val center = size / 2
        val ink = Style.INK_COLOR.toUIColor()

        // The renderer uses the screen scale, so the image is sharp on Retina displays.
        return UIGraphicsImageRenderer(size = CGSizeMake(size, size)).imageWithActions { _ ->
            // Pointer first: the circle drawn next hides its inner part.
            if (bearingBucket != null) {
                val pointer = UIBezierPath().apply {
                    // Built around (0, 0) pointing up (north), then rotated and moved to the center.
                    moveToPoint(CGPointMake(0.0, -Style.POINTER_TIP.toDouble()))
                    addLineToPoint(CGPointMake(-Style.POINTER_HALF_WIDTH.toDouble(), -Style.POINTER_BASE.toDouble()))
                    addLineToPoint(CGPointMake(Style.POINTER_HALF_WIDTH.toDouble(), -Style.POINTER_BASE.toDouble()))
                    closePath()
                    // UIKit's y axis points down, so a positive angle turns clockwise, like a bearing.
                    applyTransform(CGAffineTransformMakeRotation(bearingBucket * PI / 180))
                    applyTransform(CGAffineTransformMakeTranslation(center, center))
                }
                ink.setFill()
                pointer.fill()
            }

            val radius = Style.CIRCLE_RADIUS.toDouble()
            val circle = UIBezierPath.bezierPathWithOvalInRect(
                CGRectMake(center - radius, center - radius, radius * 2, radius * 2),
            )
            Style.FILL_COLOR.toUIColor().setFill()
            circle.fill()
            Style.OUTLINE_COLOR.toUIColor().setStroke()
            circle.setLineWidth(Style.OUTLINE_WIDTH.toDouble())
            circle.stroke()

            // SF Symbol, kept upright and fitted into the glyph box without stretching.
            UIImage.systemImageNamed("bus.fill")
                ?.imageWithTintColor(ink, UIImageRenderingMode.UIImageRenderingModeAlwaysOriginal)
                ?.let { glyph ->
                    val (width, height) = glyph.size.useContents { width to height }
                    val scale = min(Style.GLYPH_SIZE / width, Style.GLYPH_SIZE / height)
                    val w = width * scale
                    val h = height * scale
                    glyph.drawInRect(CGRectMake(center - w / 2, center - h / 2, w, h))
                }
        }
    }

    private fun Long.toUIColor(): UIColor = UIColor.colorWithRed(
        red = ((this shr 16) and 0xFF) / 255.0,
        green = ((this shr 8) and 0xFF) / 255.0,
        blue = (this and 0xFF) / 255.0,
        alpha = ((this shr 24) and 0xFF) / 255.0,
    )
}
