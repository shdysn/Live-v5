package pk.livecaster.app.core.storage.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "broadcasts")
data class BroadcastEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val rtmpUrl: String,
    val streamKey: String,
    val platform: String, // FACEBOOK, YOUTUBE, CUSTOM_RTMP, MULTI_DESTINATION
    val status: String,   // CREATED, LIVE, ENDED, FAILED
    val durationSeconds: Long = 0,
    val resolution: String = "720p",
    val bitrateKbps: Int = 3500,
    val fps: Int = 30,
    val peakViewers: Long = 0,
    val startedAt: Long? = null,
    val endedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
