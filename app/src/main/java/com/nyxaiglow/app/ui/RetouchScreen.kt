package com.nyxaiglow.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Coral = Color(0xFFFF9A8B)
private val CoralDeep = Color(0xFF96463B)
private val SurfaceDark = Color(0xFF171515)
private val SurfaceRaised = Color(0xFF242020)

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
    Column(
        Modifier
            .fillMaxSize()
            .background(SurfaceDark)
            .padding(start = 16.dp, top = 72.dp, end = 16.dp, bottom = 104.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("AI CORE V2.4 ACTIVE", color = Color.White.copy(alpha = .78f), fontSize = 10.sp, letterSpacing = 1.sp)
                Surface(color = SurfaceRaised, shape = RoundedCornerShape(50)) {
                    Text("HOLD BEFORE", color = Color.White, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                }
            }
            Spacer(Modifier.height(18.dp))
            Surface(color = Color.Black.copy(alpha = .34f), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().height(310.dp)) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    RetouchGlowReticle(Modifier.size(172.dp), .72f, .62f)
                    Text("98.4% NATURAL MATCH", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.align(Alignment.TopCenter).padding(top = 18.dp))
                }
            }
            Spacer(Modifier.height(18.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                RetouchTool("Skin", Icons.Default.AutoAwesome, selectedTool == "Skin") { onToolSelected("Skin") }
                RetouchTool("Shape", Icons.Default.PhotoLibrary, selectedTool == "Shape") { onToolSelected("Shape") }
                RetouchTool("Light", Icons.Default.FlashOn, selectedTool == "Light") { onToolSelected("Light") }
                RetouchTool("Makeup", Icons.Default.Palette, selectedTool == "Makeup") { onToolSelected("Makeup") }
            }
            Spacer(Modifier.height(18.dp))
            Text("PRESETS & TONE", color = Color.White.copy(alpha = .64f), fontSize = 10.sp, letterSpacing = 1.sp)
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(listOf("Smooth", "Freckles", "Matte", "Dewy", "Refine")) { item ->
                    Surface(
                        color = if (item == selectedPreset) Coral else SurfaceRaised,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.clickable { onPresetSelected(item) }
                    ) {
                        Column(Modifier.width(58.dp).padding(7.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(Modifier.size(44.dp).clip(RoundedCornerShape(8.dp)).background(if (item == selectedPreset) CoralDeep else Color(0xFF3B3331)))
                            Spacer(Modifier.height(5.dp))
                            Text(item, color = if (item == selectedPreset) CoralDeep else Color.White, fontSize = 9.sp)
                        }
                    }
                }
            }
            Spacer(Modifier.height(18.dp))
            Text("Smoothing intensity", color = Color.White, fontSize = 12.sp)
            Slider(
                value = smoothingIntensity,
                onValueChange = onSmoothingChange,
                valueRange = 0f..1f,
                colors = androidx.compose.material3.SliderDefaults.colors(thumbColor = Coral, activeTrackColor = Coral, inactiveTrackColor = SurfaceRaised)
            )
            Surface(color = SurfaceRaised, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().clickable(onClick = onTextureToggle)) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Subtle Micro-Texture", color = Color.White, fontSize = 12.sp)
                        Text("Preserves natural pores & grain", color = Color.White.copy(alpha = .58f), fontSize = 10.sp)
                    }
                    Text(if (preserveTexture) "ON" else "OFF", color = if (preserveTexture) Coral else Color.White.copy(alpha = .5f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = onReset, colors = ButtonDefaults.buttonColors(containerColor = SurfaceRaised, contentColor = Color.White), modifier = Modifier.weight(1f)) {
                Text("RESET", fontSize = 11.sp)
            }
            Button(onClick = onApply, colors = ButtonDefaults.buttonColors(containerColor = Coral, contentColor = CoralDeep), modifier = Modifier.weight(2f)) {
                Text("APPLY TO PREVIEW", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun RetouchTool(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, selected: Boolean, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(onClick = onClick)) {
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
