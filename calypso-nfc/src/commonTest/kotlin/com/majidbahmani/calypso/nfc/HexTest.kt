package com.majidbahmani.calypso.nfc

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class HexTest {

    @Test
    fun roundTrip_ignoresSpacesAndCase() {
        assertEquals("00A40FFF", "00 a4 0f ff".hexToBytes().toHex())
    }

    @Test
    fun oddLength_throws() {
        assertFailsWith<IllegalArgumentException> { "ABC".hexToBytes() }
    }
}
