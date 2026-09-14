package com.nyxaiglow.app.camera

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import kotlin.math.max
import kotlin.math.min

class AmbientLightAnalyzer(
    private val onLightChanged: (Float) -> Unit
) : ImageAnalysis.Analyzer {
    private var lastSampleTime = 0L

    override fun analyze(image: ImageProxy) {
        val now = System.currentTimeMillis()
        if (now - lastSampleTime < SAMPLE_INTERVAL_MS) {
            image.close()
            return
        }
        lastSampleTime = now

        val buffer = image.planes.firstOrNull()?.buffer
        if (buffer == null || !buffer.hasRemaining()) {
            image.close()
            return
        }

        var sum = 0L
        var samples = 0
        val step = max(1, buffer.remaining() / 120)
        var index = buffer.position()
        while (index < buffer.limit()) {
            sum += buffer.get(index).toInt() and 0xFF
            samples++
            index += step
        }
        val luminance = if (samples == 0) 0.5f else (sum.toFloat() / samples / 255f)
        onLightChanged(min(1f, max(0f, luminance)))
        image.close()
    }

    private companion object {
        const val SAMPLE_INTERVAL_MS = 250L
    }
}
