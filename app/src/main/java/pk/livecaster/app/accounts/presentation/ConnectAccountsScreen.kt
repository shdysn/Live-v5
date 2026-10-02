package pk.livecaster.app.accounts.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LiveRed
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCard
import com.example.ui.theme.StudioDark
import com.example.ui.theme.StudioGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import pk.livecaster.app.accounts.presentation.components.FacebookPageSection
import pk.livecaster.app.accounts.presentation.components.YouTubeChannelSection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectAccountsScreen(
    viewModel: ConnectAccountsViewModel,
    onContinue: () -> Unit,
    onNavigateBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = androidx.compose.ui.platform.LocalContext.current

    if (onNavigateBack != null) {
        BackHandler(onBack = onNavigateBack)
    }

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = StudioDark,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(LiveRed.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Hub,
                                contentDescription = null,
                                tint = LiveRed,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Connect Live Accounts",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                    }
                },
                navigationIcon = {
                    if (onNavigateBack != null) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = TextPrimary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = StudioDark)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ──────────────────────────────
            // SECTION 1: FACEBOOK PAGE
            // ──────────────────────────────
            FacebookPageSection(
                isLoggedIn = uiState.isFacebookLoggedIn,
                isConnected = uiState.isFacebookConnected,
                connectedName = uiState.facebookConnectedName,
                selectedPage = uiState.selectedFacebookPage,
                availablePages = uiState.availableFacebookPages,
                permissions = uiState.facebookPermissions,
                onContinueWithFacebook = { viewModel.continueWithFacebook() },
                onSelectPage = { viewModel.selectFacebookPage(it) },
                onCancel = { viewModel.cancelFacebook() },
                onConnectPage = { viewModel.connectFacebookPage() },
                onDisconnect = { viewModel.disconnectFacebook() },
                customPageName = uiState.customPageName,
                onCustomPageNameChange = { viewModel.updateCustomPageName(it) },
                customStreamKey = uiState.customPageStreamKey,
                onCustomStreamKeyChange = { viewModel.updateCustomPageStreamKey(it) },
                customAppId = uiState.customFacebookAppId,
                onCustomAppIdChange = { viewModel.updateFacebookAppId(it) },
                showDevOAuth = uiState.showFacebookDevOAuth,
                onToggleDevOAuth = { viewModel.toggleFacebookDevOAuth() },
                onLoginWithChrome = { viewModel.openFacebookInChrome(context) },
                onOpenLiveProducer = { viewModel.openFacebookLiveProducer(context) }
            )

            HorizontalDivider(color = StudioBorder, thickness = 1.dp)

            // ──────────────────────────────
            // SECTION 2: YOUTUBE CHANNEL
            // ──────────────────────────────
            YouTubeChannelSection(
                isLoggedIn = uiState.isGoogleLoggedIn,
                isConnected = uiState.isYouTubeConnected,
                connectedName = uiState.youtubeConnectedName,
                selectedChannel = uiState.selectedYouTubeChannel,
                availableChannels = uiState.availableYouTubeChannels,
                permissions = uiState.youTubePermissions,
                onContinueWithGoogle = { viewModel.continueWithGoogle() },
                onSelectChannel = { viewModel.selectYouTubeChannel(it) },
                onCancel = { viewModel.cancelGoogle() },
                onConnectChannel = { viewModel.connectYouTubeChannel() },
                onDisconnect = { viewModel.disconnectYouTube() },
                customChannelName = uiState.customChannelName,
                onCustomChannelNameChange = { viewModel.updateCustomChannelName(it) },
                customStreamKey = uiState.customChannelStreamKey,
                onCustomStreamKeyChange = { viewModel.updateCustomChannelStreamKey(it) },
                customClientId = uiState.customGoogleClientId,
                onCustomClientIdChange = { viewModel.updateGoogleClientId(it) },
                showDevOAuth = uiState.showYouTubeDevOAuth,
                onToggleDevOAuth = { viewModel.toggleYouTubeDevOAuth() },
                onLoginWithChrome = { viewModel.openGoogleInChrome(context) },
                onOpenLiveStudio = { viewModel.openYouTubeLiveStudio(context) }
            )

            HorizontalDivider(color = StudioBorder, thickness = 1.dp)

            // ──────────────────────────────
            // SECTION 3: CONNECTED ACCOUNTS
            // ──────────────────────────────
            ConnectedAccountsSummaryCard(
                isFacebookLive = uiState.isFacebookConnected,
                facebookName = uiState.facebookConnectedName,
                isYouTubeLive = uiState.isYouTubeConnected,
                youtubeName = uiState.youtubeConnectedName,
                onContinue = onContinue
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ConnectedAccountsSummaryCard(
    isFacebookLive: Boolean,
    facebookName: String,
    isYouTubeLive: Boolean,
    youtubeName: String,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, StudioGreen.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .testTag("connected_accounts_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = StudioCard)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Connected Accounts",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // ✓ Facebook: Live
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = if (isFacebookLive) StudioGreen else TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Facebook: ",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    )
                    Text(
                        text = if (isFacebookLive) "Live" else "Not Connected",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isFacebookLive) StudioGreen else TextMuted
                        )
                    )
                }

                if (isFacebookLive) {
                    Text(
                        text = facebookName,
                        style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary),
                        maxLines = 1
                    )
                }
            }

            // ✓ YouTube: Live
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = if (isYouTubeLive) StudioGreen else TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "YouTube: ",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    )
                    Text(
                        text = if (isYouTubeLive) "Live" else "Not Connected",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isYouTubeLive) StudioGreen else TextMuted
                        )
                    )
                }

                if (isYouTubeLive) {
                    Text(
                        text = youtubeName,
                        style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary),
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // [ Continue ] Button
            Button(
                onClick = onContinue,
                colors = ButtonDefaults.buttonColors(containerColor = LiveRed),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("continue_to_broadcast_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Radio,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Continue",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                )
            }
        }
    }
}
