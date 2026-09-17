package com.nyxiaglow.app.ui

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasRole
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeRight
import androidx.compose.ui.semantics.Role
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RetouchScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun toolsAndPresetsAreVisible() {
        composeRule.setContent { TestRetouchScreen() }

        composeRule.onNodeWithText("Skin").assertIsDisplayed().assertIsSelected()
        composeRule.onNodeWithText("Shape").assertIsDisplayed()
        composeRule.onNodeWithText("Light").assertIsDisplayed()
        composeRule.onNodeWithText("Makeup").assertIsDisplayed()
        composeRule.onNodeWithText("Smooth").assertIsDisplayed().assertIsSelected()
        composeRule.onNodeWithText("Freckles").assertIsDisplayed()
        composeRule.onNodeWithText("Matte").assertIsDisplayed()
        composeRule.onNodeWithText("Dewy").assertIsDisplayed()
        composeRule.onNodeWithText("Refine").assertIsDisplayed()
    }

    @Test
    fun everyPresetCanBeSelected() {
        composeRule.setContent { TestRetouchScreen() }

        listOf("Smooth", "Freckles", "Matte", "Dewy", "Refine").forEach { preset ->
            composeRule.onNodeWithText(preset).performClick().assertIsSelected()
        }
    }

    @Test
    fun smoothingSliderReportsBoundedValue() {
        val values = mutableListOf<Float>()
        composeRule.setContent {
            RetouchScreen(
                preserveTexture = true,
                smoothingIntensity = 0.45f,
                selectedTool = "Skin",
                selectedPreset = "Smooth",
                onTextureToggle = {},
                onSmoothingChange = { values += it },
                onToolSelected = {},
                onPresetSelected = {},
                onReset = {},
                onApply = {}
            )
        }

        composeRule.onNode(hasRole(Role.Slider)).performTouchInput { swipeRight() }

        assert(values.isNotEmpty())
        assert(values.all { it in 0f..1f })
    }

    @Test
    fun textureToggleInvokesCallback() {
        var toggleCount = 0
        composeRule.setContent {
            RetouchScreen(
                preserveTexture = true,
                smoothingIntensity = 0.45f,
                selectedTool = "Skin",
                selectedPreset = "Smooth",
                onTextureToggle = { toggleCount++ },
                onSmoothingChange = {},
                onToolSelected = {},
                onPresetSelected = {},
                onReset = {},
                onApply = {}
            )
        }

        composeRule.onNodeWithText("Subtle Micro-Texture").performClick()

        assert(toggleCount == 1)
    }

    @Test
    fun selectingToolAndPresetUpdatesSemantics() {
        composeRule.setContent { TestRetouchScreen() }

        composeRule.onNodeWithText("Makeup").performClick()
        composeRule.onNodeWithText("Dewy").performClick()

        composeRule.onNodeWithText("Makeup").assertIsSelected()
        composeRule.onNodeWithText("Dewy").assertIsSelected()
    }

    @Test
    fun resetAndApplyInvokeCallbacks() {
        val resetCalled = mutableStateOf(false)
        val applyCalled = mutableStateOf(false)
        composeRule.setContent {
            RetouchScreen(
                preserveTexture = true,
                smoothingIntensity = 0.45f,
                selectedTool = "Skin",
                selectedPreset = "Smooth",
                onTextureToggle = {},
                onSmoothingChange = {},
                onToolSelected = {},
                onPresetSelected = {},
                onReset = { resetCalled.value = true },
                onApply = { applyCalled.value = true }
            )
        }

        composeRule.onNodeWithText("RESET").performClick()
        composeRule.onNodeWithText("APPLY TO PREVIEW").performClick()

        assert(resetCalled.value)
        assert(applyCalled.value)
    }

    @Composable
    private fun TestRetouchScreen() {
        val selectedTool = mutableStateOf("Skin")
        val selectedPreset = mutableStateOf("Smooth")

        RetouchScreen(
            preserveTexture = true,
            smoothingIntensity = 0.45f,
            selectedTool = selectedTool.value,
            selectedPreset = selectedPreset.value,
            onTextureToggle = {},
            onSmoothingChange = {},
            onToolSelected = { selectedTool.value = it },
            onPresetSelected = { selectedPreset.value = it },
            onReset = {},
            onApply = {}
        )
    }
}