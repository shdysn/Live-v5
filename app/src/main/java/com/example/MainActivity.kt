package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch
import pk.livecaster.app.LiveCasterApp
import pk.livecaster.app.core.di.AppContainer

class MainActivity : ComponentActivity() {

    private lateinit var appContainer: AppContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        try {
            appContainer = AppContainer(applicationContext)
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Failed to initialize AppContainer", e)
            appContainer = AppContainer(applicationContext)
        }

        handleOAuthIntent(intent)

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LiveCasterApp(appContainer = appContainer)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleOAuthIntent(intent)
    }

    private fun handleOAuthIntent(intent: Intent?) {
        val uri = intent?.data ?: return
        lifecycleScope.launch {
            try {
                appContainer.oAuthChromeManager.handleRedirect(uri)
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Error handling OAuth deep link", e)
            }
        }
    }
}
