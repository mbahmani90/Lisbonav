package com.majidbahmani.calypso.nfc

/**
 * Sends ISO 7816-4 APDUs to a contactless card and returns its raw response (data + status word).
 *
 * The only platform-specific part of the SDK: Android implements it with `IsoDep`, iOS later with
 * Core NFC, and tests with a scripted fake. Implementations may throw when the card leaves the field.
 */
interface CardTransport {
    suspend fun transceive(command: ByteArray): ByteArray
}
