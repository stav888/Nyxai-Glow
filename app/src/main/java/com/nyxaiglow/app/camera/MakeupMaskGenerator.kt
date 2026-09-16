package com.nyxaiglow.app.camera

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult

class MakeupMaskGenerator {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val maskBitmap = Bitmap.createBitmap(MASK_SIZE, MASK_SIZE, Bitmap.Config.ARGB_8888)
    private val canvas = Canvas(maskBitmap)

    @Synchronized
    fun generateMask(result: FaceLandmarkerResult): Bitmap {
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        val faces = result.faceLandmarks()
        if (faces.isEmpty()) return Bitmap.createBitmap(maskBitmap)

        val landmarks = faces[0]
        fun point(index: Int): Pair<Float, Float> {
            val landmark = landmarks[index]
            return ((1f - landmark.x()) * MASK_SIZE) to (landmark.y() * MASK_SIZE)
        }

        paint.color = Color.argb(205, 255, 0, 0)
        drawPolygon(LIP_INDICES.map(::point))

        paint.color = Color.argb(145, 0, 255, 0)
        val leftCheek = point(116)
        val rightCheek = point(345)
        canvas.drawCircle(leftCheek.first, leftCheek.second, 28f, paint)
        canvas.drawCircle(rightCheek.first, rightCheek.second, 28f, paint)

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

    private companion object {
        const val MASK_SIZE = 256
        val LIP_INDICES = intArrayOf(
            61, 185, 40, 39, 37, 0, 267, 269, 270, 409,
            291, 375, 321, 405, 314, 17, 84, 181, 91, 146
        )
    }
}
