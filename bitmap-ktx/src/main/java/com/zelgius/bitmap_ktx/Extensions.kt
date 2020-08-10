package com.zelgius.bitmap_ktx

import android.graphics.*
import android.view.View
import java.lang.Math.max
import java.lang.Math.min

fun Bitmap.floydSteinbergDithering(
    colorPalette: IntArray? = null,
    area: Rect = Rect(0, 0, width, height)
): Bitmap {
    val palette = colorPalette?.map {
        C3(
            r = Color.red(it),
            g = Color.green(it),
            b = Color.blue(it)
        )
    }?.toTypedArray()
        ?: arrayOf(
            C3(0, 0, 0),  // black
            C3(0, 0, 255),  // green
            C3(0, 255, 0),  // blue
            C3(0, 255, 255),  // cyan
            C3(255, 0, 0),  // red
            C3(255, 0, 255),  // purple
            C3(255, 255, 0),  // yellow
            C3(255, 255, 255) // white
        )

    val w = area.width()
    val h = area.height()
    val d = Array(h) { arrayOfNulls<C3>(w) }
    for (y in area.top until area.top + h) {
        for (x in area.left until area.left + w) {
            with(getPixel(x, y)) {
                d[y - area.top][x - area.left] = C3(
                    r = Color.red(this),
                    g = Color.green(this),
                    b = Color.blue(this)
                )
            }
        }
    }
    for (i in area.top until area.top + h) {
        for (j in area.left until area.left + w) {
            val x = j - area.left
            val y = i - area.top

            val oldColor = d[y][x]
            val newColor = FloydSteinbergDithering.findClosestPaletteColor(oldColor, palette)
            setPixel(j, i, newColor.toColor())

            val err = oldColor!!.sub(newColor)
            if (x + 1 < w) {
                d[y][x + 1] = d[y][x + 1]!!.add(err.mul(7.0 / 16))
            }
            if (x - 1 >= 0 && y + 1 < h) {
                d[y + 1][x - 1] = d[y + 1][x - 1]!!.add(err.mul(3.0 / 16))
            }
            if (y + 1 < h) {
                d[y + 1][x] = d[y + 1][x]!!.add(err.mul(5.0 / 16))
            }
            if (x + 1 < w && y + 1 < h) {
                d[y + 1][x + 1] = d[y + 1][x + 1]!!.add(err.mul(1.0 / 16))
            }
        }
    }
    return this
}

internal class C3(var r: Int, var g: Int, var b: Int) {

    fun add(o: C3): C3 {
        return C3(r + o.r, g + o.g, b + o.b)
    }

    fun clamp(c: Int): Int {
        return max(0, min(255, c))
    }

    fun diff(o: C3?): Int {
        val Rdiff = o!!.r - r
        val Gdiff = o.g - g
        val Bdiff = o.b - b
        return Rdiff * Rdiff + Gdiff * Gdiff + Bdiff * Bdiff
    }

    fun mul(d: Double): C3 {
        return C3((d * r).toInt(), (d * g).toInt(), (d * b).toInt())
    }

    fun sub(o: C3): C3 {
        return C3(r - o.r, g - o.g, b - o.b)
    }

    fun toColor(): Int {
        return Color.rgb(clamp(r), clamp(g), clamp(b))
    }
}

internal object FloydSteinbergDithering {
    fun findClosestPaletteColor(c: C3?, palette: Array<C3>): C3 {
        var closest = palette[0]
        for (n in palette) {
            if (n.diff(c) < closest.diff(c)) {
                closest = n
            }
        }
        return closest
    }
}

fun View.toBitmap(
    totalWidth: Int = measuredWidth,
    totalHeight: Int = measuredHeight,
    rect: Rect
): Bitmap {
    setLayerType(View.LAYER_TYPE_SOFTWARE, Paint().apply { isAntiAlias = false })
    val b = Bitmap.createBitmap(totalWidth, totalHeight, Bitmap.Config.ARGB_8888)
    val c = Canvas(b).apply {
        drawFilter = PaintFlagsDrawFilter(Paint.ANTI_ALIAS_FLAG, 0)
    }
    measure(totalWidth, totalHeight)
    layout(0, 0, totalHeight, totalWidth)

    draw(c)
    return Bitmap.createBitmap(b, rect.left, rect.top, rect.width(), rect.height())
}

fun Bitmap.scale(dstWidth: Int, dstHeight: Int): Bitmap =
    Bitmap.createScaledBitmap(this, dstWidth, dstHeight, false)

private fun Bitmap.rotate(degrees: Float = 90f): Bitmap =
    Bitmap.createBitmap(
        this,
        0,
        0,
        width,
        height,
        Matrix().apply { postRotate(degrees) },
        false
    )