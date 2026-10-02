package pk.livecaster.app

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.theme.LiveRed
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCard
import com.example.ui.theme.StudioDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import pk.livecaster.app.core.di.AppContainer
import pk.livecaster.app.core.util.PermissionHelper
import pk.livecaster.app.navigation.LiveCasterNavGraph
import pk.livecaster.app.navigation.Screen
import pk.livecaster.app.navigation.bottomNavItems

@Composable
fun LiveCasterApp(
    appContainer: AppContainer,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Check permissions on startup
    val context = LocalContext.current
    var permissionsGranted by remember { mutableStateOf(PermissionHelper.hasAllPermissions(context)) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        permissionsGranted = results.values.all { it }
    }

    LaunchedEffect(Unit) {
        if (!permissionsGranted) {
            permissionLauncher.launch(PermissionHelper.REQUIRED_PERMISSIONS)
        }
    }

    // Hide bottom navigation in full screen studio control mode
    val isStudioControl = currentRoute?.startsWith("broadcast_control") == true

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = StudioDark,
        bottomBar = {
            AnimatedVisibility(
                visible = !isStudioControl,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                NavigationBar(
                    containerColor = StudioCard,
                    contentColor = TextPrimary,
                    tonalElevation = 8.dp,
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .testTag("main_bottom_nav_bar")
                ) {
                    bottomNavItems.forEach { screen ->
                        val isSelected = currentRoute == screen.route
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                screen.icon?.let { icon ->
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = screen.title,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = LiveRed,
                                selectedTextColor = LiveRed,
                                indicatorColor = LiveRed.copy(alpha = 0.15f),
                                unselectedIconColor = TextMuted,
                                unselectedTextColor = TextMuted
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        LiveCasterNavGraph(
            navController = navController,
            appContainer = appContainer,
            paddingValues = if (isStudioControl) androidx.compose.foundation.layout.PaddingValues(0.dp) else innerPadding
        )
    }
}
