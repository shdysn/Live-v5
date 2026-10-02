package pk.livecaster.app.streaming.encoder

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaRecorder
import android.util.Log
import pk.livecaster.app.streaming.rtmp.RtmpStreamSink
import java.nio.ByteBuffer

class AudioMediaCodecEncoder(
    private val rtmpSink: RtmpStreamSink,
    private val sampleRate: Int = 44100,
    private val channelCount: Int = 2,
    private val bitrate: Int = 128000
) {
    private var mediaCodec: MediaCodec? = null
    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private var recordThread: Thread? = null
    private var encodeThread: Thread? = null
    private var startTimeMs: Long = 0

    @SuppressLint("MissingPermission")
    fun start() {
        if (isRecording) return

        try {
            val channelConfig = if (channelCount == 1) AudioFormat.CHANNEL_IN_MONO else AudioFormat.CHANNEL_IN_STEREO
            val minBufferSize = AudioRecord.getMinBufferSize(
                sampleRate,
                channelConfig,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val bufferSize = maxOf(minBufferSize, 4096)

            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
            )

            val format = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC, sampleRate, channelCount).apply {
                setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
                setInteger(MediaFormat.KEY_BIT_RATE, bitrate)
                setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 8192)
            }

            mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC).apply {
                configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
                start()
            }

            // Send AAC Sequence Header to RTMP
            rtmpSink.sendAacSequenceHeader(sampleRate, channelCount)

            isRecording = true
            startTimeMs = System.currentTimeMillis()
            audioRecord?.startRecording()

            startAudioPipeline(bufferSize)
            Log.d(TAG, "Audio encoder successfully started")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start audio encoder", e)
            stop()
        }
    }

    private fun startAudioPipeline(bufferSize: Int) {
        val codec = mediaCodec ?: return
        val record = audioRecord ?: return

        recordThread = Thread({
            val audioBuffer = ByteArray(bufferSize)
            while (isRecording) {
                val readBytes = record.read(audioBuffer, 0, audioBuffer.size)
                if (readBytes > 0) {
                    val inputBufferIndex = codec.dequeueInputBuffer(10000)
                    if (inputBufferIndex >= 0) {
                        val inputBuffer: ByteBuffer? = codec.getInputBuffer(inputBufferIndex)
                        inputBuffer?.clear()
                        inputBuffer?.put(audioBuffer, 0, readBytes)
                        val ptsUs = (System.currentTimeMillis() - startTimeMs) * 1000
                        codec.queueInputBuffer(inputBufferIndex, 0, readBytes, ptsUs, 0)
                    }
                }
            }
        }, "LiveCaster-AudioRecord")

        encodeThread = Thread({
            val bufferInfo = MediaCodec.BufferInfo()
            while (isRecording) {
                val outputBufferIndex = codec.dequeueOutputBuffer(bufferInfo, 10000)
                if (outputBufferIndex >= 0) {
                    val outputBuffer = codec.getOutputBuffer(outputBufferIndex)
                    if (outputBuffer != null && bufferInfo.size > 0) {
                        // Skip codec config as sequence header already sent
                        if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) == 0) {
                            val data = ByteArray(bufferInfo.size)
                            outputBuffer.position(bufferInfo.offset)
                            outputBuffer.get(data)
                            val timestampMs = bufferInfo.presentationTimeUs / 1000
                            rtmpSink.sendAudioFrame(data, 0, data.size, timestampMs)
                        }
                    }
                    codec.releaseOutputBuffer(outputBufferIndex, false)
                }
            }
        }, "LiveCaster-AudioEncode")

        recordThread?.start()
        encodeThread?.start()
    }

    fun stop() {
        isRecording = false
        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (_: Exception) {}
        audioRecord = null

        try {
            mediaCodec?.stop()
            mediaCodec?.release()
        } catch (_: Exception) {}
        mediaCodec = null

        recordThread?.interrupt()
        encodeThread?.interrupt()
        recordThread = null
        encodeThread = null
    }

    companion object {
        private const val TAG = "AudioMediaCodecEncoder"
    }
}
