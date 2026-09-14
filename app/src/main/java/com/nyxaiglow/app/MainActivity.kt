package com.nyxaiglow.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.nyxaiglow.app.camera.AmbientLightAnalyzer
import com.nyxaiglow.app.ui.theme.NyxaiGlowTheme
import java.util.concurrent.Executors
import kotlinx.coroutines.delay

private val Coral = Color(0xFFFF9A8B)
private val CoralDeep = Color(0xFF96463B)
private val Mist = Color(0xFFFAF8FF)
private val Ink = Color(0xFF171B2B)

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
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { cameraGranted = it }
    LaunchedEffect(Unit) { if (!cameraGranted) permissionLauncher.launch(Manifest.permission.CAMERA) }
    if (cameraGranted) GlowStudio() else PermissionPrompt { permissionLauncher.launch(Manifest.permission.CAMERA) }
}

@Composable
private fun GlowStudio() {
    var glow by remember { mutableFloatStateOf(0.68f) }
    var ambient by remember { mutableFloatStateOf(0.58f) }
    var preset by remember { mutableStateOf("Natural") }
    var zoom by remember { mutableStateOf("1x") }
    var facing by remember { mutableStateOf(CameraSelector.LENS_FACING_FRONT) }
    var flashOn by remember { mutableStateOf(false) }
    var activeTab by remember { mutableStateOf("Camera") }
    var preserveTexture by remember { mutableStateOf(true) }
    var reticleVisible by remember { mutableStateOf(true) }

    LaunchedEffect(preset, zoom, flashOn, facing) {
        reticleVisible = true
        delay(2_000)
        reticleVisible = false
    }

    Box(Modifier.fillMaxSize().background(Mist)) {
        CameraPreview(Modifier.fillMaxSize(), facing) { ambient = it }
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Ink.copy(alpha = .68f), Color.Transparent, Ink.copy(alpha = .88f)))))
        Column(Modifier.fillMaxSize().padding(WindowInsets.navigationBars.asPaddingValues()), verticalArrangement = Arrangement.SpaceBetween) {
            TopBar(flashOn, { flashOn = !flashOn })
            Column(Modifier.fillMaxWidth()) {
                CameraOverlay(glow, ambient, preserveTexture, reticleVisible) { preserveTexture = !preserveTexture }
                CameraDeck(preset, zoom, { chosen ->
                    preset = chosen
                    glow = when (chosen) { "Radiant" -> .86f; "Velvet" -> .34f; "Defined" -> .57f; else -> .68f }
                }, { zoom = it }) { facing = if (facing == CameraSelector.LENS_FACING_FRONT) CameraSelector.LENS_FACING_BACK else CameraSelector.LENS_FACING_FRONT }
                BottomNavigation(activeTab) { activeTab = it }
            }
        }
    }
}

@Composable
private fun TopBar(flashOn: Boolean, onFlash: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AuraLogo(Modifier.size(34.dp))
            Spacer(Modifier.width(9.dp))
            Text("Nyxai Glow", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Camera", color = Color.White.copy(alpha = .72f), fontSize = 12.sp)
            IconButton(onClick = onFlash) { Icon(Icons.Default.FlashOn, "Toggle flash", tint = if (flashOn) Coral else Color.White.copy(alpha = .75f)) }
            IconButton(onClick = { }) { Icon(Icons.Default.Settings, "Camera settings", tint = Color.White.copy(alpha = .75f)) }
        }
    }
}

@Composable
private fun CameraOverlay(glow: Float, ambient: Float, preserveTexture: Boolean, reticleVisible: Boolean, onTextureToggle: () -> Unit) {
    Box(Modifier.fillMaxWidth().height(350.dp)) {
        Surface(color = Ink.copy(alpha = .44f), shape = RoundedCornerShape(50), modifier = Modifier.align(Alignment.TopCenter).padding(top = 10.dp)) {
            Row(Modifier.padding(horizontal = 13.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(8.dp).clip(CircleShape).background(Coral))
                Spacer(Modifier.width(7.dp))
                Text("Glow Engine", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.width(8.dp))
                Text("Ready", color = Color.White.copy(alpha = .72f), fontSize = 12.sp)
            }
        }
        androidx.compose.animation.AnimatedVisibility(
            visible = reticleVisible,
            modifier = Modifier.align(Alignment.Center)
        ) {
            GlowReticle(Modifier, glow, ambient)
        }
        Surface(color = Ink.copy(alpha = .52f), shape = RoundedCornerShape(50), modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 18.dp)) {
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

private fun lightingState(ambient: Float): String = when {
    ambient < .2f -> "Low light - glow boosted"
    ambient < .6f -> "Balanced light"
    ambient < .85f -> "Bright - highlights softened"
    else -> "Harsh light - smoothing adjusted"
}

@Composable
private fun CameraDeck(preset: String, zoom: String, onPreset: (String) -> Unit, onZoom: (String) -> Unit, onFlip: () -> Unit) {
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
            IconButton(onClick = { }) { Icon(Icons.Default.PhotoLibrary, "Open gallery", tint = Color.White.copy(alpha = .86f), modifier = Modifier.size(26.dp)) }
            Box(contentAlignment = Alignment.Center) {
                Box(Modifier.size(84.dp).clip(CircleShape).background(Coral.copy(alpha = .22f)))
                Surface(color = Color.White.copy(alpha = .94f), shape = CircleShape, modifier = Modifier.size(70.dp)) { IconButton(onClick = { }) { Icon(Icons.Default.Camera, "Capture photo", tint = CoralDeep, modifier = Modifier.size(30.dp)) } }
            }
            IconButton(onClick = onFlip) { Icon(Icons.Default.FlipCameraAndroid, "Flip camera", tint = Color.White.copy(alpha = .86f), modifier = Modifier.size(27.dp)) }
        }
        Spacer(Modifier.height(13.dp))
    }
}

@Composable
private fun BottomNavigation(active: String, onChange: (String) -> Unit) {
    Surface(color = Mist.copy(alpha = .95f), shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp), modifier = Modifier.fillMaxWidth()) {
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
        Icon(icon, label, tint = if (selected) CoralDeep else Color(0xFF595F65), modifier = Modifier.size(21.dp))
        Text(label, color = if (selected) CoralDeep else Color(0xFF595F65), fontSize = 10.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
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
private fun CameraPreview(modifier: Modifier, lensFacing: Int, onAmbient: (Float) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val executor = remember { Executors.newSingleThreadExecutor() }
    val previewView = remember { PreviewView(context) }
    DisposableEffect(lensFacing) {
        val future = ProcessCameraProvider.getInstance(context)
        future.addListener({
            val provider = future.get()
            val preview = Preview.Builder().build().also { it.surfaceProvider = previewView.surfaceProvider }
            val analysis = ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build().also { it.setAnalyzer(executor, AmbientLightAnalyzer(onAmbient)) }
            provider.unbindAll()
            provider.bindToLifecycle(lifecycleOwner, CameraSelector.Builder().requireLensFacing(lensFacing).build(), preview, analysis)
        }, ContextCompat.getMainExecutor(context))
        onDispose { executor.shutdown() }
    }
    AndroidView(factory = { previewView }, modifier = modifier)
}
