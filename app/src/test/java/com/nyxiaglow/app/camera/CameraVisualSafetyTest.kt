package com.nyxiaglow.app.camera

import com.nyxiaglow.app.ui.RetouchState
import com.nyxiaglow.app.ui.retouchApplyMessage
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
    fun coordinateConversionHandlesAllRightAngleRotations() {
        assertEquals(0.2f to 0.3f, MakeupMaskGenerator.CoordinateConverter.fromRotatedImage(.2f, .3f, 0, false))
        assertEquals(0.3f to 0.8f, MakeupMaskGenerator.CoordinateConverter.fromRotatedImage(.2f, .3f, 90, false))
        assertEquals(0.8f to 0.7f, MakeupMaskGenerator.CoordinateConverter.fromRotatedImage(.2f, .3f, 180, false))
        assertEquals(0.7f to 0.2f, MakeupMaskGenerator.CoordinateConverter.fromRotatedImage(.2f, .3f, 270, false))
    }

    @Test
    fun coordinateConversionClampsMaskBounds() {
        val point = MakeupMaskGenerator.CoordinateConverter.fromRotatedImage(-2f, 3f, 0, false)
        assertEquals(0f, point.first, .0001f)
        assertEquals(1f, point.second, .0001f)
    }

    @Test
    fun landmarkChangeThresholdIgnoresSmallMotion() {
        val previous = floatArrayOf(.2f, .3f, .4f, .5f)
        val unchanged = floatArrayOf(.202f, .298f, .4f, .5f)
        val changed = floatArrayOf(.204f, .3f, .4f, .5f)
        assertTrue(!LandmarkChangeDetector.changed(previous, unchanged))
        assertTrue(LandmarkChangeDetector.changed(previous, changed))
    }

    @Test
    fun shaderParametersAreClamped() {
        assertEquals(0f, RendererParameters.clampStrength(-1f), .0001f)
        assertEquals(1f, RendererParameters.clampStrength(2f), .0001f)
        assertEquals(.4f, RendererParameters.clampStrength(.4f), .0001f)
    }

    @Test
    fun rendererReleaseStateIsIdempotent() {
        val state = RendererReleaseState()
        assertTrue(state.request())
        assertTrue(!state.request())
        assertTrue(state.markResourcesReleased())
        assertTrue(!state.markResourcesReleased())
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
