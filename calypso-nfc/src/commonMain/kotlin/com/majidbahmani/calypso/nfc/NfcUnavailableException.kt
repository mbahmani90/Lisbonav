package com.majidbahmani.calypso.nfc

/** The device can't read cards right now: no NFC hardware, or NFC switched off by the user. */
class NfcUnavailableException(val reason: Reason) : Exception("NFC unavailable: $reason") {
    enum class Reason { NOT_SUPPORTED, DISABLED }
}
