package com.majidbahmani.calypso.nfc

/** Builds bit-packed test records, most significant bit first (the inverse of BitReader). */
class BitWriter {
    private val bits = StringBuilder()

    fun write(value: Long, count: Int): BitWriter = apply {
        bits.append(value.toString(2).padStart(count, '0').takeLast(count))
    }

    fun write(value: Int, count: Int): BitWriter = write(value.toLong(), count)

    /** The bits so far, zero-padded to [size] bytes (Calypso records are 29 bytes). */
    fun toByteArray(size: Int = 29): ByteArray {
        val padded = bits.toString().padEnd(size * 8, '0')
        return ByteArray(size) { i -> padded.substring(i * 8, i * 8 + 8).toInt(2).toByte() }
    }
}
