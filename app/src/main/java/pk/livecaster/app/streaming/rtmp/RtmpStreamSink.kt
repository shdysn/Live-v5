package pk.livecaster.app.streaming.rtmp

import android.util.Log

interface RtmpStreamSink {
    fun sendAvcSequenceHeader(sps: ByteArray, pps: ByteArray)
    fun sendVideoNalu(nalu: ByteArray, isKeyframe: Boolean, timestampMs: Long)
    fun sendAacSequenceHeader(sampleRate: Int = 44100, channelCount: Int = 2)
    fun sendAudioFrame(data: ByteArray, offset: Int, size: Int, timestampMs: Long)
}

class MultiRtmpDispatcher(private val sinks: List<RtmpStreamSink>) : RtmpStreamSink {

    override fun sendAvcSequenceHeader(sps: ByteArray, pps: ByteArray) {
        for (sink in sinks) {
            try {
                sink.sendAvcSequenceHeader(sps, pps)
            } catch (e: Exception) {
                Log.e(TAG, "Error sending AVC sequence header to sink", e)
            }
        }
    }

    override fun sendVideoNalu(nalu: ByteArray, isKeyframe: Boolean, timestampMs: Long) {
        for (sink in sinks) {
            try {
                sink.sendVideoNalu(nalu, isKeyframe, timestampMs)
            } catch (e: Exception) {
                Log.e(TAG, "Error sending video NALU to sink", e)
            }
        }
    }

    override fun sendAacSequenceHeader(sampleRate: Int, channelCount: Int) {
        for (sink in sinks) {
            try {
                sink.sendAacSequenceHeader(sampleRate, channelCount)
            } catch (e: Exception) {
                Log.e(TAG, "Error sending AAC sequence header to sink", e)
            }
        }
    }

    override fun sendAudioFrame(data: ByteArray, offset: Int, size: Int, timestampMs: Long) {
        for (sink in sinks) {
            try {
                sink.sendAudioFrame(data, offset, size, timestampMs)
            } catch (e: Exception) {
                Log.e(TAG, "Error sending audio frame to sink", e)
            }
        }
    }

    companion object {
        private const val TAG = "MultiRtmpDispatcher"
    }
}
