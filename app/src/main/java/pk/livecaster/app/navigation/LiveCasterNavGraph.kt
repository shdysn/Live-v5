package pk.livecaster.app.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import pk.livecaster.app.accounts.presentation.ConnectAccountsScreen
import pk.livecaster.app.accounts.presentation.ConnectAccountsViewModel
import pk.livecaster.app.auth.presentation.login.LoginScreen
import pk.livecaster.app.auth.presentation.login.LoginViewModel
import pk.livecaster.app.broadcast.presentation.control.BroadcastControlScreen
import pk.livecaster.app.broadcast.presentation.control.BroadcastControlViewModel
import pk.livecaster.app.broadcast.presentation.setup.BroadcastSetupScreen
import pk.livecaster.app.broadcast.presentation.setup.BroadcastSetupViewModel
import pk.livecaster.app.core.di.AppContainer
import pk.livecaster.app.dashboard.presentation.DashboardScreen
import pk.livecaster.app.dashboard.presentation.DashboardViewModel
import pk.livecaster.app.facebook.presentation.pages.FacebookPagesScreen
import pk.livecaster.app.facebook.presentation.pages.FacebookPagesViewModel
import pk.livecaster.app.settings.presentation.SettingsScreen
import pk.livecaster.app.settings.presentation.SettingsViewModel
import pk.livecaster.app.youtube.presentation.channels.YouTubeChannelsScreen
import pk.livecaster.app.youtube.presentation.channels.YouTubeChannelsViewModel

@Composable
fun LiveCasterNavGraph(
    navController: NavHostController,
    appContainer: AppContainer,
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route,
        modifier = modifier.padding(paddingValues),
        enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(250)) },
        exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(250)) },
        popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(250)) },
        popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(250)) }
    ) {
        composable(Screen.Dashboard.route) {
            val viewModel = remember {
                DashboardViewModel(
                    appContainer.getBroadcastsUseCase,
                    appContainer.broadcastRepository,
                    appContainer.facebookRepository,
                    appContainer.youtubeRepository
                )
            }
            DashboardScreen(
                viewModel = viewModel,
                onNavigateToSetup = { navController.navigate(Screen.BroadcastSetup.route) },
                onNavigateToStudio = { broadcastId ->
                    navController.navigate(Screen.BroadcastControl.createRoute(broadcastId))
                },
                onNavigateToFacebook = { navController.navigate(Screen.FacebookPages.route) },
                onNavigateToYouTube = { navController.navigate(Screen.YouTubeChannels.route) },
                onNavigateToConnect = { navController.navigate(Screen.ConnectAccounts.route) }
            )
        }

        composable(Screen.ConnectAccounts.route) {
            val viewModel = remember {
                ConnectAccountsViewModel(
                    appContainer.facebookRepository,
                    appContainer.youtubeRepository,
                    appContainer.tokenStorage,
                    appContainer.oAuthChromeManager
                )
            }
            ConnectAccountsScreen(
                viewModel = viewModel,
                onContinue = {
                    navController.navigate(Screen.BroadcastSetup.route)
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.BroadcastSetup.route) {
            val viewModel = remember {
                BroadcastSetupViewModel(
                    appContainer.context,
                    appContainer.createBroadcastUseCase,
                    appContainer.facebookRepository,
                    appContainer.youtubeRepository
                )
            }
            BroadcastSetupScreen(
                viewModel = viewModel,
                onLaunchStudio = { broadcastId ->
                    navController.navigate(Screen.BroadcastControl.createRoute(broadcastId)) {
                        popUpTo(Screen.Dashboard.route)
                    }
                },
                onNavigateToConnect = { navController.navigate(Screen.ConnectAccounts.route) }
            )
        }

        composable(
            route = Screen.BroadcastControl.route,
            arguments = listOf(
                navArgument("broadcastId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val broadcastId = backStackEntry.arguments?.getLong("broadcastId") ?: 0L
            val viewModel = remember(broadcastId) {
                BroadcastControlViewModel(
                    broadcastId = broadcastId,
                    getBroadcastsUseCase = appContainer.getBroadcastsUseCase,
                    updateStatusUseCase = appContainer.updateBroadcastStatusUseCase,
                    broadcastRepository = appContainer.broadcastRepository,
                    publisher = appContainer.rtmpPublisher,
                    appContext = appContainer.context
                )
            }
            BroadcastControlScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.FacebookPages.route) {
            val viewModel = remember {
                FacebookPagesViewModel(
                    appContainer.getFacebookPagesUseCase,
                    appContainer.facebookRepository
                )
            }
            FacebookPagesScreen(
                viewModel = viewModel,
                onStartStreamForPage = { pageId ->
                    navController.navigate(Screen.BroadcastSetup.route)
                }
            )
        }

        composable(Screen.YouTubeChannels.route) {
            val viewModel = remember {
                YouTubeChannelsViewModel(
                    appContainer.getYouTubeChannelsUseCase,
                    appContainer.youtubeRepository
                )
            }
            YouTubeChannelsScreen(
                viewModel = viewModel,
                onStartStreamForChannel = { channelId ->
                    navController.navigate(Screen.BroadcastSetup.route)
                }
            )
        }

        composable(Screen.Settings.route) {
            val viewModel = remember {
                SettingsViewModel(appContainer.settingsRepository)
            }
            SettingsScreen(viewModel = viewModel)
        }

        composable(Screen.AuthLogin.route) {
            val viewModel = remember {
                LoginViewModel(
                    appContainer.loginUseCase,
                    appContainer.logoutUseCase,
                    appContainer.authRepository
                )
            }
            LoginScreen(
                viewModel = viewModel,
                onNavigateToDashboard = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                }
            )
        }
    }
}
