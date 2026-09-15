package com.nyxaiglow.app.camera

import android.content.Context
import android.os.Build
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import android.os.SystemClock
import com.google.mediapipe.framework.image.MediaImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.core.ImageProcessingOptions
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarker
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult
import kotlin.math.max
import java.util.concurrent.atomic.AtomicBoolean

class FaceLandmarkAnalyzer(
    context: Context,
    private val onLandmarksDetected: (FaceLandmarkerResult) -> Unit,
    private val onLightChanged: (Float) -> Unit,
    private val onError: (String) -> Unit = {}
) : ImageAnalysis.Analyzer {
    private var lastFaceFrameTime = 0L
    private var lastLightSampleTime = 0L
    private val processingFrame = AtomicBoolean(false)
    private val faceLandmarker: FaceLandmarker? = createFaceLandmarker(context, onLandmarksDetected, onError, processingFrame)

    override fun analyze(image: ImageProxy) {
        try {
            sampleAmbientLight(image)
            val landmarker = faceLandmarker ?: return
            val mediaImage = image.image ?: return
            val now = SystemClock.elapsedRealtime()
            if (now - lastFaceFrameTime < FACE_SAMPLE_INTERVAL_MS || !processingFrame.compareAndSet(false, true)) {
                return
            }
            lastFaceFrameTime = now
            val mpImage = MediaImageBuilder(mediaImage).build()
            val processingOptions = ImageProcessingOptions.builder()
                .setRotationDegrees(image.imageInfo.rotationDegrees)
                .build()
            landmarker.detectAsync(mpImage, processingOptions, now)
        } catch (exception: Exception) {
            processingFrame.set(false)
            onError(exception.message ?: "Face landmarking failed")
        } finally {
            image.close()
        }
    }

    private fun sampleAmbientLight(image: ImageProxy) {
        val now = SystemClock.elapsedRealtime()
        if (now - lastLightSampleTime < LIGHT_SAMPLE_INTERVAL_MS) return
        lastLightSampleTime = now
        val yPlane = image.planes.firstOrNull() ?: return
        val buffer = yPlane.buffer.duplicate()
        if (!buffer.hasRemaining() || image.width == 0 || image.height == 0) return

        val sampleCount = 120
        val y = image.height / 2
        var sum = 0L
        repeat(sampleCount) { index ->
            val x = (index * image.width / sampleCount).coerceIn(0, image.width - 1)
            val offset = y * yPlane.rowStride + x * yPlane.pixelStride
            if (offset < buffer.limit()) sum += buffer.get(offset).toInt() and 0xFF
        }
        onLightChanged((sum.toFloat() / sampleCount / 255f).coerceIn(0f, 1f))
    }

    fun close() {
        processingFrame.set(false)
        faceLandmarker?.close()
    }

    private companion object {
        const val MODEL_NAME = "face_landmarker.task"
        const val FACE_SAMPLE_INTERVAL_MS = 100L
        const val LIGHT_SAMPLE_INTERVAL_MS = 250L
    }
}

private fun createFaceLandmarker(
    context: Context,
    onLandmarksDetected: (FaceLandmarkerResult) -> Unit,
    onError: (String) -> Unit,
    processingFrame: AtomicBoolean
): FaceLandmarker? {
    if (Build.SUPPORTED_ABIS.firstOrNull() != "arm64-v8a") {
        onError("Face landmarking is unavailable on this device")
        return null
    }

    fun options(delegate: Delegate): FaceLandmarker.FaceLandmarkerOptions =
        FaceLandmarker.FaceLandmarkerOptions.builder()
            .setBaseOptions(
                BaseOptions.builder()
                    .setModelAssetPath("face_landmarker.task")
                    .setDelegate(delegate)
                    .build()
            )
            .setRunningMode(RunningMode.LIVE_STREAM)
            .setNumFaces(1)
            .setMinFaceDetectionConfidence(0.5f)
            .setMinTrackingConfidence(0.5f)
            .setResultListener { result, _ ->
                processingFrame.set(false)
                onLandmarksDetected(result)
            }
            .setErrorListener { error ->
                processingFrame.set(false)
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