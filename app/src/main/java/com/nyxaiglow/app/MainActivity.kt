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
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.nyxaiglow.app.camera.AmbientLightAnalyzer
import com.nyxaiglow.app.ui.theme.NyxaiGlowTheme
import java.util.concurrent.Executors

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { NyxaiGlowTheme { NyxaiGlowApp() } }
    }
}

@Composable
private fun NyxaiGlowApp() {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        hasCameraPermission = it
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    if (hasCameraPermission) {
        GlowStudio()
    } else {
        PermissionPrompt(onRequest = { permissionLauncher.launch(Manifest.permission.CAMERA) })
    }
}

@Composable
private fun GlowStudio() {
    var glow by remember { mutableFloatStateOf(0.62f) }
    var smooth by remember { mutableFloatStateOf(0.38f) }
    var preserveTexture by remember { mutableStateOf(true) }
    var ambientLight by remember { mutableFloatStateOf(0.58f) }
    var cameraFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_FRONT) }
    var selectedMode by remember { mutableStateOf("Live") }

    Box(Modifier.fillMaxSize().background(Color(0xFF0D1110))) {
        CameraPreview(
            modifier = Modifier.fillMaxSize(),
            lensFacing = cameraFacing,
            onAmbientLightChanged = { ambientLight = it }
        )
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    listOf(Color.Black.copy(alpha = 0.52f), Color.Transparent, Color.Black.copy(alpha = 0.85f))
                )
            )
        )
        Column(
            Modifier.fillMaxSize().padding(WindowInsets.navigationBars.asPaddingValues()),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            TopBar(ambientLight = ambientLight)
            Controls(
                glow = glow,
                smooth = smooth,
                preserveTexture = preserveTexture,
                selectedMode = selectedMode,
                onGlowChange = { glow = it },
                onSmoothChange = { smooth = it },
                onPreserveTextureChange = { preserveTexture = it },
                onModeChange = { selectedMode = it },
                onFlipCamera = {
                    cameraFacing = if (cameraFacing == CameraSelector.LENS_FACING_FRONT) CameraSelector.LENS_FACING_BACK else CameraSelector.LENS_FACING_FRONT
                }
            )
        }
    }
}

@Composable
private fun TopBar(ambientLight: Float) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text("NYXAI GLOW", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            Text("Live beauty studio", color = Color.White.copy(alpha = 0.68f), fontSize = 13.sp)
        }
        Surface(color = Color.Black.copy(alpha = 0.34f), shape = RoundedCornerShape(50)) {
            Row(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LightMode, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("${(ambientLight * 100).toInt()}% light", color = Color.White, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun Controls(
    glow: Float,
    smooth: Float,
    preserveTexture: Boolean,
    selectedMode: String,
    onGlowChange: (Float) -> Unit,
    onSmoothChange: (Float) -> Unit,
    onPreserveTextureChange: (Boolean) -> Unit,
    onModeChange: (String) -> Unit,
    onFlipCamera: () -> Unit
) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)).background(Color(0xFF111715).copy(alpha = 0.96f)).padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Your look, in real time", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            IconButton(onClick = onFlipCamera) { Icon(Icons.Default.FlipCameraAndroid, "Flip camera", tint = Color.White) }
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(listOf("Live", "Photo", "Portrait")) { mode ->
                val selected = mode == selectedMode
                Button(
                    onClick = { onModeChange(mode) },
                    colors = ButtonDefaults.buttonColors(containerColor = if (selected) MaterialTheme.colorScheme.primary else Color(0xFF26332E), contentColor = if (selected) Color(0xFF2D1900) else Color.White),
                    shape = RoundedCornerShape(50),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 18.dp, vertical = 0.dp),
                    modifier = Modifier.height(36.dp)
                ) { Text(mode, fontSize = 12.sp, fontWeight = FontWeight.Medium) }
            }
        }
        Spacer(Modifier.height(16.dp))
        Adjuster("Adaptive glow", "Responds to the light around you", glow, Icons.Default.AutoAwesome, onGlowChange)
        Adjuster("Texture preserve", "Keep natural detail, moles and freckles", smooth, Icons.Default.Tune, onSmoothChange)
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Lock, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text("Preserve texture", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Text("Your details stay yours", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            }
            Switch(checked = preserveTexture, onCheckedChange = onPreserveTextureChange)
        }
        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { }) { Icon(Icons.Default.ChevronLeft, "Previous", tint = Color.White.copy(alpha = 0.7f)) }
            Surface(color = MaterialTheme.colorScheme.primary, shape = CircleShape, modifier = Modifier.size(68.dp)) {
                IconButton(onClick = { }) { Icon(Icons.Default.Camera, "Capture", tint = Color(0xFF2D1900), modifier = Modifier.size(28.dp)) }
            }
            IconButton(onClick = { }) { Icon(Icons.Default.Tune, "Settings", tint = Color.White.copy(alpha = 0.7f)) }
        }
    }
}

@Composable
private fun Adjuster(label: String, description: String, value: Float, icon: androidx.compose.ui.graphics.vector.ImageVector, onValueChange: (Float) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(19.dp))
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(label, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Text("${(value * 100).toInt()}%", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
            }
            Text(description, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            Slider(value = value, onValueChange = onValueChange, valueRange = 0f..1f)
        }
    }
}

@Composable
private fun PermissionPrompt(onRequest: () -> Unit) {
    Box(Modifier.fillMaxSize().background(Color(0xFF0D1110)), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Icon(Icons.Default.Camera, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
            Spacer(Modifier.height(18.dp))
            Text("Camera access brings the glow to life", color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Text("Nyxai Glow needs your camera for the live beauty preview.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
            Spacer(Modifier.height(22.dp))
            Button(onClick = onRequest) { Text("Enable camera") }
        }
    }
}

@Composable
private fun CameraPreview(modifier: Modifier, lensFacing: Int, onAmbientLightChanged: (Float) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val executor = remember { Executors.newSingleThreadExecutor() }
    val previewView = remember { PreviewView(context) }

    DisposableEffect(lensFacing) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val provider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also { it.surfaceProvider = previewView.surfaceProvider }
            val analysis = ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build().also {
                it.setAnalyzer(executor, AmbientLightAnalyzer(onAmbientLightChanged))
            }
            provider.unbindAll()
            provider.bindToLifecycle(lifecycleOwner, CameraSelector.Builder().requireLensFacing(lensFacing).build(), preview, analysis)
        }, ContextCompat.getMainExecutor(context))
        onDispose { executor.shutdown() }
    }

    AndroidView(factory = { previewView }, modifier = modifier)
}
