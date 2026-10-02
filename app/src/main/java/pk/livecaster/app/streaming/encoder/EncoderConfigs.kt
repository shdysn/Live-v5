package pk.livecaster.app.streaming.encoder

import android.media.MediaFormat

data class VideoEncoderConfig(
    val width: Int = 1280,
    val height: Int = 720,
    val bitrateKbps: Int = 3500,
    val frameRate: Int = 30,
    val iFrameIntervalSec: Int = 2,
    val mimeType: String = MediaFormat.MIMETYPE_VIDEO_AVC
)

data class AudioEncoderConfig(
    val sampleRate: Int = 44100,
    val channelCount: Int = 2,
    val bitrateKbps: Int = 128,
    val mimeType: String = MediaFormat.MIMETYPE_AUDIO_AAC
)
