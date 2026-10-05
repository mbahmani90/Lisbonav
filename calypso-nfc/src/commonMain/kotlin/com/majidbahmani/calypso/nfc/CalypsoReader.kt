package com.majidbahmani.calypso.nfc

/**
 * Reads a Calypso transport card through a [CardTransport].
 *
 * Read-only by design: writing (e.g. loading a pass) needs the operator's secure keys (SAM).
 */
class CalypsoReader(private val transport: CardTransport) {

    // Starts with the ISO class byte; switches to Calypso's legacy one if the card refuses it.
    private var cla: Byte = Apdu.CLA_ISO

    /**
     * Discovery read: selects the Calypso application, then reads every record of every
     * [CalypsoFile]. A file the card doesn't have or won't give is reported, not fatal.
     */
    suspend fun readRaw(): RawCalypsoDump {
        val select = send(Apdu.selectByAid(CALYPSO_AID))
        if (!select.isSuccess) return RawCalypsoDump(select, files = emptyList())
        return RawCalypsoDump(select, files = CalypsoFile.entries.map { readFile(it) })
    }

    private suspend fun readFile(file: CalypsoFile): RawFile {
        val records = mutableListOf<ByteArray>()
        for (record in 1..file.maxRecords) {
            val response = readRecord(file.sfi, record)
            if (!response.isSuccess) {
                return RawFile(file, records, stopStatus = response.statusWord.toStatusHex())
            }
            records += response.data
        }
        return RawFile(file, records, stopStatus = null)
    }

    private suspend fun readRecord(sfi: Int, record: Int): ApduResponse {
        var response = send(Apdu.readRecord(sfi, record, cla))
        if (response.statusWord == ApduResponse.SW_CLASS_NOT_SUPPORTED && cla != Apdu.CLA_CALYPSO) {
            // Older Calypso cards only answer to class 94; keep it for the following commands.
            cla = Apdu.CLA_CALYPSO
            response = send(Apdu.readRecord(sfi, record, cla))
        }
        response.correctLength?.let { length ->
            // The card asks for the exact record length (6C XX): repeat with it.
            response = send(Apdu.readRecord(sfi, record, cla, expectedLength = length))
        }
        return response
    }

    private suspend fun send(command: ByteArray): ApduResponse = ApduResponse.parse(transport.transceive(command))

    companion object {
        /** AID of the Calypso transport application: "1TIC.ICA" in ASCII. */
        val CALYPSO_AID: ByteArray = "315449432E494341".hexToBytes()
    }
}
