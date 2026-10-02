package pk.livecaster.app.core.storage.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stream_logs")
data class StreamLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val broadcastId: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val currentFps: Int,
    val currentBitrateKbps: Int,
    val droppedFrames: Long,
    val streamHealth: String, // EXCELLENT, GOOD, POOR, CRITICAL
    val currentViewers: Long
)
