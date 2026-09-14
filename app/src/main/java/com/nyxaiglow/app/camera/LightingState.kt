package com.nyxaiglow.app.camera

fun lightingState(ambient: Float): String = when {
    ambient < 0.2f -> "Low light - glow boosted"
    ambient < 0.6f -> "Balanced light"
    ambient < 0.85f -> "Bright - highlights softened"
    else -> "Harsh light - smoothing adjusted"
}