package com.nyxaiglow.app

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Build
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Camera
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.nyxaiglow.app.camera.FaceLandmarkAnalyzer
import com.nyxaiglow.app.camera.lightingState
import com.nyxaiglow.app.ui.theme.NyxaiGlowTheme
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult
import java.util.concurrent.Executors
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.delay

private val Coral = Color(0xFFFF9A8B)
private val CoralDeep = Color(0xFF96463B)
private val Mist = Color(0xFFF5F1F0)
private val Ink = Color(0xFF111111)
private val SurfaceDark = Color(0xFF171515)
private val SurfaceRaised = Color(0xFF242020)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { NyxaiGlowTheme { NyxaiGlowApp() } }
    }
}

@Composable
private fun NyxaiGlowApp() {
    val context = LocalContext.current
    var cameraGranted by remember { mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
        cameraGranted = results[Manifest.permission.CAMERA] == true || ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }
    if (cameraGranted) {
        GlowStudio()
    } else {
        PermissionPrompt {
            val permissions = buildList {
                if (!cameraGranted) add(Manifest.permission.CAMERA)
            }
            permissionLauncher.launch(permissions.toTypedArray())
        }
    }
}

@Composable
private fun GlowStudio() {
    val context = LocalContext.current
    var glow by remember { mutableFloatStateOf(0.68f) }
    var ambient by remember { mutableFloatStateOf(0.58f) }
    var preset by remember { mutableStateOf("Soft") }
    var zoom by remember { mutableStateOf("1x") }
    var facing by remember { mutableStateOf(CameraSelector.LENS_FACING_FRONT) }
    var flashOn by remember { mutableStateOf(false) }
    var activeTab by remember { mutableStateOf("Camera") }
    var preserveTexture by remember { mutableStateOf(true) }
    var reticleVisible by remember { mutableStateOf(true) }
    var captureRequest by remember { mutableStateOf(0) }
    val captureLock = remember { AtomicBoolean(false) }
    var capturedUri by remember { mutableStateOf<Uri?>(null) }
    var statusMessage by remember { mutableStateOf("Ready") }
    var showSettings by remember { mutableStateOf(false) }
    var flashAvailable by remember { mutableStateOf(false) }
    val legacyStorageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            captureRequest++
        } else {
            captureLock.set(false)
            statusMessage = "Storage permission is required to save photos"
        }
    }
    fun requestCapture() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        ) {
            legacyStorageLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else {
            captureRequest++
        }
    }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            capturedUri = uri
            statusMessage = "Photo selected"
        }
    }

    LaunchedEffect(preset, zoom, flashOn, facing) {
        reticleVisible = true
        delay(2_000)
        reticleVisible = false
    }

    Box(Modifier.fillMaxSize().background(Ink)) {
        if (activeTab == "Retouch") {
            RetouchDashboard(preserveTexture, { preserveTexture = !preserveTexture }, { statusMessage = "Retouch saved" })
        } else {
            CameraPreview(Modifier.fillMaxSize(), facing, zoom, flashOn, captureRequest,
                onAmbient = { ambient = it },
                onLandmarksDetected = { result ->
                    if (result.faceLandmarks().isNotEmpty()) statusMessage = "Face detected"
                },
                onCapture = { uri ->
                    captureLock.set(false)
                    capturedUri = uri
                    statusMessage = "Photo captured"
                },
                onCameraError = {
                    captureLock.set(false)
                    statusMessage = it
                },
                onFlashAvailabilityChanged = {
                    flashAvailable = it
                    if (!it) flashOn = false
                }
            )
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Ink.copy(alpha = .78f), Color.Transparent, Ink.copy(alpha = .96f)))))
            Column(Modifier.fillMaxSize().padding(WindowInsets.navigationBars.asPaddingValues()), verticalArrangement = Arrangement.SpaceBetween) {
                TopBar(flashOn, flashAvailable, { flashOn = !flashOn }, { showSettings = true }, activeTab)
                Column(Modifier.fillMaxWidth().padding(bottom = 126.dp)) {
                    CameraOverlay(glow, ambient, preserveTexture, reticleVisible) { preserveTexture = !preserveTexture }
                    CameraDeck(
                        preset = preset,
                        zoom = zoom,
                        onPreset = { chosen ->
                            preset = chosen
                            glow = when (chosen) { "Radiant" -> .86f; "Velvet" -> .34f; "Defined" -> .57f; else -> .68f }
                        },
                        onZoom = { zoom = it },
                        onFlip = { facing = if (facing == CameraSelector.LENS_FACING_FRONT) CameraSelector.LENS_FACING_BACK else CameraSelector.LENS_FACING_FRONT },
                        onGallery = { galleryLauncher.launch("image/*") },
                        onCapture = { if (captureLock.compareAndSet(false, true)) requestCapture() }
                    )
                }
            }
        }
        Box(Modifier.align(Alignment.BottomCenter)) {
            BottomNavigation(activeTab) { tab ->
                activeTab = tab
                when (tab) {
                    "Gallery" -> galleryLauncher.launch("image/*")
                    "Looks" -> activeTab = "Retouch"
                    "Profile" -> showSettings = true
                }
            }
        }
        if (statusMessage != "Ready") {
            Surface(color = Ink.copy(alpha = .82f), shape = RoundedCornerShape(50), modifier = Modifier.align(Alignment.TopCenter).padding(top = 70.dp)) {
                Text(statusMessage, color = Color.White, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp))
            }
        }
        if (showSettings) {
            Dialog(onDismissRequest = { showSettings = false }) {
                Surface(color = Mist, shape = RoundedCornerShape(24.dp)) {
                    Column(Modifier.padding(22.dp)) {
                        Text("Camera settings", color = Ink, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(12.dp))
                        Text("Texture preservation is ${if (preserveTexture) "on" else "off"}.", color = Color(0xFF595F65), fontSize = 14.sp)
                        Text("Flash and zoom follow the connected camera hardware.", color = Color(0xFF595F65), fontSize = 14.sp)
                        TextButton(onClick = { showSettings = false }) { Text("Done", color = CoralDeep) }
                    }
                }
            }
        }
    }
}

