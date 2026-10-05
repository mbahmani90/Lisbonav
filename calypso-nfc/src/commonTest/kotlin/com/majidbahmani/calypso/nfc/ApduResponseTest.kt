package com.majidbahmani.calypso.nfc

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ApduResponseTest {

    @Test
    fun parse_splitsDataAndStatusWord() {
        val response = ApduResponse.parse("AABBCC9000".hexToBytes())

        assertEquals("AABBCC", response.data.toHex())
        assertEquals(0x9000, response.statusWord)
        assertTrue(response.isSuccess)
    }

    @Test
    fun parse_statusOnly() {
        val response = ApduResponse.parse("6A82".hexToBytes())

        assertEquals("", response.data.toHex())
        assertEquals(ApduResponse.SW_FILE_NOT_FOUND, response.statusWord)
        assertFalse(response.isSuccess)
    }

    @Test
    fun parse_tooShort_throws() {
        assertFailsWith<IllegalArgumentException> { ApduResponse.parse("90".hexToBytes()) }
    }

    @Test
    fun correctLength_onlyFor6Cxx() {
        assertEquals(0x1D, ApduResponse.parse("6C1D".hexToBytes()).correctLength)
        assertNull(ApduResponse.parse("9000".hexToBytes()).correctLength)
    }
}
