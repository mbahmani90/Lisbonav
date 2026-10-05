package com.majidbahmani.calypso.nfc

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class BitReaderTest {

    @Test
    fun readsFieldsAcrossByteBoundaries() {
        val bytes = BitWriter().write(5, 3).write(0x131, 12).write(9_000, 14).toByteArray(size = 4)
        val reader = BitReader(bytes)

        assertEquals(5, reader.readInt(3))
        assertEquals(0x131, reader.readInt(12))
        assertEquals(9_000, reader.readInt(14))
        assertEquals(29, reader.position)
    }

    @Test
    fun skipMovesThePosition() {
        val reader = BitReader(BitWriter().write(0, 7).write(1, 1).toByteArray(size = 1))
        reader.skip(7)
        assertEquals(1, reader.readInt(1))
    }

    @Test
    fun readingPastTheEnd_throws() {
        assertFailsWith<IllegalArgumentException> { BitReader(ByteArray(1)).read(9) }
    }
}
