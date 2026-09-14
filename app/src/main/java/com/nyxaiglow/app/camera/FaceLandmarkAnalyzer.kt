package com.nyxaiglow.app.camera

import android.content.Context
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mediapipe.framework.image.MediaImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarker
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult

class FaceLandmarkAnalyzer(
    context: Context,
    private val onLandmarksDetected: (FaceLandmarkerResult) -> Unit,
    private val onError: (String) -> Unit = {}
) : ImageAnalysis.Analyzer {
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

    override fun analyze(image: ImageProxy) {
        val landmarker = faceLandmarker
        if (landmarker == null) {
            image.close()
            return
        }
        val mediaImage = image.image
        if (mediaImage == null) {
            image.close()
            return
        }
        try {
            val mpImage = MediaImageBuilder(mediaImage).build()
            landmarker.detectAsync(mpImage, System.currentTimeMillis())
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
    }
}