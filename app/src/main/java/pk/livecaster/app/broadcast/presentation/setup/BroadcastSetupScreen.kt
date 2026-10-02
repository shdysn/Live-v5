package pk.livecaster.app.broadcast.presentation.setup

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
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.FacebookBrandColor
import com.example.ui.theme.LiveRed
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import com.example.ui.theme.StudioAmber
import com.example.ui.theme.StudioGreen
import com.example.ui.theme.RtmpBrandColor
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCard
import com.example.ui.theme.StudioCyan
import com.example.ui.theme.StudioDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.YouTubeBrandColor
import pk.livecaster.app.broadcast.domain.model.PlatformType

@Composable
fun BroadcastSetupScreen(
    viewModel: BroadcastSetupViewModel,
    onLaunchStudio: (broadcastId: Long) -> Unit,
    onNavigateToConnect: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(StudioDark)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(LiveRed.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Videocam,
                    contentDescription = null,
                    tint = LiveRed,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "New Live Stream Session",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
                Text(
                    text = "Configure studio destination and stream encoder parameters",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
            }
        }

        // Platform Selection
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Select Ingest Destination",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            )
            if (onNavigateToConnect != null) {
                Text(
                    text = "Connect Accounts →",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = StudioCyan
                    ),
                    modifier = Modifier
                        .clickable { onNavigateToConnect() }
                        .testTag("setup_connect_accounts_link")
                )
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PlatformChip(
                    name = "Simulcast (FB + YT)",
                    icon = Icons.Default.Hub,
                    isSelected = uiState.platform == PlatformType.MULTI_DESTINATION,
                    onClick = { viewModel.updatePlatform(PlatformType.MULTI_DESTINATION) },
                    modifier = Modifier.weight(1f)
                )
                PlatformChip(
                    name = "Facebook",
                    icon = Icons.Default.Public,
                    isSelected = uiState.platform == PlatformType.FACEBOOK,
                    onClick = { viewModel.updatePlatform(PlatformType.FACEBOOK) },
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PlatformChip(
                    name = "YouTube",
                    icon = Icons.Default.PlayCircle,
                    isSelected = uiState.platform == PlatformType.YOUTUBE,
                    onClick = { viewModel.updatePlatform(PlatformType.YOUTUBE) },
                    modifier = Modifier.weight(1f)
                )
                PlatformChip(
                    name = "Custom RTMP",
                    icon = Icons.Default.Cast,
                    isSelected = uiState.platform == PlatformType.CUSTOM_RTMP,
                    onClick = { viewModel.updatePlatform(PlatformType.CUSTOM_RTMP) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Session Information Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = StudioCard),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Stream Details",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    ),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                OutlinedTextField(
                    value = uiState.title,
                    onValueChange = { viewModel.updateTitle(it) },
                    label = { Text("Broadcast Title") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("broadcast_title_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = { viewModel.updateDescription(it) },
                    label = { Text("Stream Description") },
                    maxLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("broadcast_desc_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (uiState.platform == PlatformType.MULTI_DESTINATION) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(StudioCyan.copy(alpha = 0.12f))
                            .border(1.dp, StudioCyan.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Hub,
                                contentDescription = null,
                                tint = StudioCyan,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Simulcast Mode: Live camera and audio will stream simultaneously to BOTH Facebook Live and YouTube Live!",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextPrimary)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 1-Tap Mobile Direct Broadcast Indicator Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(StudioCard)
                            .border(1.dp, if (uiState.isPersistentKeySaved) StudioGreen.copy(alpha = 0.5f) else StudioBorder, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.FlashOn,
                                        contentDescription = null,
                                        tint = if (uiState.isPersistentKeySaved) StudioGreen else StudioAmber,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (uiState.isPersistentKeySaved) "1-Tap Mobile Stream Ready" else "1-Tap Mobile Live Setup",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (uiState.isPersistentKeySaved) StudioGreen else StudioAmber
                                        )
                                    )
                                }

                                Text(
                                    text = "Auto-Start Guide ⓘ",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = StudioCyan,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier
                                        .clickable { viewModel.toggleAutoStartGuide(true) }
                                        .testTag("auto_start_guide_trigger")
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (uiState.isPersistentKeySaved)
                                    "Persistent Key is saved in memory. When you tap 'Start Live', LiveCaster directly broadcasts from mobile without opening any browser."
                                else
                                    "Paste your Facebook Persistent Stream Key once below. It is automatically remembered for 1-tap broadcasts.",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = uiState.streamKey,
                        onValueChange = { viewModel.updateStreamKey(it) },
                        label = { Text("Facebook Persistent Stream Key") },
                        placeholder = { Text("Paste FB Live Stream Key (starts with FB-...)") },
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Public,
                                contentDescription = null,
                                tint = FacebookBrandColor
                            )
                        },
                        trailingIcon = {
                            if (uiState.isPersistentKeySaved) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Saved",
                                    tint = StudioGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("broadcast_fb_stream_key_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = uiState.youtubeStreamKey,
                        onValueChange = { viewModel.updateYoutubeStreamKey(it) },
                        label = { Text("YouTube Stream Key (Optional for Simulcast)") },
                        placeholder = { Text("Paste YouTube Live Stream Key here") },
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.PlayCircle,
                                contentDescription = null,
                                tint = YouTubeBrandColor
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("broadcast_yt_stream_key_input")
                    )
                } else {
                    OutlinedTextField(
                        value = uiState.rtmpUrl,
                        onValueChange = { viewModel.updateRtmpUrl(it) },
                        label = { Text("RTMP Server Endpoint") },
                        placeholder = { Text("e.g. rtmp://a.rtmp.youtube.com/live2") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("broadcast_rtmp_url_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = uiState.streamKey,
                        onValueChange = { viewModel.updateStreamKey(it) },
                        label = { Text("Stream Key") },
                        placeholder = { Text("Enter your live stream key") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("broadcast_stream_key_input")
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Test Connection Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { viewModel.testConnection() },
                        enabled = !uiState.isTestingConnection && uiState.rtmpUrl.isNotBlank(),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("test_connection_button")
                    ) {
                        if (uiState.isTestingConnection) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = StudioCyan
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Testing Ping...", style = MaterialTheme.typography.bodySmall.copy(color = StudioCyan))
                        } else {
                            Icon(
                                imageVector = Icons.Default.NetworkCheck,
                                contentDescription = null,
                                tint = StudioCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Test Connection", style = MaterialTheme.typography.bodySmall.copy(color = StudioCyan))
                        }
                    }

                    if (uiState.testConnectionResult != null) {
                        Text(
                            text = "Clear",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextMuted),
                            modifier = Modifier
                                .clickable { viewModel.clearConnectionTestResult() }
                                .padding(4.dp)
                        )
                    }
                }

                // Connection Test Result Badge
                uiState.testConnectionResult?.let { result ->
                    Spacer(modifier = Modifier.height(10.dp))
                    when (result) {
                        is ConnectionTestResult.Success -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(StudioGreen.copy(alpha = 0.15f))
                                    .border(1.dp, StudioGreen.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Success",
                                        tint = StudioGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Connection Successful!",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = StudioGreen
                                            )
                                        )
                                        Text(
                                            text = "Reachable at ${result.host}:${result.port} • Latency: ${result.latencyMs} ms",
                                            style = MaterialTheme.typography.labelSmall.copy(color = TextPrimary)
                                        )
                                    }
                                }
                            }
                        }
                        is ConnectionTestResult.Error -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(LiveRed.copy(alpha = 0.15f))
                                    .border(1.dp, LiveRed.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.ErrorOutline,
                                        contentDescription = "Error",
                                        tint = LiveRed,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Connection Failed",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = LiveRed
                                            )
                                        )
                                        Text(
                                            text = result.message,
                                            style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quality Presets Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = StudioCard),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Broadcast Profile Preset",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    ),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QualityChip(
                        label = "1080p 60fps",
                        sub = "6.0 Mbps",
                        isSelected = uiState.resolution == "1080p",
                        onClick = { viewModel.updateQuality("1080p", 6000, 60) },
                        modifier = Modifier.weight(1f)
                    )
                    QualityChip(
                        label = "720p 30fps",
                        sub = "3.5 Mbps",
                        isSelected = uiState.resolution == "720p",
                        onClick = { viewModel.updateQuality("720p", 3500, 30) },
                        modifier = Modifier.weight(1f)
                    )
                    QualityChip(
                        label = "480p 30fps",
                        sub = "1.8 Mbps",
                        isSelected = uiState.resolution == "480p",
                        onClick = { viewModel.updateQuality("480p", 1800, 30) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        if (uiState.errorMessage != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = uiState.errorMessage!!,
                style = MaterialTheme.typography.bodySmall.copy(color = LiveRed)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Go Live Button
        Button(
            onClick = {
                viewModel.createAndStartBroadcast { id ->
                    onLaunchStudio(id)
                }
            },
            enabled = !uiState.isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = LiveRed),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("launch_studio_button")
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    color = TextPrimary,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Emergency,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Initialize Live Studio",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                }
            }
        }

        if (uiState.showAutoStartGuideDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.toggleAutoStartGuide(false) },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FlashOn,
                            contentDescription = null,
                            tint = StudioAmber,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "1-Tap Mobile Live Guide",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "To stream directly from LiveCaster on your phone without ever opening a browser:",
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary)
                        )
                        Text(
                            text = "1. Persistent Stream Key:\nYour stream key is already saved in LiveCaster. It never expires.",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )
                        Text(
                            text = "2. Facebook Auto-Start Setting:\nIn Facebook Live Producer under Settings -> Stream, keep 'Persistent stream key' ON and 'Allow encoder to end stream' ON.",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )
                        Text(
                            text = "3. Pure 1-Tap Experience:\nNow, whenever you open LiveCaster and tap 'Start Live', your stream automatically publishes directly to your Facebook profile!",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = StudioGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { viewModel.toggleAutoStartGuide(false) }) {
                        Text("Understood", color = StudioCyan, fontWeight = FontWeight.Bold)
                    }
                },
                containerColor = StudioCard,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

@Composable
fun PlatformChip(
    name: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) LiveRed.copy(alpha = 0.15f) else StudioCard)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) LiveRed else StudioBorder,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = name,
                tint = if (isSelected) LiveRed else TextMuted,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) TextPrimary else TextSecondary
                )
            )
        }
    }
}

@Composable
fun QualityChip(
    label: String,
    sub: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) LiveRed.copy(alpha = 0.15f) else StudioDark)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) LiveRed else StudioBorder,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) LiveRed else TextPrimary
                )
            )
            Text(
                text = sub,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = if (isSelected) TextPrimary else TextMuted
                )
            )
        }
    }
}
