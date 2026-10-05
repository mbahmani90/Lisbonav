package com.majidbahmani.calypso.nfc

/**
 * Reads the bit-packed fields of EN 1545 / Intercode records, most significant bit first.
 * Fields don't align with bytes: a 14-bit date can start in the middle of one.
 */
internal class BitReader(private val bytes: ByteArray) {

    var position: Int = 0
        private set

    /** The next [count] bits (1..63) as an unsigned number. */
    fun read(count: Int): Long {
        require(count in 1..63) { "Can read 1..63 bits at once, asked $count" }
        require(position + count <= bytes.size * 8) { "Reading $count bits at $position passes the end (${bytes.size * 8} bits)" }
        var value = 0L
        repeat(count) {
            val byte = bytes[position / 8].toInt() and 0xFF
            val bit = (byte shr (7 - position % 8)) and 1
            value = (value shl 1) or bit.toLong()
            position++
        }
        return value
    }

    fun readInt(count: Int): Int {
        require(count <= 31) { "Use read() for more than 31 bits" }
        return read(count).toInt()
    }

    fun skip(count: Int) {
        require(position + count <= bytes.size * 8) { "Skipping $count bits at $position passes the end" }
        position += count
    }
}
