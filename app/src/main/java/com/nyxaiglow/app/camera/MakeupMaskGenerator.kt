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
    private val maskBitmap = Bitmap.createBitmap(MASK_SIZE, MASK_SIZE, Bitmap.Config.ARGB_8888)
    private val canvas = Canvas(maskBitmap)

    @Synchronized
    fun generateMask(result: FaceLandmarkerResult, rotationDegrees: Int = 0, mirrorX: Boolean = false): Bitmap {
        // MediaPipe coordinates are converted once here; the renderer mirrors the camera texture once.
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        val faces = result.faceLandmarks()
        if (faces.isEmpty()) return Bitmap.createBitmap(maskBitmap)

        val landmarks = faces[0]
        fun point(index: Int): Pair<Float, Float>? {
            if (index !in landmarks.indices) return null
            val landmark = landmarks[index]
            val normalized = CoordinateConverter.fromRotatedImage(landmark.x(), landmark.y(), rotationDegrees, mirrorX)
            return (normalized.first * MASK_SIZE) to (normalized.second * MASK_SIZE)
        }

        paint.color = Color.argb(205, 255, 0, 0)
        drawPolygon(CoordinateConverter.validIndices(LIP_INDICES, landmarks.size).mapNotNull(::point))

        paint.color = Color.argb(145, 0, 255, 0)
        point(116)?.let { drawFeatheredCircle(it.first, it.second) }
        point(345)?.let { drawFeatheredCircle(it.first, it.second) }

        return Bitmap.createBitmap(maskBitmap)
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

    private companion object {
        const val MASK_SIZE = 256
        val LIP_INDICES = intArrayOf(
            61, 185, 40, 39, 37, 0, 267, 269, 270, 409,
            291, 375, 321, 405, 314, 17, 84, 181, 91, 146
        )
    }
}
