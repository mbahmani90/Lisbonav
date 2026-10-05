package com.majidbahmani.calypso.nfc

/** ISO 7816-4 command APDUs used to read a Calypso card. */
object Apdu {

    /** Standard ISO class byte. */
    const val CLA_ISO: Byte = 0x00

    /** Calypso's legacy class byte, needed by older (revision 1/2) cards. */
    const val CLA_CALYPSO: Byte = 0x94.toByte()

    /** SELECT by application identifier (AID): `00 A4 04 00 Lc AID 00`. */
    fun selectByAid(aid: ByteArray): ByteArray =
        byteArrayOf(CLA_ISO, 0xA4.toByte(), 0x04, 0x00, aid.size.toByte()) + aid + byteArrayOf(0x00)

    /**
     * READ RECORD of one record of the file with short file identifier [sfi]:
     * `CLA B2 record P2 Le`, where P2 = SFI << 3 | 0b100 ("read record number P1").
     * [expectedLength] 0 means "as many bytes as the record has".
     */
    fun readRecord(sfi: Int, record: Int, cla: Byte = CLA_ISO, expectedLength: Int = 0): ByteArray {
        require(sfi in 1..30) { "SFI must be 1..30, was $sfi" }
        require(record in 1..255) { "Record number must be 1..255, was $record" }
        return byteArrayOf(cla, 0xB2.toByte(), record.toByte(), ((sfi shl 3) or 0x04).toByte(), expectedLength.toByte())
    }
}
