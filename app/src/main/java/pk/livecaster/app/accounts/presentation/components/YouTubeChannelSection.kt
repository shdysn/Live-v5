package pk.livecaster.app.accounts.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCard
import com.example.ui.theme.StudioCardElevated
import com.example.ui.theme.StudioDark
import com.example.ui.theme.StudioGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.YouTubeBrandColor

@Composable
fun YouTubeChannelSection(
    isLoggedIn: Boolean,
    isConnected: Boolean,
    connectedName: String,
    selectedChannel: String,
    availableChannels: List<String>,
    permissions: List<String>,
    onContinueWithGoogle: () -> Unit,
    onSelectChannel: (String) -> Unit,
    onCancel: () -> Unit,
    onConnectChannel: () -> Unit,
    onDisconnect: () -> Unit,
    onLoginWithChrome: (() -> Unit)? = null,
    onOpenLiveStudio: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isConnected) YouTubeBrandColor.copy(alpha = 0.5f) else StudioBorder,
                RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = StudioCard)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header: YouTube Channel
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(YouTubeBrandColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "YouTube",
                            tint = YouTubeBrandColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "YouTube Channel",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                }

                if (isConnected) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(StudioGreen.copy(alpha = 0.15f))
                            .border(1.dp, StudioGreen.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = StudioGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Live",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = StudioGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // State A: Connected State Display
            if (isConnected && !isLoggedIn) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(StudioCardElevated)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Connected Channel",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                        )
                        Text(
                            text = connectedName,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        )
                    }
                    Row {
                        IconButton(
                            onClick = onContinueWithGoogle,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Switch Channel",
                                tint = YouTubeBrandColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(
                            onClick = onDisconnect,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Disconnect Channel",
                                tint = TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // State B: Pre-Login Button
            if (!isConnected && !isLoggedIn) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onLoginWithChrome ?: onContinueWithGoogle,
                        colors = ButtonDefaults.buttonColors(containerColor = YouTubeBrandColor),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("continue_with_google_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Login via Chrome (Official Google)",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }

                    if (onOpenLiveStudio != null) {
                        OutlinedButton(
                            onClick = onOpenLiveStudio,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("open_yt_live_studio_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.OpenInBrowser,
                                contentDescription = null,
                                tint = YouTubeBrandColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Open YouTube Live Studio in Chrome ↗",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                            )
                        }
                    }
                }
            }

            // State C: Login کے بعد: (Select Channel + Permissions)
            if (isLoggedIn) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(StudioDark.copy(alpha = 0.5f))
                        .border(1.dp, StudioBorder, RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Badge: Login کے بعد:
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(YouTubeBrandColor.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Login کے بعد:",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = YouTubeBrandColor
                                )
                            )
                        }
                    }

                    // Select YouTube Channel
                    Text(
                        text = "Select YouTube Channel",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    )

                    PageDropdownSelector(
                        selectedItem = selectedChannel,
                        items = availableChannels,
                        onItemSelected = onSelectChannel,
                        testTag = "youtube_channel_dropdown"
                    )

                    // Permissions Checklist
                    Text(
                        text = "Permissions:",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        permissions.forEach { perm ->
                            PermissionItem(text = perm)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Buttons: [ Cancel ]   [ Connect Channel ]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onCancel,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("cancel_youtube_button")
                        ) {
                            Text(
                                text = "Cancel",
                                color = TextSecondary,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        Button(
                            onClick = onConnectChannel,
                            colors = ButtonDefaults.buttonColors(containerColor = YouTubeBrandColor),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1.2f)
                                .height(44.dp)
                                .testTag("connect_channel_button")
                        ) {
                            Text(
                                text = "Connect Channel",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }
    }
}
