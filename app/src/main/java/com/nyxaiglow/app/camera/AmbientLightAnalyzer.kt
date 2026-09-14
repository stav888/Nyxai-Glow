package com.nyxaiglow.app.camera

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import kotlin.math.max

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

        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        var sum = 0L
        val step = max(1, bytes.size / 120)
        var samples = 0
        for (index in bytes.indices step step) {
            sum += bytes[index].toInt() and 0xFF
            samples++
        }
        val luminance = if (samples == 0) 0.5f else (sum.toFloat() / samples / 255f)
        onLightChanged(luminance.coerceIn(0f, 1f))
        image.close()
    }

    private companion object {
        const val SAMPLE_INTERVAL_MS = 250L
    }
}
