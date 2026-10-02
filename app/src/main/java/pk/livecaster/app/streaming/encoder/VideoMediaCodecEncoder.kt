package pk.livecaster.app.streaming.encoder

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.util.Log
import pk.livecaster.app.streaming.rtmp.RtmpStreamSink
import java.nio.ByteBuffer

class VideoMediaCodecEncoder(
    private val rtmpSink: RtmpStreamSink,
    private val width: Int = 1280,
    private val height: Int = 720,
    private val fps: Int = 30,
    private val bitrateKbps: Int = 3500
) {
    private var mediaCodec: MediaCodec? = null
    private var isEncoding = false
    private var startTimeMs: Long = 0
    private var sequenceHeaderSent = false
    private var drainThread: Thread? = null
    private var keepAliveThread: Thread? = null
    private var lastFrameBytes: ByteArray? = null
    @Volatile
    private var lastFrameTimeMs: Long = 0

    fun start() {
        if (isEncoding) return

        try {
            val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height).apply {
                setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible)
                setInteger(MediaFormat.KEY_BIT_RATE, bitrateKbps * 1000)
                setInteger(MediaFormat.KEY_FRAME_RATE, fps)
                setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1) // 1 second keyframe interval for low-latency live
                try {
                    setInteger(MediaFormat.KEY_PROFILE, MediaCodecInfo.CodecProfileLevel.AVCProfileBaseline)
                    setInteger(MediaFormat.KEY_LEVEL, MediaCodecInfo.CodecProfileLevel.AVCLevel31)
                } catch (_: Exception) {}
            }

            mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC).apply {
                configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
                start()
            }

            isEncoding = true
            sequenceHeaderSent = false
            startTimeMs = System.currentTimeMillis()
            lastFrameTimeMs = startTimeMs

            startDrainThread()
            startKeepAliveThread()
            requestSyncFrame()
            Log.d(TAG, "Video H.264 MediaCodec started successfully: ${width}x${height} @ ${bitrateKbps}kbps")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start H.264 video encoder", e)
            stop()
        }
    }

    fun requestSyncFrame() {
        try {
            val params = android.os.Bundle().apply {
                putInt(MediaCodec.PARAMETER_KEY_REQUEST_SYNC_FRAME, 0)
            }
            mediaCodec?.setParameters(params)
        } catch (_: Exception) {}
    }

    fun encodeYuv(nv21OrYuv420: ByteArray) {
        lastFrameBytes = nv21OrYuv420
        lastFrameTimeMs = System.currentTimeMillis()
        encodeInternal(nv21OrYuv420)
    }

    private fun encodeInternal(nv21OrYuv420: ByteArray) {
        val codec = mediaCodec ?: return
        if (!isEncoding) return

        try {
            val inputBufferIndex = codec.dequeueInputBuffer(5000)
            if (inputBufferIndex >= 0) {
                val inputBuffer = codec.getInputBuffer(inputBufferIndex)
                inputBuffer?.clear()
                val copyLen = minOf(inputBuffer?.remaining() ?: 0, nv21OrYuv420.size)
                inputBuffer?.put(nv21OrYuv420, 0, copyLen)
                val ptsUs = (System.currentTimeMillis() - startTimeMs) * 1000
                codec.queueInputBuffer(inputBufferIndex, 0, copyLen, ptsUs, 0)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error queuing frame to MediaCodec", e)
        }
    }

    private fun startKeepAliveThread() {
        keepAliveThread = Thread({
            var lastSyncRequestMs = System.currentTimeMillis()
            while (isEncoding) {
                try {
                    Thread.sleep(100)
                    val now = System.currentTimeMillis()

                    // Facebook Live strictly requires an IDR keyframe at least every 2 seconds
                    if (now - lastSyncRequestMs >= 1900) {
                        requestSyncFrame()
                        lastSyncRequestMs = now
                    }

                    // If app is in background (e.g. user switched to Chrome) and camera paused
                    if (isEncoding && now - lastFrameTimeMs > 200) {
                        val frame = lastFrameBytes ?: createStandbyFrame()
                        encodeInternal(frame)
                        lastFrameTimeMs = now
                    }
                } catch (_: InterruptedException) {
                    break
                } catch (e: Exception) {
                    Log.e(TAG, "Keep-alive video loop error", e)
                }
            }
        }, "LiveCaster-VideoKeepAlive")
        keepAliveThread?.start()
    }

    private fun createStandbyFrame(): ByteArray {
        val ySize = width * height
        val uvSize = width * height / 2
        val standby = ByteArray(ySize + uvSize)
        // High contrast test luma for encoder
        for (i in 0 until ySize) {
            standby[i] = if ((i / width / 30) % 2 == 0) 180.toByte() else 40.toByte()
        }
        java.util.Arrays.fill(standby, ySize, standby.size, 128.toByte())
        return standby
    }

    private fun startDrainThread() {
        drainThread = Thread({
            val bufferInfo = MediaCodec.BufferInfo()
            val codec = mediaCodec ?: return@Thread

            while (isEncoding) {
                try {
                    val outputBufferIndex = codec.dequeueOutputBuffer(bufferInfo, 10000)
                    if (outputBufferIndex >= 0) {
                        val outputBuffer = codec.getOutputBuffer(outputBufferIndex)
                        if (outputBuffer != null && bufferInfo.size > 0) {
                            val data = ByteArray(bufferInfo.size)
                            outputBuffer.position(bufferInfo.offset)
                            outputBuffer.get(data)

                            val isConfig = (bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0
                            val isKeyframe = (bufferInfo.flags and MediaCodec.BUFFER_FLAG_KEY_FRAME) != 0

                            if (isConfig) {
                                // Extract SPS and PPS and send AVC sequence header
                                handleCodecConfig(data)
                            } else {
                                if (!sequenceHeaderSent) {
                                    // Try extracting from csd if not sent yet
                                    extractFromOutputFormat()
                                }
                                val timestampMs = bufferInfo.presentationTimeUs / 1000
                                sendNalus(data, isKeyframe, timestampMs)
                            }
                        }
                        codec.releaseOutputBuffer(outputBufferIndex, false)
                    } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                        extractFromOutputFormat()
                    }
                } catch (_: Exception) {}
            }
        }, "LiveCaster-VideoDrain")
        drainThread?.start()
    }

    private fun handleCodecConfig(configData: ByteArray) {
        val spsPps = parseSpsPps(configData)
        if (spsPps != null) {
            val (sps, pps) = spsPps
            rtmpSink.sendAvcSequenceHeader(sps, pps)
            sequenceHeaderSent = true
            Log.d(TAG, "AVC Sequence Header sent (SPS: ${sps.size}b, PPS: ${pps.size}b)")
        }
    }

    private fun extractFromOutputFormat() {
        val codec = mediaCodec ?: return
        try {
            val format = codec.outputFormat
            val csd0 = format.getByteBuffer("csd-0")
            val csd1 = format.getByteBuffer("csd-1")
            if (csd0 != null && csd1 != null) {
                val sps = ByteArray(csd0.remaining())
                csd0.get(sps)
                val pps = ByteArray(csd1.remaining())
                csd1.get(pps)

                val cleanSps = removeStartCode(sps)
                val cleanPps = removeStartCode(pps)
                rtmpSink.sendAvcSequenceHeader(cleanSps, cleanPps)
                sequenceHeaderSent = true
                Log.d(TAG, "Extracted SPS/PPS from outputFormat and sent sequence header")
            }
        } catch (_: Exception) {}
    }

    private fun sendNalus(data: ByteArray, isKeyframe: Boolean, timestampMs: Long) {
        val nalus = splitAnnexBNalus(data)
        for (nalu in nalus) {
            if (nalu.isNotEmpty()) {
                val nalType = nalu[0].toInt() and 0x1F
                if (nalType == 7 || nalType == 8) {
                    // SPS or PPS, already handled
                    continue
                }
                rtmpSink.sendVideoNalu(nalu, isKeyframe, timestampMs)
            }
        }
    }

    private fun splitAnnexBNalus(data: ByteArray): List<ByteArray> {
        val list = mutableListOf<ByteArray>()
        var start = -1
        var i = 0
        while (i < data.size - 3) {
            if (data[i] == 0.toByte() && data[i + 1] == 0.toByte() &&
                ((data[i + 2] == 1.toByte()) || (data[i + 2] == 0.toByte() && data[i + 3] == 1.toByte()))
            ) {
                if (start >= 0) {
                    val nalu = data.copyOfRange(start, i)
                    list.add(nalu)
                }
                val startCodeLen = if (data[i + 2] == 1.toByte()) 3 else 4
                start = i + startCodeLen
                i += startCodeLen
            } else {
                i++
            }
        }
        if (start in 0 until data.size) {
            list.add(data.copyOfRange(start, data.size))
        } else if (list.isEmpty() && data.isNotEmpty()) {
            list.add(data)
        }
        return list
    }

    private fun parseSpsPps(data: ByteArray): Pair<ByteArray, ByteArray>? {
        val nalus = splitAnnexBNalus(data)
        var sps: ByteArray? = null
        var pps: ByteArray? = null
        for (nalu in nalus) {
            if (nalu.isNotEmpty()) {
                val type = nalu[0].toInt() and 0x1F
                if (type == 7) sps = nalu
                if (type == 8) pps = nalu
            }
        }
        return if (sps != null && pps != null) Pair(sps, pps) else null
    }

    private fun removeStartCode(data: ByteArray): ByteArray {
        return if (data.size >= 4 && data[0] == 0.toByte() && data[1] == 0.toByte() && data[2] == 0.toByte() && data[3] == 1.toByte()) {
            data.copyOfRange(4, data.size)
        } else if (data.size >= 3 && data[0] == 0.toByte() && data[1] == 0.toByte() && data[2] == 1.toByte()) {
            data.copyOfRange(3, data.size)
        } else {
            data
        }
    }

    fun stop() {
        isEncoding = false
        keepAliveThread?.interrupt()
        keepAliveThread = null
        try {
            mediaCodec?.stop()
            mediaCodec?.release()
        } catch (_: Exception) {}
        mediaCodec = null
        drainThread?.interrupt()
        drainThread = null
        lastFrameBytes = null
    }

    companion object {
        private const val TAG = "VideoMediaCodecEncoder"
    }
}
