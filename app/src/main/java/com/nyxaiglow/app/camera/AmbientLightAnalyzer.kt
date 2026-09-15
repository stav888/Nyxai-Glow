package com.nyxaiglow.app.camera

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import android.os.SystemClock
import kotlin.math.max
class AmbientLightAnalyzer(
    private val onLightChanged: (Float) -> Unit
) : ImageAnalysis.Analyzer {
    private var lastSampleTime = 0L

    override fun analyze(image: ImageProxy) {
        try {
            val now = SystemClock.elapsedRealtime()
            if (now - lastSampleTime < SAMPLE_INTERVAL_MS) return
            lastSampleTime = now

            val buffer = image.planes.firstOrNull()?.buffer ?: return
            if (!buffer.hasRemaining()) return

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
            onLightChanged(luminance.coerceIn(0f, 1f))
        } finally {
            image.close()
        }
    }

    private companion object {
        const val SAMPLE_INTERVAL_MS = 250L
    }
}
