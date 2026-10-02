package pk.livecaster.app.streaming.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.ServiceCompat
import pk.livecaster.app.core.constants.AppConstants
import pk.livecaster.app.streaming.notification.StreamNotificationManager

class LiveStreamingService : Service() {
    private var wakeLock: PowerManager.WakeLock? = null
    private var wifiLock: WifiManager.WifiLock? = null
    private lateinit var notificationManager: StreamNotificationManager

    override fun onCreate() {
        super.onCreate()
        notificationManager = StreamNotificationManager(this)
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "LiveCaster:StreamWakeLock")

        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
        wifiLock = wifiManager?.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "LiveCaster:StreamWifiLock")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        val broadcastTitle = intent?.getStringExtra(EXTRA_BROADCAST_TITLE) ?: "Live Broadcast"

        when (action) {
            ACTION_START -> {
                wakeLock?.acquire(1000 * 60 * 180L) // 3 hours max wake lock
                try {
                    wifiLock?.acquire()
                } catch (_: Exception) {}
                val notification = notificationManager.buildNotification(
                    title = broadcastTitle,
                    statusText = "Streaming live • Tap to return to studio",
                    isLive = true
                )
                val foregroundType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA or ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
                } else {
                    0
                }
                ServiceCompat.startForeground(this, AppConstants.NOTIFICATION_ID, notification, foregroundType)
            }
            ACTION_STOP -> {
                stopStreamService()
            }
        }
        return START_NOT_STICKY
    }

    private fun stopStreamService() {
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }
        if (wifiLock?.isHeld == true) {
            wifiLock?.release()
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }
        if (wifiLock?.isHeld == true) {
            wifiLock?.release()
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_START = "pk.livecaster.app.ACTION_START_STREAM"
        const val ACTION_STOP = "pk.livecaster.app.ACTION_STOP_STREAM"
        const val EXTRA_BROADCAST_TITLE = "extra_broadcast_title"

        fun startService(context: Context, broadcastTitle: String) {
            val intent = Intent(context, LiveStreamingService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_BROADCAST_TITLE, broadcastTitle)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, LiveStreamingService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }
}
