package pk.livecaster.app.settings.presentation

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LiveRed
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCard
import com.example.ui.theme.StudioCyan
import com.example.ui.theme.StudioDark
import com.example.ui.theme.StudioGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import pk.livecaster.app.core.constants.AppConstants

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsState()
    val message by viewModel.saveMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = StudioDark,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(StudioCyan.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = StudioCyan,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Encoder & Studio Preferences",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Text(
                        text = "Fine-tune hardware codec, bitrates, and localization",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )
                }
            }

            // Video Profile Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = StudioCard),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Tune, contentDescription = null, tint = StudioCyan, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Video Ingest Profile",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("Target Resolution", style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary))
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("1080p", "720p", "480p").forEach { res ->
                            val isSel = settings.resolution == res
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSel) LiveRed.copy(alpha = 0.2f) else StudioDark)
                                    .border(1.dp, if (isSel) LiveRed else StudioBorder, RoundedCornerShape(10.dp))
                                    .clickable { viewModel.updateResolution(res) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = res,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSel) LiveRed else TextPrimary
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Framerate: ${settings.fps} FPS", style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary))
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(60, 30, 24).forEach { fps ->
                            val isSel = settings.fps == fps
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSel) StudioCyan.copy(alpha = 0.2f) else StudioDark)
                                    .border(1.dp, if (isSel) StudioCyan else StudioBorder, RoundedCornerShape(10.dp))
                                    .clickable { viewModel.updateFps(fps) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$fps FPS",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSel) StudioCyan else TextPrimary
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Video Bitrate", style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary))
                        Text("${settings.videoBitrateKbps} kbps", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = StudioGreen))
                    }
                    Slider(
                        value = settings.videoBitrateKbps.toFloat(),
                        onValueChange = { viewModel.updateVideoBitrate(it.toInt()) },
                        valueRange = 1000f..8000f,
                        steps = 13,
                        colors = SliderDefaults.colors(
                            thumbColor = StudioGreen,
                            activeTrackColor = StudioGreen
                        ),
                        modifier = Modifier.testTag("video_bitrate_slider")
                    )
                }
            }

            // Hardware & Network Controls
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = StudioCard),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Memory, contentDescription = null, tint = StudioGreen, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Hardware & Adaptive Ingest",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Adaptive Bitrate (ABR)", style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary))
                            Text("Automatically downscale bitrate on network congestion", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted))
                        }
                        Switch(
                            checked = settings.isAdaptiveBitrateEnabled,
                            onCheckedChange = { viewModel.toggleAdaptiveBitrate(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = StudioGreen, checkedTrackColor = StudioGreen.copy(alpha = 0.4f))
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Hardware H.264 Encoder", style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary))
                            Text("Use device GPU MediaCodec for battery saving and high FPS", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted))
                        }
                        Switch(
                            checked = settings.isHardwareEncoderEnabled,
                            onCheckedChange = { viewModel.toggleHardwareEncoder(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = StudioCyan, checkedTrackColor = StudioCyan.copy(alpha = 0.4f))
                        )
                    }
                }
            }

            // Language & Studio Localization
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = StudioCard),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Language, contentDescription = null, tint = LiveRed, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Language / زبان",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (settings.language == "en") LiveRed.copy(alpha = 0.2f) else StudioDark)
                                .border(1.dp, if (settings.language == "en") LiveRed else StudioBorder, RoundedCornerShape(10.dp))
                                .clickable { viewModel.updateLanguage("en") }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "English (US)",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (settings.language == "en") FontWeight.Bold else FontWeight.Normal,
                                    color = if (settings.language == "en") LiveRed else TextPrimary
                                )
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (settings.language == "ur") LiveRed.copy(alpha = 0.2f) else StudioDark)
                                .border(1.dp, if (settings.language == "ur") LiveRed else StudioBorder, RoundedCornerShape(10.dp))
                                .clickable { viewModel.updateLanguage("ur") }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "اردو (Urdu)",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (settings.language == "ur") FontWeight.Bold else FontWeight.Normal,
                                    color = if (settings.language == "ur") LiveRed else TextPrimary
                                )
                            )
                        }
                    }
                }
            }

            // Save Settings Action Button
            Button(
                onClick = { viewModel.saveSettings() },
                colors = ButtonDefaults.buttonColors(containerColor = LiveRed),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_settings_button")
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Save Studio Configuration",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
            }
        }
    }
}