@Composable
private fun TopBar(flashOn: Boolean, flashAvailable: Boolean, onFlash: () -> Unit, onSettings: () -> Unit, activeTab: String) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AuraLogo(Modifier.size(34.dp))
            Spacer(Modifier.width(9.dp))
            Text("NYXAI-GLOW", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(if (activeTab == "Retouch") "Retouch Looks" else "Camera", color = Color.White.copy(alpha = .82f), fontSize = 11.sp)
            IconButton(onClick = onFlash, enabled = flashAvailable) { Icon(Icons.Default.FlashOn, "Toggle flash", tint = if (flashOn) Coral else Color.White.copy(alpha = if (flashAvailable) .75f else .3f)) }
            IconButton(onClick = onSettings) { Icon(Icons.Default.Settings, "Camera settings", tint = Color.White.copy(alpha = .75f)) }
        }
    }
}

@Composable
private fun CameraOverlay(glow: Float, ambient: Float, preserveTexture: Boolean, reticleVisible: Boolean, onTextureToggle: () -> Unit) {
    Box(Modifier.fillMaxWidth().height(350.dp)) {
        Surface(color = Ink.copy(alpha = .68f), shape = RoundedCornerShape(50), modifier = Modifier.align(Alignment.TopCenter).padding(top = 10.dp)) {
            Row(Modifier.padding(horizontal = 13.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(8.dp).clip(CircleShape).background(Coral))
                Spacer(Modifier.width(7.dp))
                Text("AI ACTIVE", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Medium, letterSpacing = 1.sp)
                Spacer(Modifier.width(8.dp))
                Text("4K RAW", color = Coral, fontSize = 10.sp, fontWeight = FontWeight.Medium)
            }
        }
        if (reticleVisible) {
            GlowReticle(Modifier.align(Alignment.Center), glow, ambient)
        }
        Surface(color = Ink.copy(alpha = .72f), shape = RoundedCornerShape(50), modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 18.dp)) {
            Row(
                Modifier
                    .clickable(onClick = onTextureToggle)
                    .semantics {
                        role = Role.Switch
                        stateDescription = if (preserveTexture) "On" else "Off"
                    }
                    .padding(horizontal = 12.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.AutoAwesome, null, tint = Coral, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(6.dp))
                Text("Preserve natural texture", color = Color.White, fontSize = 12.sp)
                Spacer(Modifier.width(7.dp))
                Text(if (preserveTexture) "On" else "Off", color = Coral, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun GlowReticle(modifier: Modifier, progress: Float, ambient: Float) {
    Box(modifier.size(150.dp), contentAlignment = Alignment.Center) {
        Box(Modifier.size(150.dp).clip(CircleShape).background(Brush.radialGradient(listOf(Coral.copy(alpha = .2f), Color.Transparent))))
        Canvas(Modifier.size(136.dp)) {
            drawArc(Brush.sweepGradient(listOf(Coral, Coral.copy(alpha = .15f), Coral)), -90f, 360f * progress, false, style = Stroke(7.dp.toPx(), cap = StrokeCap.Round))
        }
        Canvas(Modifier.size(116.dp)) {
            drawCircle(Coral.copy(alpha = .82f), style = Stroke(2.dp.toPx()))
            drawCircle(Coral.copy(alpha = .9f), radius = size.minDimension / 2f - 10.dp.toPx(), style = Stroke(2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(8.dp.toPx(), 12.dp.toPx()))))
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            AuraLogo(Modifier.size(30.dp))
            Spacer(Modifier.height(3.dp))
            Text("NEURAL FOCUS", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(lightingState(ambient), color = Color.White.copy(alpha = .82f), fontSize = 12.sp)
        }
    }
}

@Composable
private fun CameraDeck(preset: String, zoom: String, onPreset: (String) -> Unit, onZoom: (String) -> Unit, onFlip: () -> Unit, onGallery: () -> Unit, onCapture: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            items(listOf("Soft", "Radiant", "Velvet", "Defined")) { item ->
                val selected = item == preset
                Button(onClick = { onPreset(item) }, shape = RoundedCornerShape(50), colors = ButtonDefaults.buttonColors(containerColor = if (selected) Coral else Ink.copy(alpha = .44f), contentColor = if (selected) CoralDeep else Color.White), contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 15.dp, vertical = 0.dp), modifier = Modifier.height(38.dp).semantics {
                    role = Role.Button
                    stateDescription = if (selected) "Selected" else "Not selected"
                }) {
                    if (selected) { Icon(Icons.Default.AutoAwesome, null, modifier = Modifier.size(14.dp)); Spacer(Modifier.width(5.dp)) }
                    Text(item, fontSize = 12.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
            listOf("0.5x", "1x", "2x", "3x").forEach { item ->
                Text(item, color = if (item == zoom) CoralDeep else Color.White.copy(alpha = .82f), fontSize = 12.sp, fontWeight = if (item == zoom) FontWeight.Bold else FontWeight.Normal, modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(if (item == zoom) Coral else Ink.copy(alpha = .4f)).clickable { onZoom(item) }.padding(horizontal = 8.dp, vertical = 5.dp).semantics {
                    role = Role.Button
                    stateDescription = if (item == zoom) "Selected" else "Not selected"
                })
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onGallery) { Icon(Icons.Default.PhotoLibrary, "Open gallery", tint = Color.White.copy(alpha = .86f), modifier = Modifier.size(26.dp)) }
            Box(contentAlignment = Alignment.Center) {
                Box(Modifier.size(84.dp).clip(CircleShape).background(Coral.copy(alpha = .22f)))
                Surface(color = Color.White.copy(alpha = .94f), shape = CircleShape, modifier = Modifier.size(70.dp)) { IconButton(onClick = onCapture) { Icon(Icons.Default.Camera, "Capture photo", tint = CoralDeep, modifier = Modifier.size(30.dp)) } }
            }
            IconButton(onClick = onFlip) { Icon(Icons.Default.FlipCameraAndroid, "Flip camera", tint = Color.White.copy(alpha = .86f), modifier = Modifier.size(27.dp)) }
        }
        Spacer(Modifier.height(13.dp))
    }
}

@Composable
private fun RetouchDashboard(preserveTexture: Boolean, onTextureToggle: () -> Unit, onApply: () -> Unit) {
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
                    GlowReticle(Modifier.size(172.dp), .72f, .62f)
                    Text("98.4% NATURAL MATCH", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.align(Alignment.TopCenter).padding(top = 18.dp))
                }
            }
            Spacer(Modifier.height(18.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                RetouchTool("Skin", Icons.Default.AutoAwesome, true)
                RetouchTool("Shape", Icons.Default.PhotoLibrary, false)
                RetouchTool("Light", Icons.Default.FlashOn, false)
                RetouchTool("Makeup", Icons.Default.Palette, false)
            }
            Spacer(Modifier.height(18.dp))
            Text("PRESETS & TONE", color = Color.White.copy(alpha = .64f), fontSize = 10.sp, letterSpacing = 1.sp)
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(listOf("Smooth", "Freckles", "Matte", "Dewy", "Refine")) { item ->
                    Surface(color = if (item == "Smooth") Coral else SurfaceRaised, shape = RoundedCornerShape(10.dp)) {
                        Column(Modifier.width(58.dp).padding(7.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(Modifier.size(44.dp).clip(RoundedCornerShape(8.dp)).background(if (item == "Smooth") CoralDeep else Color(0xFF3B3331)))
                            Spacer(Modifier.height(5.dp))
                            Text(item, color = if (item == "Smooth") CoralDeep else Color.White, fontSize = 9.sp)
                        }
                    }
                }
            }
            Spacer(Modifier.height(18.dp))
            Text("Smoothing intensity", color = Color.White, fontSize = 12.sp)
            Slider(value = if (preserveTexture) .45f else .72f, onValueChange = {}, valueRange = 0f..1f, colors = androidx.compose.material3.SliderDefaults.colors(thumbColor = Coral, activeTrackColor = Coral, inactiveTrackColor = SurfaceRaised))
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
            Button(onClick = {}, colors = ButtonDefaults.buttonColors(containerColor = SurfaceRaised, contentColor = Color.White), modifier = Modifier.weight(1f)) { Text("RESET", fontSize = 11.sp) }
            Button(onClick = onApply, colors = ButtonDefaults.buttonColors(containerColor = Coral, contentColor = CoralDeep), modifier = Modifier.weight(2f)) { Text("APPLY & SAVE", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
private fun RetouchTool(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, selected: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(color = if (selected) Coral.copy(alpha = .2f) else SurfaceRaised, shape = CircleShape, modifier = Modifier.size(42.dp)) {
            Icon(icon, label, tint = if (selected) Coral else Color.White.copy(alpha = .72f), modifier = Modifier.padding(12.dp))
        }
        Spacer(Modifier.height(5.dp))
        Text(label, color = if (selected) Coral else Color.White.copy(alpha = .7f), fontSize = 10.sp)
    }
}

@Composable
private fun BottomNavigation(active: String, onChange: (String) -> Unit) {
    Surface(color = SurfaceDark.copy(alpha = .98f), shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp), modifier = Modifier.fillMaxWidth().navigationBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceAround, verticalAlignment = Alignment.CenterVertically) {
            NavItem("Gallery", Icons.Default.PhotoLibrary, active, onChange)
            NavItem("Looks", Icons.Default.AutoAwesome, active, onChange)
            Surface(color = Coral, shape = CircleShape, modifier = Modifier.size(54.dp)) { IconButton(onClick = { onChange("Camera") }) { Icon(Icons.Default.Camera, "Camera", tint = CoralDeep, modifier = Modifier.size(25.dp)) } }
            NavItem("Profile", Icons.Default.Person, active, onChange)
        }
    }
}

@Composable
private fun NavItem(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, active: String, onChange: (String) -> Unit) {
    val selected = label == active
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onChange(label) }.padding(horizontal = 7.dp, vertical = 3.dp)) {
        Icon(icon, label, tint = if (selected) Coral else Color.White.copy(alpha = .58f), modifier = Modifier.size(21.dp))
        Text(label, color = if (selected) Coral else Color.White.copy(alpha = .58f), fontSize = 10.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
    }
}

@Composable
private fun AuraLogo(modifier: Modifier) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Box(Modifier.fillMaxSize().clip(CircleShape).border(3.dp, Coral, CircleShape))
        Box(Modifier.fillMaxSize(.7f).clip(CircleShape).border(1.5.dp, Coral, CircleShape))
        Box(Modifier.size(12.dp).clip(CircleShape).background(Brush.linearGradient(listOf(Coral, CoralDeep))))
    }
}

