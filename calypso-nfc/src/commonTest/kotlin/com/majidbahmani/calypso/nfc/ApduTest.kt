package com.majidbahmani.calypso.nfc

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ApduTest {

    @Test
    fun selectByAid_calypsoApplication() {
        // CLA 00, INS A4 (SELECT), P1 04 (by name), P2 00, Lc 08, "1TIC.ICA", Le 00
        assertEquals("00A4040008315449432E49434100", Apdu.selectByAid(CalypsoReader.CALYPSO_AID).toHex())
    }

    @Test
    fun readRecord_encodesSfiInP2() {
        // P2 = SFI 07 << 3 | 100b = 3C; record 1; Le 00
        assertEquals("00B2013C00", Apdu.readRecord(sfi = 0x07, record = 1).toHex())
        // SFI 09, record 2 → P2 4C
        assertEquals("00B2024C00", Apdu.readRecord(sfi = 0x09, record = 2).toHex())
    }

    @Test
    fun readRecord_calypsoClassAndExpectedLength() {
        assertEquals("94B2013C1D", Apdu.readRecord(sfi = 0x07, record = 1, cla = Apdu.CLA_CALYPSO, expectedLength = 0x1D).toHex())
    }

    @Test
    fun readRecord_rejectsInvalidSfiAndRecord() {
        assertFailsWith<IllegalArgumentException> { Apdu.readRecord(sfi = 0, record = 1) }
        assertFailsWith<IllegalArgumentException> { Apdu.readRecord(sfi = 31, record = 1) }
        assertFailsWith<IllegalArgumentException> { Apdu.readRecord(sfi = 7, record = 0) }
    }
}
