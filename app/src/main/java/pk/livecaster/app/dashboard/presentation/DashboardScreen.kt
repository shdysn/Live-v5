package pk.livecaster.app.dashboard.presentation

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.FacebookBrandColor
import com.example.ui.theme.LiveRed
import com.example.ui.theme.RtmpBrandColor
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCard
import com.example.ui.theme.StudioCardElevated
import com.example.ui.theme.StudioCyan
import com.example.ui.theme.StudioDark
import com.example.ui.theme.StudioGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.YouTubeBrandColor
import pk.livecaster.app.broadcast.domain.model.Broadcast
import pk.livecaster.app.broadcast.domain.model.BroadcastStatus
import pk.livecaster.app.broadcast.domain.model.PlatformType
import pk.livecaster.app.core.util.Formatters

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToSetup: () -> Unit,
    onNavigateToStudio: (broadcastId: Long) -> Unit,
    onNavigateToFacebook: () -> Unit,
    onNavigateToYouTube: () -> Unit,
    onNavigateToConnect: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = StudioDark,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToSetup,
                containerColor = LiveRed,
                contentColor = TextPrimary,
                shape = CircleShape,
                modifier = Modifier.testTag("dashboard_new_broadcast_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Broadcast")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Studio Header Banner
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("studio_hero_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = StudioCard)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        LiveRed.copy(alpha = 0.25f),
                                        StudioCardElevated
                                    )
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(LiveRed),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Radio,
                                            contentDescription = null,
                                            tint = TextPrimary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "LiveCaster Studio",
                                            style = MaterialTheme.typography.titleLarge.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary
                                            )
                                        )
                                        Text(
                                            text = "Broadcasting Hub • Pakistan",
                                            style = MaterialTheme.typography.labelSmall.copy(color = StudioCyan)
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(StudioGreen.copy(alpha = 0.2f))
                                        .border(1.dp, StudioGreen, RoundedCornerShape(12.dp))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "READY",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = StudioGreen
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Stream concurrently to Facebook, YouTube, and Custom RTMP servers with full camera telemetry.",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = onNavigateToSetup,
                                colors = ButtonDefaults.buttonColors(containerColor = LiveRed),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("hero_go_live_button")
                            ) {
                                Icon(Icons.Default.Videocam, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Start New Broadcast Session",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // 2. Telemetry Overview Metric Grid
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = "Broadcasts",
                        value = "${uiState.stats.totalStreams}",
                        icon = Icons.Default.PlayCircle,
                        color = StudioCyan,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Airtime",
                        value = Formatters.formatDuration(uiState.stats.totalAirtimeSeconds),
                        icon = Icons.Default.History,
                        color = StudioGreen,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Peak Viewers",
                        value = Formatters.formatViewers(uiState.stats.peakAudience),
                        icon = Icons.Default.TrendingUp,
                        color = LiveRed,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 3. Destinations Quick Manager
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Streaming Destinations",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        ),
                        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                    )
                    Text(
                        text = "Connect Accounts →",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = StudioCyan
                        ),
                        modifier = Modifier
                            .clickable(onClick = onNavigateToConnect)
                            .testTag("dashboard_connect_accounts_link")
                    )
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onNavigateToConnect)
                        .border(1.dp, StudioGreen.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                        .testTag("dashboard_connected_accounts_card"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = StudioCard)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(StudioGreen.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Hub,
                                    contentDescription = null,
                                    tint = StudioGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Connected Live Accounts",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                )
                                val statusText = when {
                                    uiState.fbPagesCount > 0 && uiState.ytChannelsCount > 0 ->
                                        "✓ Facebook: Linked • ✓ YouTube: Linked"
                                    uiState.fbPagesCount > 0 ->
                                        "✓ Facebook: Linked • YouTube: Not connected"
                                    uiState.ytChannelsCount > 0 ->
                                        "Facebook: Not connected • ✓ YouTube: Linked"
                                    else ->
                                        "No accounts connected yet. Tap to set up."
                                }
                                Text(
                                    text = statusText,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (uiState.fbPagesCount > 0 || uiState.ytChannelsCount > 0) StudioGreen else TextMuted
                                    )
                                )
                            }
                        }

                        Button(
                            onClick = onNavigateToConnect,
                            colors = ButtonDefaults.buttonColors(containerColor = StudioCardElevated),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text(
                                text = "Manage",
                                color = TextPrimary,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DestinationCard(
                        title = "Facebook Live",
                        statusText = "${uiState.fbPagesCount} Pages Linked",
                        icon = Icons.Default.Public,
                        brandColor = FacebookBrandColor,
                        onClick = onNavigateToFacebook,
                        modifier = Modifier.weight(1f)
                    )
                    DestinationCard(
                        title = "YouTube Live",
                        statusText = "${uiState.ytChannelsCount} Channels Linked",
                        icon = Icons.Default.PlayArrow,
                        brandColor = YouTubeBrandColor,
                        onClick = onNavigateToYouTube,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 4. Broadcast History List
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Session History & Logs",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Text(
                        text = "${uiState.broadcasts.size} Sessions",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                    )
                }
            }

            if (uiState.broadcasts.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, StudioBorder, RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = StudioCard),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(LiveRed.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Videocam,
                                    contentDescription = null,
                                    tint = LiveRed,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Ready to Broadcast",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "No stream sessions yet. Connect your channel or configure a Custom RTMP stream to go live.",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onNavigateToSetup,
                                colors = ButtonDefaults.buttonColors(containerColor = LiveRed),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("New Live Stream", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            } else {
                items(uiState.broadcasts, key = { it.id }) { broadcast ->
                    BroadcastHistoryCard(
                        broadcast = broadcast,
                        onOpenStudio = { onNavigateToStudio(broadcast.id) },
                        onDelete = { viewModel.deleteBroadcast(broadcast.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = StudioCard),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                ),
                maxLines = 1
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
            )
        }
    }
}

@Composable
fun DestinationCard(
    title: String,
    statusText: String,
    icon: ImageVector,
    brandColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable(onClick = onClick)
            .border(1.dp, brandColor.copy(alpha = 0.3f), RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = StudioCard),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(brandColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = brandColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                )
            }
        }
    }
}

@Composable
fun BroadcastHistoryCard(
    broadcast: Broadcast,
    onOpenStudio: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenStudio)
            .border(1.dp, StudioBorder, RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = StudioCard),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = broadcast.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        ),
                        maxLines = 1
                    )
                    Text(
                        text = "${broadcast.platform.name} • ${broadcast.resolution} @ ${broadcast.fps}fps • ${broadcast.bitrateKbps} kbps",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            when (broadcast.status) {
                                BroadcastStatus.LIVE -> LiveRed
                                BroadcastStatus.ENDED -> StudioCardElevated
                                else -> StudioBorder
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = broadcast.status.name,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete broadcast",
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Duration: ${Formatters.formatDuration(broadcast.durationSeconds)} • Peak: ${Formatters.formatViewers(broadcast.peakViewers)}",
                    style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                )

                Text(
                    text = "Open Studio →",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = StudioCyan
                    )
                )
            }
        }
    }
}
