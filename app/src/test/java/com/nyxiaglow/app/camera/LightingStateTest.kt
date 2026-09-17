package com.nyxiaglow.app.camera

import kotlin.test.Test
import kotlin.test.assertEquals

class LightingStateTest {
    @Test
    fun classifiesLightingBoundaries() {
        assertEquals("Low light - glow boosted", lightingState(0.19f))
        assertEquals("Balanced light", lightingState(0.2f))
        assertEquals("Bright - highlights softened", lightingState(0.6f))
        assertEquals("Harsh light - smoothing adjusted", lightingState(0.85f))
    }
}