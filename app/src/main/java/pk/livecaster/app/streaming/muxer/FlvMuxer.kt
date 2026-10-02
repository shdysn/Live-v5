package pk.livecaster.app.streaming.muxer

import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

class FlvMuxer {
    fun createFlvHeader(hasAudio: Boolean = true, hasVideo: Boolean = true): ByteArray {
        val header = ByteArray(9)
        header[0] = 'F'.code.toByte()
        header[1] = 'L'.code.toByte()
        header[2] = 'V'.code.toByte()
        header[3] = 0x01 // Version 1

        var typeFlags = 0
        if (hasAudio) typeFlags = typeFlags or 0x04
        if (hasVideo) typeFlags = typeFlags or 0x01
        header[4] = typeFlags.toByte()

        // Data offset (9 bytes)
        ByteBuffer.wrap(header, 5, 4).putInt(9)
        return header
    }

    fun createFlvTag(tagType: Int, data: ByteArray, timestampMs: Long): ByteArray {
        val out = ByteArrayOutputStream()
        out.write(tagType) // 8 for Audio, 9 for Video, 18 for Script

        // Data size (3 bytes)
        val size = data.size
        out.write((size shr 16) and 0xFF)
        out.write((size shr 8) and 0xFF)
        out.write(size and 0xFF)

        // Timestamp (3 bytes lower, 1 byte extended)
        out.write((timestampMs shr 16).toInt() and 0xFF)
        out.write((timestampMs shr 8).toInt() and 0xFF)
        out.write(timestampMs.toInt() and 0xFF)
        out.write((timestampMs shr 24).toInt() and 0xFF)

        // Stream ID (3 bytes, always 0)
        out.write(0)
        out.write(0)
        out.write(0)

        // Payload
        out.write(data)

        // Previous Tag Size (4 bytes = 11 + size)
        val tagSize = 11 + size
        out.write((tagSize shr 24) and 0xFF)
        out.write((tagSize shr 16) and 0xFF)
        out.write((tagSize shr 8) and 0xFF)
        out.write(tagSize and 0xFF)

        return out.toByteArray()
    }
}
