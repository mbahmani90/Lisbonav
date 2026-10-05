package com.majidbahmani.calypso.nfc

/** A card's answer: response data followed by the 2-byte status word (SW1 SW2). */
class ApduResponse(val data: ByteArray, val statusWord: Int) {

    /** `90 00`: the command succeeded. */
    val isSuccess: Boolean get() = statusWord == SW_SUCCESS

    override fun toString(): String = "${data.toHex()} [${statusWord.toStatusHex()}]"

    companion object {
        const val SW_SUCCESS = 0x9000

        /** File not found. */
        const val SW_FILE_NOT_FOUND = 0x6A82

        /** Record not found: read past the last record of a file. */
        const val SW_RECORD_NOT_FOUND = 0x6A83

        /** Instruction class (CLA) not supported: the card wants another class byte. */
        const val SW_CLASS_NOT_SUPPORTED = 0x6E00

        fun parse(raw: ByteArray): ApduResponse {
            require(raw.size >= 2) { "A response needs at least the 2 status bytes, got ${raw.size}" }
            val sw = ((raw[raw.size - 2].toInt() and 0xFF) shl 8) or (raw[raw.size - 1].toInt() and 0xFF)
            return ApduResponse(data = raw.copyOfRange(0, raw.size - 2), statusWord = sw)
        }
    }
}

/** `6C XX`: wrong expected length; XX is the length the card wants. */
internal val ApduResponse.correctLength: Int?
    get() = if (statusWord shr 8 == 0x6C) statusWord and 0xFF else null
