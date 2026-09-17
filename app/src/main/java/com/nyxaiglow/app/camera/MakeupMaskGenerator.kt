package com.nyxaiglow.app.camera

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult
import kotlin.math.max
import kotlin.math.min

class MakeupMaskGenerator {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private var maskBitmap = Bitmap.createBitmap(DEFAULT_MASK_WIDTH, DEFAULT_MASK_HEIGHT, Bitmap.Config.ARGB_8888)
    private var canvas = Canvas(maskBitmap)

    @Synchronized
    fun generateMask(
        result: FaceLandmarkerResult,
        rotationDegrees: Int = 0,
        mirrorX: Boolean = false,
        width: Int = DEFAULT_MASK_WIDTH,
        height: Int = DEFAULT_MASK_HEIGHT
    ): Bitmap {
        ensureSize(width, height)
        // MediaPipe image coordinates are rotated and optionally mirrored here once.
        // The renderer samples the camera texture without an additional mirror.
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        val faces = result.faceLandmarks()
        if (faces.isEmpty()) return Bitmap.createBitmap(maskBitmap)

        val landmarks = faces[0]
        fun point(index: Int): Pair<Float, Float>? {
            if (index !in landmarks.indices) return null
            val landmark = landmarks[index]
            val normalized = CoordinateConverter.fromRotatedImage(landmark.x(), landmark.y(), rotationDegrees, mirrorX)
            return (normalized.first * maskBitmap.width) to (normalized.second * maskBitmap.height)
        }

        paint.color = Color.argb(205, 255, 0, 0)
        drawPolygon(
            CoordinateConverter
                .validIndices(LIP_INDICES, landmarks.size)
                .asList()
                .mapNotNull(::point)
        )

        paint.color = Color.argb(145, 0, 255, 0)
        point(116)?.let { drawFeatheredCircle(it.first, it.second) }
        point(345)?.let { drawFeatheredCircle(it.first, it.second) }

        return Bitmap.createBitmap(maskBitmap)
    }

    companion object {
        fun hasVisiblePixels(bitmap: Bitmap): Boolean {
            val pixels = IntArray(bitmap.width * bitmap.height)
            bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
            return pixels.any { Color.alpha(it) != 0 }
        }

        const val DEFAULT_MASK_WIDTH = 640
        const val DEFAULT_MASK_HEIGHT = 480
        private val LIP_INDICES = intArrayOf(
            61, 185, 40, 39, 37, 0, 267, 269, 270, 409,
            291, 375, 321, 405, 314, 17, 84, 181, 91, 146
        )
    }

    private fun ensureSize(width: Int, height: Int) {
        val safeWidth = width.coerceAtLeast(1)
        val safeHeight = height.coerceAtLeast(1)
        if (maskBitmap.width == safeWidth && maskBitmap.height == safeHeight) return
        maskBitmap.recycle()
        maskBitmap = Bitmap.createBitmap(safeWidth, safeHeight, Bitmap.Config.ARGB_8888)
        canvas = Canvas(maskBitmap)
    }

    private fun drawPolygon(points: List<Pair<Float, Float>>) {
        if (points.size < 3) return
        val path = Path().apply {
            moveTo(points.first().first, points.first().second)
            points.drop(1).forEach { lineTo(it.first, it.second) }
            close()
        }
        canvas.drawPath(path, paint)
    }

    private fun drawFeatheredCircle(x: Float, y: Float) {
        val radius = 28f
        paint.shader = android.graphics.RadialGradient(
            x,
            y,
            radius,
            Color.argb(145, 0, 255, 0),
            Color.TRANSPARENT,
            android.graphics.Shader.TileMode.CLAMP
        )
        canvas.drawCircle(x, y, radius, paint)
        paint.shader = null
    }

    object CoordinateConverter {
        fun validIndices(indices: IntArray, landmarkCount: Int): IntArray =
            indices.filter { it in 0 until landmarkCount }.toIntArray()

        fun fromRotatedImage(x: Float, y: Float, rotationDegrees: Int, mirrorX: Boolean): Pair<Float, Float> {
            val normalizedRotation = ((rotationDegrees % 360) + 360) % 360
            val rotated = when (normalizedRotation) {
                90 -> y to (1f - x)
                180 -> (1f - x) to (1f - y)
                270 -> (1f - y) to x
                else -> x to y
            }
            val convertedX = if (mirrorX) 1f - rotated.first else rotated.first
            return min(1f, max(0f, convertedX)) to min(1f, max(0f, rotated.second))
        }
    }

}
