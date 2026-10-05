package com.majidbahmani.calypso.nfc

/** Upper-case hex without separators, e.g. `90 00` → "9000". */
fun ByteArray.toHex(): String = joinToString("") { byte -> (byte.toInt() and 0xFF).toString(16).padStart(2, '0') }.uppercase()

/** Parses hex like "00A4 0400" (spaces ignored). */
fun String.hexToBytes(): ByteArray {
    val hex = filterNot { it.isWhitespace() }
    require(hex.length % 2 == 0) { "Hex string must have an even number of digits: $this" }
    return ByteArray(hex.length / 2) { i -> hex.substring(i * 2, i * 2 + 2).toInt(16).toByte() }
}

/** Status word as 4 hex digits, e.g. 0x9000 → "9000". */
internal fun Int.toStatusHex(): String = toString(16).uppercase().padStart(4, '0')
