package com.nyxaiglow.app.camera

import com.nyxaiglow.app.ui.RetouchState
import com.nyxaiglow.app.ui.retouchApplyMessage
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CameraVisualSafetyTest {
    @Test
    fun landmarkIndicesSkipUnavailableValues() {
        assertArrayEquals(
            intArrayOf(0, 2),
            MakeupMaskGenerator.CoordinateConverter.validIndices(intArrayOf(-1, 0, 2, 99), 3)
        )
        assertTrue(MakeupMaskGenerator.CoordinateConverter.validIndices(intArrayOf(1, 2), 0).isEmpty())
    }

    @Test
    fun coordinateConversionHandlesRotationAndFrontMirror() {
        val rotated = MakeupMaskGenerator.CoordinateConverter.fromRotatedImage(.2f, .3f, 90, false)
        assertEquals(.3f, rotated.first, .0001f)
        assertEquals(.8f, rotated.second, .0001f)

        val mirrored = MakeupMaskGenerator.CoordinateConverter.fromRotatedImage(.2f, .3f, 0, true)
        assertEquals(.8f, mirrored.first, .0001f)
        assertTrue(mirrored.second in 0f..1f)
    }

    @Test
    fun coordinateConversionClampsMaskBounds() {
        val point = MakeupMaskGenerator.CoordinateConverter.fromRotatedImage(-2f, 3f, 0, false)
        assertEquals(0f, point.first, .0001f)
        assertEquals(1f, point.second, .0001f)
    }

    @Test
    fun shaderParametersAreClamped() {
        assertEquals(0f, RendererParameters.clampStrength(-1f), .0001f)
        assertEquals(1f, RendererParameters.clampStrength(2f), .0001f)
        assertEquals(.4f, RendererParameters.clampStrength(.4f), .0001f)
    }

    @Test
    fun retouchResetRestoresDefaults() {
        val changed = RetouchState(false, .9f, "Makeup", "Dewy")
        assertEquals(RetouchState(), changed.reset())
    }

    @Test
    fun applyMessageDoesNotClaimToSaveImage() {
        assertTrue(retouchApplyMessage(false).contains("No source image"))
        assertTrue(retouchApplyMessage(true).contains("source image unchanged"))
    }
}