@Composable
private fun PermissionPrompt(onRequest: () -> Unit) {
    Box(Modifier.fillMaxSize().background(Mist), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            AuraLogo(Modifier.size(72.dp))
            Spacer(Modifier.height(20.dp))
            Text("Camera access brings the glow to life", color = Ink, fontSize = 21.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Text("Nyxai Glow needs your camera for the live beauty preview.", color = Color(0xFF595F65), fontSize = 14.sp)
            Spacer(Modifier.height(22.dp))
            Button(onClick = onRequest, colors = ButtonDefaults.buttonColors(containerColor = Coral, contentColor = CoralDeep)) { Text("Enable camera") }
        }
    }
}

@Composable
private fun CameraPreview(
    modifier: Modifier,
    lensFacing: Int,
    zoom: String,
    flashOn: Boolean,
    captureRequest: Int,
    onAmbient: (Float) -> Unit,
    onLandmarksDetected: (FaceLandmarkerResult) -> Unit,
    onCapture: (Uri) -> Unit,
    onCameraError: (String) -> Unit,
    onFlashAvailabilityChanged: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val executor = remember { Executors.newSingleThreadExecutor() }
    val previewView = remember { PreviewView(context) }
    val imageCapture = remember { ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).build() }
    var camera by remember { mutableStateOf<Camera?>(null) }
    DisposableEffect(lensFacing, lifecycleOwner) {
        val disposed = AtomicBoolean(false)
        var localAnalyzer: FaceLandmarkAnalyzer? = null
        var localProvider: ProcessCameraProvider? = null
        val future = ProcessCameraProvider.getInstance(context)
        future.addListener({
            if (disposed.get()) return@addListener
            val provider = try {
                future.get()
            } catch (exception: Exception) {
                onCameraError("Camera initialization failed: ${exception.message ?: exception.javaClass.simpleName}")
                return@addListener
            }
            localProvider = provider
            val preview = Preview.Builder().build().also { it.surfaceProvider = previewView.surfaceProvider }
            val analyzer = FaceLandmarkAnalyzer(
                context = context,
                onLandmarksDetected = onLandmarksDetected,
                onLightChanged = onAmbient,
                onError = onCameraError
            )
            localAnalyzer = analyzer
            if (disposed.get()) {
                analyzer.close()
                localAnalyzer = null
                return@addListener
            }
            val analysis = ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build().also { it.setAnalyzer(executor, analyzer) }
            val selector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
            provider.unbindAll()
            camera = try {
                provider.bindToLifecycle(lifecycleOwner, selector, preview, imageCapture, analysis).also {
                    onFlashAvailabilityChanged(it.cameraInfo.hasFlashUnit())
                }
            } catch (exception: Exception) {
                onCameraError("Camera unavailable: ${exception.javaClass.simpleName}")
                analyzer.close()
                localAnalyzer = null
                null
            }
        }, ContextCompat.getMainExecutor(context))
        onDispose {
            disposed.set(true)
            camera = null
            onFlashAvailabilityChanged(false)
            localProvider?.unbindAll()
            localAnalyzer?.close()
            localAnalyzer = null
        }
    }

    DisposableEffect(Unit) {
        onDispose { executor.shutdown() }
    }

    LaunchedEffect(camera, zoom, flashOn) {
        val currentCamera = camera ?: return@LaunchedEffect
        val requestedZoom = zoom.removeSuffix("x").toFloatOrNull() ?: 1f
        val zoomState = currentCamera.cameraInfo.zoomState.value
        if (zoomState != null) {
            currentCamera.cameraControl.setZoomRatio(requestedZoom.coerceIn(zoomState.minZoomRatio, zoomState.maxZoomRatio))
        }
        if (currentCamera.cameraInfo.hasFlashUnit()) {
            currentCamera.cameraControl.enableTorch(flashOn)
        }
    }

    LaunchedEffect(captureRequest) {
        if (captureRequest == 0) return@LaunchedEffect
        if (executor.isShutdown) {
            onCameraError("Camera session ended")
            return@LaunchedEffect
        }
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "NyxaiGlow_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Nyxai Glow")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        ) {
            onCameraError("Storage permission is required to save photos")
            return@LaunchedEffect
        }
        val outputUri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        if (outputUri == null) {
            onCameraError("Could not prepare photo storage")
            return@LaunchedEffect
        }
        val metadata = ImageCapture.Metadata().apply {
            isReversedHorizontal = lensFacing == CameraSelector.LENS_FACING_FRONT
        }
        val output = ImageCapture.OutputFileOptions.Builder(context.contentResolver, outputUri, values)
            .setMetadata(metadata)
            .build()
        try {
            imageCapture.takePicture(output, executor, object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(result: ImageCapture.OutputFileResults) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        context.contentResolver.update(outputUri, ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) }, null, null)
                    }
                    onCapture(outputUri)
                }

                override fun onError(exception: ImageCaptureException) {
                    context.contentResolver.delete(outputUri, null, null)
                    onCameraError("Capture failed")
                }
            })
        } catch (_: RejectedExecutionException) {
            context.contentResolver.delete(outputUri, null, null)
            onCameraError("Camera session ended")
        }
    }

    AndroidView(factory = { previewView }, modifier = modifier)
}
