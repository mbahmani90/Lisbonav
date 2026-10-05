package com.majidbahmani.calypso.nfc

/**
 * Everything a discovery read got from the card, undecoded: the SELECT answer and, per file,
 * the records read and why reading stopped. Used to design the parser from real data.
 *
 * Contains the card's raw data, possibly personal: show it only to the card holder; never log
 * it outside debug builds.
 */
class RawCalypsoDump(
    val selectResponse: ApduResponse,
    val files: List<RawFile>,
) {
    /** False when the card has no Calypso application (another kind of card). */
    val isCalypso: Boolean get() = selectResponse.isSuccess

    /** The records read from [file]; empty when the file was missing or unreadable. */
    fun records(file: CalypsoFile): List<ByteArray> = files.firstOrNull { it.file == file }?.records.orEmpty()

    fun toDebugString(): String = buildString {
        appendLine("SELECT 1TIC.ICA → $selectResponse")
        files.forEach { file ->
            appendLine(
                "${file.file} (SFI ${file.file.sfi.toString(
                    16,
                ).uppercase()}): ${file.records.size} record(s), stop ${file.stopStatus}",
            )
            file.records.forEachIndexed { i, record -> appendLine("  #${i + 1} ${record.toHex()}") }
        }
    }
}

/**
 * One file's records. [stopStatus] is the status word that ended the read, e.g. 6A83 after the
 * last record or 6A82 when the file doesn't exist; null when [CalypsoFile.maxRecords] were read.
 */
class RawFile(
    val file: CalypsoFile,
    val records: List<ByteArray>,
    val stopStatus: String?,
)
