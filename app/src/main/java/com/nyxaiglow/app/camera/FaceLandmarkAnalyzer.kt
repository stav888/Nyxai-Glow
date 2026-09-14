package com.nyxaiglow.app.camera

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.core.ImageProcessingOptions
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarker
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult
import kotlin.math.max

class FaceLandmarkAnalyzer(
    context: Context,
    private val onLandmarksDetected: (FaceLandmarkerResult) -> Unit,
    private val onLightChanged: (Float) -> Unit,
    private val onError: (String) -> Unit = {}
) : ImageAnalysis.Analyzer {
    private var lastFaceFrameTime = 0L
    private val faceLandmarker: FaceLandmarker? = try {
        val baseOptions = BaseOptions.builder()
            .setModelAssetPath(MODEL_NAME)
            .setDelegate(Delegate.GPU)
            .build()
        FaceLandmarker.createFromOptions(
            context,
            FaceLandmarker.FaceLandmarkerOptions.builder()
                .setBaseOptions(baseOptions)
                .setRunningMode(RunningMode.LIVE_STREAM)
                .setNumFaces(1)
                .setMinFaceDetectionConfidence(0.5f)
                .setMinTrackingConfidence(0.5f)
                .setResultListener { result, _ -> onLandmarksDetected(result) }
                .setErrorListener { error -> onError(error.message ?: "Face landmarking unavailable") }
                .build()
        )
    } catch (exception: Exception) {
        onError(exception.message ?: "Face landmark model unavailable")
        null
    }

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(image: ImageProxy) {
        val landmarker = faceLandmarker
        if (landmarker == null) {
            image.close()
            return
        }
        if (image.image == null) {
            image.close()
            return
        }
        try {
            val luminancePlane = image.planes.firstOrNull()?.buffer?.duplicate()
            if (luminancePlane != null && luminancePlane.hasRemaining()) {
                val sampleCount = minOf(120, luminancePlane.remaining())
                val step = max(1, luminancePlane.remaining() / sampleCount)
                var sum = 0L
                var samples = 0
                for (index in 0 until luminancePlane.remaining() step step) {
                    sum += luminancePlane.get(index).toInt() and 0xFF
                    samples++
                }
                onLightChanged(if (samples == 0) 0.5f else (sum.toFloat() / samples / 255f).coerceIn(0f, 1f))
            }
            val now = System.currentTimeMillis()
            if (now - lastFaceFrameTime < FACE_SAMPLE_INTERVAL_MS) {
                return
            }
            lastFaceFrameTime = now
            val bitmap = image.toRgbaBitmap() ?: return
            val mpImage = BitmapImageBuilder(bitmap).build()
            val processingOptions = ImageProcessingOptions.builder()
                .setRotationDegrees(image.imageInfo.rotationDegrees)
                .build()
            landmarker.detectAsync(mpImage, processingOptions, now)
            bitmap.recycle()
        } catch (exception: Exception) {
            onError(exception.message ?: "Face landmarking failed")
        } finally {
            image.close()
        }
    }

    fun close() {
        faceLandmarker?.close()
    }

    private companion object {
        const val MODEL_NAME = "face_landmarker.task"
        const val FACE_SAMPLE_INTERVAL_MS = 100L
    }
}

private fun ImageProxy.toRgbaBitmap(): android.graphics.Bitmap? {
    val yPlane = planes.getOrNull(0) ?: return null
    val uPlane = planes.getOrNull(1) ?: return null
    val vPlane = planes.getOrNull(2) ?: return null
    val yBuffer = yPlane.buffer.duplicate()
    val uBuffer = uPlane.buffer.duplicate()
    val vBuffer = vPlane.buffer.duplicate()
    val nv21 = ByteArray(width * height + 2 * ((width + 1) / 2) * ((height + 1) / 2))
    var outputIndex = 0
    for (row in 0 until height) {
        val rowStart = yBuffer.position() + row * yPlane.rowStride
        for (column in 0 until width) {
            val sourceIndex = rowStart + column * yPlane.pixelStride
            if (sourceIndex >= yBuffer.limit()) return null
            nv21[outputIndex++] = yBuffer.get(sourceIndex)
        }
    }
    val chromaHeight = (height + 1) / 2
    val chromaWidth = (width + 1) / 2
    for (row in 0 until chromaHeight) {
        for (column in 0 until chromaWidth) {
            val uIndex = uBuffer.position() + row * uPlane.rowStride + column * uPlane.pixelStride
            val vIndex = vBuffer.position() + row * vPlane.rowStride + column * vPlane.pixelStride
            if (uIndex >= uBuffer.limit() || vIndex >= vBuffer.limit()) return null
            nv21[outputIndex++] = vBuffer.get(vIndex)
            nv21[outputIndex++] = uBuffer.get(uIndex)
        }
    }
    val yuvImage = YuvImage(nv21, ImageFormat.NV21, width, height, null)
    val jpeg = java.io.ByteArrayOutputStream()
    if (!yuvImage.compressToJpeg(Rect(0, 0, width, height), 85, jpeg)) return null
    return BitmapFactory.decodeByteArray(jpeg.toByteArray(), 0, jpeg.size())
}