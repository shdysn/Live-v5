package pk.livecaster.app.broadcast.presentation.control

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.ui.theme.LiveRed
import com.example.ui.theme.StudioAmber
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCard
import com.example.ui.theme.StudioCyan
import com.example.ui.theme.StudioDark
import com.example.ui.theme.StudioGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import pk.livecaster.app.core.util.Formatters
import pk.livecaster.app.streaming.capture.CameraCaptureManager
import pk.livecaster.app.streaming.state.StreamHealth
import pk.livecaster.app.streaming.state.StreamStatus

@Composable
fun BroadcastControlScreen(
    viewModel: BroadcastControlViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraManager = remember { CameraCaptureManager(context) }
    var previewViewRef by remember { androidx.compose.runtime.mutableStateOf<PreviewView?>(null) }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        if (granted) {
            previewViewRef?.let { cameraManager.bindCamera(lifecycleOwner, it) }
        }
    }

    LaunchedEffect(Unit) {
        cameraManager.onYuvFrameAvailable = { yuvBytes ->
            viewModel.feedVideoFrame(yuvBytes)
        }
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    DisposableEffect(lifecycleOwner) {
        onDispose {
            cameraManager.release()
        }
    }

    val isLive = uiState.telemetry.status == StreamStatus.LIVE

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(StudioDark)
    ) {
        // 1. Camera Viewport
        if (hasCameraPermission) {
            AndroidView(
                factory = { ctx ->
                    PreviewView(ctx).apply {
                        implementationMode = PreviewView.ImplementationMode.PERFORMANCE
                        cameraManager.bindCamera(lifecycleOwner, this)
                        previewViewRef = this
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("camera_preview_view")
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(StudioDark),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Videocam,
                        contentDescription = null,
                        tint = LiveRed,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Camera Permission Required",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Please allow camera access to display your live video feed.",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Button(
                        onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                        colors = ButtonDefaults.buttonColors(containerColor = LiveRed)
                    ) {
                        Text("Grant Camera Permission", color = TextPrimary)
                    }
                }
            }
        }

        // Lightweight vignette for HUD readability without dimming video
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            StudioDark.copy(alpha = 0.5f),
                            Color.Transparent,
                            Color.Transparent,
                            StudioDark.copy(alpha = 0.6f)
                        )
                    )
                )
        )

        // 2. Top HUD Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(top = 16.dp, start = 16.dp, end = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back Button
                IconButton(
                    onClick = {
                        if (isLive) {
                            viewModel.promptEndConfirmation(true)
                        } else {
                            onNavigateBack()
                        }
                    },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(StudioCard.copy(alpha = 0.8f))
                        .testTag("control_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }

                // Live Status Badge & Duration
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(StudioCard.copy(alpha = 0.85f))
                        .border(1.dp, StudioBorder, RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isLive) LiveRed else TextMuted)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isLive) "LIVE" else uiState.telemetry.status.name,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isLive) LiveRed else TextSecondary
                        )
                    )
                    if (isLive) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = Formatters.formatDuration(uiState.telemetry.durationSeconds),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        )
                    }
                }

                // Viewers Chip
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(StudioCard.copy(alpha = 0.85f))
                        .border(1.dp, StudioBorder, RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = null,
                        tint = StudioCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = Formatters.formatViewers(uiState.telemetry.currentViewers),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Encoder Telemetry Pills: Bitrate, FPS, Health
            if (isLive) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TelemetryPill(
                        label = "Bitrate",
                        value = Formatters.formatBitrate(uiState.telemetry.currentBitrateKbps),
                        color = StudioCyan,
                        modifier = Modifier.weight(1f)
                    )
                    TelemetryPill(
                        label = "FPS",
                        value = "${uiState.telemetry.currentFps} fps",
                        color = StudioGreen,
                        modifier = Modifier.weight(1f)
                    )
                    TelemetryPill(
                        label = "Health",
                        value = uiState.telemetry.health.name,
                        color = when (uiState.telemetry.health) {
                            StreamHealth.EXCELLENT -> StudioGreen
                            StreamHealth.GOOD -> StudioCyan
                            StreamHealth.POOR -> StudioAmber
                            StreamHealth.CRITICAL -> LiveRed
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 3. Lower Third Graphic Overlay (Studio Branding)
        AnimatedVisibility(
            visible = uiState.isLowerThirdVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = 150.dp, start = 16.dp, end = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(StudioCard.copy(alpha = 0.85f))
                    .border(1.5.dp, LiveRed.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(LiveRed)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = uiState.broadcast?.title ?: "Live Broadcast",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            ),
                            maxLines = 1
                        )
                        val platformLabel = when (uiState.broadcast?.platform) {
                            pk.livecaster.app.broadcast.domain.model.PlatformType.MULTI_DESTINATION -> "Simulcast • FACEBOOK + YOUTUBE"
                            null -> "RTMP"
                            else -> uiState.broadcast?.platform?.name ?: "RTMP"
                        }
                        Text(
                            text = "LiveCaster Ingest • $platformLabel",
                            style = MaterialTheme.typography.labelSmall.copy(color = StudioCyan)
                        )
                    }
                }
            }
        }

        // 4. Status Error Banner if stream disconnected
        if (uiState.telemetry.status == StreamStatus.ERROR && uiState.telemetry.errorMessage != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 160.dp, start = 16.dp, end = 16.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(LiveRed.copy(alpha = 0.9f))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "⚠️ ${uiState.telemetry.errorMessage}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
            }
        }

        // 5. Audio VU Meter Bar
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 100.dp, start = 16.dp, end = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = "Audio Level",
                    tint = if (uiState.telemetry.isMicMuted) TextMuted else StudioGreen,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (uiState.telemetry.isMicMuted) "Mic Muted" else "Audio Feed (Stereo AAC)",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (uiState.telemetry.isMicMuted) TextMuted else StudioGreen
                    )
                )
            }
            LinearProgressIndicator(
                progress = { if (uiState.telemetry.isMicMuted) 0f else uiState.audioVuLevel },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = when {
                    uiState.audioVuLevel > 0.85f -> LiveRed
                    uiState.audioVuLevel > 0.65f -> StudioAmber
                    else -> StudioGreen
                },
                trackColor = StudioCard.copy(alpha = 0.7f)
            )
        }

        // 6. Bottom Studio Control Dock
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(StudioDark.copy(alpha = 0.95f))
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mic Mute Toggle
                StudioIconButton(
                    icon = if (uiState.telemetry.isMicMuted) Icons.Default.MicOff else Icons.Default.Mic,
                    isActive = !uiState.telemetry.isMicMuted,
                    onClick = { viewModel.toggleMic() },
                    contentDescription = "Toggle Mic"
                )

                // Camera Switch (Front/Back)
                StudioIconButton(
                    icon = Icons.Default.Cameraswitch,
                    isActive = uiState.telemetry.isFrontCamera,
                    onClick = {
                        val isFront = viewModel.toggleCamera()
                        previewViewRef?.let {
                            cameraManager.switchCamera(lifecycleOwner, it)
                        }
                    },
                    contentDescription = "Switch Camera"
                )

                // Main Live Action Button (Go Live / Stop)
                Button(
                    onClick = {
                        if (isLive) {
                            viewModel.promptEndConfirmation(true)
                        } else {
                            viewModel.startLiveStream()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isLive) LiveRed else StudioGreen
                    ),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .height(48.dp)
                        .padding(horizontal = 8.dp)
                        .testTag("stream_toggle_button")
                ) {
                    Icon(
                        imageVector = if (isLive) Icons.Default.Stop else Icons.Default.Videocam,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isLive) "End Stream" else "Start Live",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = StudioDark
                        )
                    )
                }

                // Flashlight / Torch
                StudioIconButton(
                    icon = if (uiState.telemetry.isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashlightOff,
                    isActive = uiState.telemetry.isTorchOn,
                    onClick = {
                        val isTorch = viewModel.toggleTorch()
                        cameraManager.toggleTorch(isTorch)
                    },
                    contentDescription = "Torch"
                )

                // Lower Third Toggle
                StudioIconButton(
                    icon = Icons.Default.Visibility,
                    isActive = uiState.isLowerThirdVisible,
                    onClick = { viewModel.toggleLowerThird() },
                    contentDescription = "Toggle Graphics"
                )
            }
        }
    }

    // End Stream Confirmation Dialog
    if (uiState.showEndConfirmDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.promptEndConfirmation(false) },
            title = { Text("End Broadcast?", color = TextPrimary) },
            text = {
                Text(
                    "Ending this broadcast will disconnect all active RTMP endpoints and stop live ingest. Viewers will be notified that the stream has finished.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.promptEndConfirmation(false)
                        viewModel.stopLiveStream()
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LiveRed)
                ) {
                    Text("End Broadcast", color = TextPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.promptEndConfirmation(false) }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = StudioCard,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun TelemetryPill(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(StudioCard.copy(alpha = 0.85f))
            .border(1.dp, StudioBorder, RoundedCornerShape(10.dp))
            .padding(vertical = 4.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = TextMuted)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = color)
            )
        }
    }
}

@Composable
fun StudioIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isActive: Boolean,
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(if (isActive) StudioCyan.copy(alpha = 0.2f) else StudioCard)
            .border(1.dp, if (isActive) StudioCyan else StudioBorder, CircleShape)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (isActive) StudioCyan else TextSecondary,
            modifier = Modifier.size(22.dp)
        )
    }
}
