package pk.livecaster.app.streaming.rtmp

import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.nio.ByteBuffer

class AmfWriter(private val out: OutputStream = ByteArrayOutputStream()) {

    fun writeNumber(value: Double): AmfWriter {
        out.write(AMF_NUMBER)
        val buf = ByteBuffer.allocate(8)
        buf.putDouble(value)
        out.write(buf.array())
        return this
    }

    fun writeBoolean(value: Boolean): AmfWriter {
        out.write(AMF_BOOLEAN)
        out.write(if (value) 1 else 0)
        return this
    }

    fun writeString(value: String): AmfWriter {
        out.write(AMF_STRING)
        writeShortString(value)
        return this
    }

    fun writeShortString(value: String): AmfWriter {
        val bytes = value.toByteArray(Charsets.UTF_8)
        out.write((bytes.size shr 8) and 0xFF)
        out.write(bytes.size and 0xFF)
        out.write(bytes)
        return this
    }

    fun writeNull(): AmfWriter {
        out.write(AMF_NULL)
        return this
    }

    fun writeObject(properties: Map<String, Any>): AmfWriter {
        out.write(AMF_OBJECT)
        for ((key, value) in properties) {
            writeShortString(key)
            writeValue(value)
        }
        // Object end marker: empty string (0x00 0x00) + 0x09
        out.write(0x00)
        out.write(0x00)
        out.write(AMF_OBJECT_END)
        return this
    }

    fun writeEcmaArray(properties: Map<String, Any>): AmfWriter {
        out.write(AMF_ECMA_ARRAY)
        // 4 bytes count
        val count = properties.size
        out.write((count shr 24) and 0xFF)
        out.write((count shr 16) and 0xFF)
        out.write((count shr 8) and 0xFF)
        out.write(count and 0xFF)

        for ((key, value) in properties) {
            writeShortString(key)
            writeValue(value)
        }
        // End marker
        out.write(0x00)
        out.write(0x00)
        out.write(AMF_OBJECT_END)
        return this
    }

    private fun writeValue(value: Any) {
        when (value) {
            is Double -> writeNumber(value)
            is Number -> writeNumber(value.toDouble())
            is String -> writeString(value)
            is Boolean -> writeBoolean(value)
            is Map<*, *> -> {
                @Suppress("UNCHECKED_CAST")
                writeObject(value as Map<String, Any>)
            }
            else -> writeNull()
        }
    }

    fun toByteArray(): ByteArray {
        return (out as? ByteArrayOutputStream)?.toByteArray() ?: ByteArray(0)
    }

    companion object {
        const val AMF_NUMBER = 0x00
        const val AMF_BOOLEAN = 0x01
        const val AMF_STRING = 0x02
        const val AMF_OBJECT = 0x03
        const val AMF_NULL = 0x05
        const val AMF_ECMA_ARRAY = 0x08
        const val AMF_OBJECT_END = 0x09
    }
}
