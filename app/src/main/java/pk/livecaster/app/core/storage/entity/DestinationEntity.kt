package pk.livecaster.app.core.storage.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "destinations")
data class DestinationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val platform: String, // FACEBOOK, YOUTUBE, CUSTOM_RTMP
    val rtmpUrl: String,
    val streamKey: String,
    val pageOrChannelId: String? = null,
    val isEnabled: Boolean = true,
    val iconUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
