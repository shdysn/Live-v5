package pk.livecaster.app.core.util

import java.util.Locale
import java.util.concurrent.TimeUnit

object Formatters {
    fun formatDuration(durationSeconds: Long): String {
        val hours = TimeUnit.SECONDS.toHours(durationSeconds)
        val minutes = TimeUnit.SECONDS.toMinutes(durationSeconds) % 60
        val seconds = durationSeconds % 60
        return if (hours > 0) {
            String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.US, "%02d:%02d", minutes, seconds)
        }
    }

    fun formatBitrate(kbps: Int): String {
        return if (kbps >= 1000) {
            String.format(Locale.US, "%.1f Mbps", kbps / 1000f)
        } else {
            "$kbps kbps"
        }
    }

    fun formatViewers(count: Long): String {
        return when {
            count >= 1_000_000 -> String.format(Locale.US, "%.1fM", count / 1_000_000f)
            count >= 1_000 -> String.format(Locale.US, "%.1fK", count / 1_000f)
            else -> count.toString()
        }
    }
}
