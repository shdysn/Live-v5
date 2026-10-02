package pk.livecaster.app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Dashboard)
    object ConnectAccounts : Screen("connect_accounts", "Connect", Icons.Default.Hub)
    object BroadcastSetup : Screen("broadcast_setup", "New Stream", Icons.Default.Videocam)
    object BroadcastControl : Screen("broadcast_control/{broadcastId}", "Live Studio") {
        fun createRoute(broadcastId: Long) = "broadcast_control/$broadcastId"
    }
    object FacebookPages : Screen("facebook_pages", "Facebook", Icons.Default.Public)
    object YouTubeChannels : Screen("youtube_channels", "YouTube", Icons.Default.PlayCircle)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    object AuthLogin : Screen("auth_login", "Account", Icons.Default.Person)
}

val bottomNavItems = listOf(
    Screen.Dashboard,
    Screen.ConnectAccounts,
    Screen.BroadcastSetup,
    Screen.FacebookPages,
    Screen.YouTubeChannels,
    Screen.Settings
)
