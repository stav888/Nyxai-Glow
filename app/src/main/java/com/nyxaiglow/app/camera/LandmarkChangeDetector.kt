package com.nyxaiglow.app.camera

import kotlin.math.abs

internal object LandmarkChangeDetector {
    const val THRESHOLD = 0.003f

    fun changed(previous: FloatArray, current: FloatArray): Boolean {
        if (previous.size != current.size) return true
        return previous.indices.any { index -> abs(previous[index] - current[index]) >= THRESHOLD }
    }
}