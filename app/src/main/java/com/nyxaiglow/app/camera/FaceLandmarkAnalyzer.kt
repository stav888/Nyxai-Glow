package com.nyxaiglow.app.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.os.SystemClock
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.core.ImageProcessingOptions
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarker
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.max

class FaceLandmarkAnalyzer(
    context: Context,
    private val onLandmarksDetected: (FaceLandmarkerResult) -> Unit,
    private val onLightChanged: (Float) -> Unit,
    private val onError: (String) -> Unit = {}
) : ImageAnalysis.Analyzer {
    private var lastFaceFrameTime = 0L
    private var lastLightSampleTime = 0L
    private val bitmapLock = Any()
    private var inFlightBitmap: Bitmap? = null
    private val closed = AtomicBoolean(false)
    private val faceLandmarker: FaceLandmarker? = createFaceLandmarker(
        context,
        onLandmarksDetected,
        onError,
        ::recycleCompletedBitmap
    )

    override fun analyze(image: ImageProxy) {
        try {
            val now = SystemClock.uptimeMillis()
            if (now - lastLightSampleTime >= LIGHT_SAMPLE_INTERVAL_MS) {
                lastLightSampleTime = now
                sampleLuminance(image)?.let(onLightChanged)
            }
            val landmarker = faceLandmarker ?: return
            if (now - lastFaceFrameTime < FACE_SAMPLE_INTERVAL_MS) {
                return
            }
            lastFaceFrameTime = now
            val bitmap = image.toRgbaBitmap() ?: return
            synchronized(bitmapLock) {
                if (inFlightBitmap != null) {
                    bitmap.recycle()
                    return
                }
                inFlightBitmap = bitmap
            }
            val mpImage = BitmapImageBuilder(bitmap).build()
            val processingOptions = ImageProcessingOptions.builder()
                .setRotationDegrees(image.imageInfo.rotationDegrees)
                .build()
            try {
                landmarker.detectAsync(mpImage, processingOptions, now)
            } catch (exception: Exception) {
                synchronized(bitmapLock) {
                    if (inFlightBitmap === bitmap) inFlightBitmap = null
                }
                bitmap.recycle()
                throw exception
            }
        } catch (exception: Exception) {
            onError("${exception.javaClass.simpleName}: ${exception.message ?: "Face landmarking failed"}")
        } finally {
            image.close()
        }
    }

    private fun sampleLuminance(image: ImageProxy): Float? {
        val plane = image.planes.firstOrNull() ?: return null
        val buffer = plane.buffer.duplicate()
        if (!buffer.hasRemaining()) return null
        val columns = 12
        val rows = 10
        var sum = 0L
        var samples = 0
        for (row in 0 until rows) {
            val y = row * image.height / rows
            for (column in 0 until columns) {
                val x = column * image.width / columns
                val offset = buffer.position() + y * plane.rowStride + x * plane.pixelStride
                if (offset < buffer.limit()) {
                    sum += buffer.get(offset).toInt() and 0xFF
                    samples++
                }
            }
        }
        return if (samples == 0) null else (sum.toFloat() / samples / 255f).coerceIn(0f, 1f)
    }

    fun close() {
        if (!closed.compareAndSet(false, true)) return
        faceLandmarker?.close()
        synchronized(bitmapLock) {
            inFlightBitmap?.recycle()
            inFlightBitmap = null
        }
    }

    private fun recycleCompletedBitmap() {
        synchronized(bitmapLock) {
            inFlightBitmap?.recycle()
            inFlightBitmap = null
        }
    }

    private companion object {
        const val MODEL_NAME = "face_landmarker.task"
        const val FACE_SAMPLE_INTERVAL_MS = 100L
        const val LIGHT_SAMPLE_INTERVAL_MS = 250L
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

private fun createFaceLandmarker(
    context: Context,
    onLandmarksDetected: (FaceLandmarkerResult) -> Unit,
    onError: (String) -> Unit,
    onFrameCompleted: () -> Unit
): FaceLandmarker? {
    fun options(delegate: Delegate): FaceLandmarker.FaceLandmarkerOptions =
        FaceLandmarker.FaceLandmarkerOptions.builder()
            .setBaseOptions(
                BaseOptions.builder()
                    .setModelAssetPath(FaceLandmarkAnalyzer.MODEL_NAME)
                    .setDelegate(delegate)
                    .build()
            )
            .setRunningMode(RunningMode.LIVE_STREAM)
            .setNumFaces(1)
            .setMinFaceDetectionConfidence(0.5f)
            .setMinTrackingConfidence(0.5f)
            .setResultListener { result, _ ->
                onFrameCompleted()
                onLandmarksDetected(result)
            }
            .setErrorListener { error ->
                onFrameCompleted()
                onError(error.message ?: "Face landmarking unavailable")
            }
            .build()

    return try {
        FaceLandmarker.createFromOptions(context, options(Delegate.GPU))
    } catch (gpuException: Exception) {
        try {
            FaceLandmarker.createFromOptions(context, options(Delegate.CPU))
        } catch (cpuException: Exception) {
            onError(cpuException.message ?: gpuException.message ?: "Face landmark model unavailable")
            null
        }
    }
}