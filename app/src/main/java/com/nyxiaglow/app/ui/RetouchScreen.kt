package com.nyxiaglow.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Coral = Color(0xFFFF9A8B)
private val CoralDeep = Color(0xFF96463B)
private val SurfaceDark = Color(0xFF171515)
private val SurfaceRaised = Color(0xFF242020)
private val Ink = Color(0xFF0E0E0E)
private val CoralSoft = Color(0xFFFFC2B9)
private val TextMuted = Color(0xFFDAC1BD)

@Composable
fun RetouchScreen(
    preserveTexture: Boolean,
    smoothingIntensity: Float,
    selectedTool: String,
    selectedPreset: String,
    onTextureToggle: () -> Unit,
    onSmoothingChange: (Float) -> Unit,
    onToolSelected: (String) -> Unit,
    onPresetSelected: (String) -> Unit,
    onReset: () -> Unit,
    onApply: () -> Unit
) {
    BoxWithConstraints(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(SurfaceDark, Ink)))) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, top = 22.dp, bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("RETOUCH CONTROL DECK", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Medium, letterSpacing = .9.sp)
                    Text("Tactile studio drawer", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                }
                Surface(color = Coral.copy(alpha = .16f), shape = RoundedCornerShape(50)) {
                    Text("AI CORE V2.4", color = CoralSoft, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp))
                }
            }
            Surface(color = Color.Black.copy(alpha = .28f), shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth().weight(1f).heightIn(min = 220.dp, max = 290.dp)) {
                Box(Modifier.fillMaxSize()) {
                    Box(Modifier.fillMaxWidth().height(90.dp).background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = .7f), Color.Transparent))))
                    Row(Modifier.align(Alignment.TopCenter).padding(top = 14.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TelemetryPill("98.4% NATURAL MATCH")
                        TelemetryPill("LIVE")
                    }
                    RetouchGlowReticle(Modifier.align(Alignment.Center).size(164.dp), .72f, .62f)
                    Text("HOLD BEFORE", color = TextMuted, fontSize = 10.sp, letterSpacing = .6.sp, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 14.dp))
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                RetouchTool("Skin", Icons.Default.AutoAwesome, selectedTool == "Skin") { onToolSelected("Skin") }
                RetouchTool("Shape", Icons.Default.PhotoLibrary, selectedTool == "Shape") { onToolSelected("Shape") }
                RetouchTool("Light", Icons.Default.FlashOn, selectedTool == "Light") { onToolSelected("Light") }
                RetouchTool("Makeup", Icons.Default.Palette, selectedTool == "Makeup") { onToolSelected("Makeup") }
            }
            Column(Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("PRESETS & TONE", color = TextMuted, fontSize = 10.sp, letterSpacing = .8.sp)
                    Text("SWIPE TO EXPLORE", color = CoralSoft, fontSize = 9.sp, letterSpacing = .5.sp)
                }
                Spacer(Modifier.height(7.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(listOf("Smooth", "Freckles", "Matte", "Dewy", "Refine")) { item ->
                        val selected = item == selectedPreset
                        Surface(
                            color = if (selected) Coral else SurfaceRaised,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.clickable(role = Role.Button) { onPresetSelected(item) }.semantics {
                                this.selected = selected
                                stateDescription = if (selected) "Selected" else "Not selected"
                            }
                        ) {
                            Column(Modifier.width(58.dp).padding(7.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(Modifier.size(44.dp).clip(RoundedCornerShape(8.dp)).background(if (selected) CoralDeep else Color(0xFF3B3331)))
                                Spacer(Modifier.height(5.dp))
                                Text(item, color = if (selected) CoralDeep else Color.White, fontSize = 9.sp)
                            }
                        }
                    }
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("SMOOTHING INTENSITY", color = TextMuted, fontSize = 10.sp, letterSpacing = .7.sp)
                Text("${(smoothingIntensity * 100).toInt()}%", color = CoralSoft, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
            Slider(value = smoothingIntensity, onValueChange = onSmoothingChange, valueRange = 0f..1f, modifier = Modifier.semantics {
                stateDescription = "Smoothing intensity ${(smoothingIntensity * 100).toInt()} percent"
            }, colors = androidx.compose.material3.SliderDefaults.colors(thumbColor = Coral, activeTrackColor = Coral, inactiveTrackColor = SurfaceRaised))
            Surface(color = SurfaceRaised.copy(alpha = .9f), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().clickable(role = Role.Switch, onClick = onTextureToggle).semantics {
                stateDescription = if (preserveTexture) "On" else "Off"
            }) {
                Row(Modifier.padding(horizontal = 13.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Subtle Micro-Texture", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text("Preserves natural pores & grain", color = TextMuted, fontSize = 10.sp)
                    }
                    Text(if (preserveTexture) "ON" else "OFF", color = if (preserveTexture) CoralSoft else TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = onReset, colors = ButtonDefaults.buttonColors(containerColor = SurfaceRaised, contentColor = Color.White), modifier = Modifier.weight(1f)) { Text("RESET", fontSize = 11.sp) }
                Button(onClick = onApply, colors = ButtonDefaults.buttonColors(containerColor = Coral, contentColor = CoralDeep), modifier = Modifier.weight(2f)) { Text("APPLY TO PREVIEW", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            }
        }
    }
}

@Composable
private fun TelemetryPill(label: String) {
    Surface(color = Ink.copy(alpha = .72f), shape = RoundedCornerShape(50)) {
        Text(label, color = if (label == "LIVE") CoralSoft else Color.White, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, letterSpacing = .45.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
    }
}

@Composable
private fun RetouchTool(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, selected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(role = Role.Button, onClick = onClick)
            .semantics {
                this.selected = selected
                stateDescription = if (selected) "Selected" else "Not selected"
            }
    ) {
        Surface(color = if (selected) Coral.copy(alpha = .2f) else SurfaceRaised, shape = CircleShape, modifier = Modifier.size(42.dp)) {
            Icon(icon, label, tint = if (selected) Coral else Color.White.copy(alpha = .72f), modifier = Modifier.padding(12.dp))
        }
        Spacer(Modifier.height(5.dp))
        Text(label, color = if (selected) Coral else Color.White.copy(alpha = .7f), fontSize = 10.sp)
    }
}

@Composable
private fun RetouchGlowReticle(modifier: Modifier, progress: Float, ambient: Float) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Box(Modifier.fillMaxSize().clip(CircleShape).background(Brush.radialGradient(listOf(Coral.copy(alpha = .2f), Color.Transparent))))
        Canvas(Modifier.fillMaxSize()) {
            drawArc(Brush.sweepGradient(listOf(Coral, Coral.copy(alpha = .15f), Coral)), -90f, 360f * progress, false, style = Stroke(7.dp.toPx(), cap = StrokeCap.Round))
            drawCircle(Coral.copy(alpha = .82f), style = Stroke(2.dp.toPx()))
            drawCircle(Coral.copy(alpha = .9f), radius = size.minDimension / 2f - 10.dp.toPx(), style = Stroke(2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(8.dp.toPx(), 12.dp.toPx()))))
        }
        Box(Modifier.size(12.dp).clip(CircleShape).background(Brush.linearGradient(listOf(Coral, CoralDeep))))
    }
}
