package com.majidbahmani.calypso.nfc

/**
 * The usual files of a Calypso transport application (Intercode / EN 1545 layout),
 * by short file identifier. Which ones a card has and lets us read is checked per card.
 */
enum class CalypsoFile(val sfi: Int, val maxRecords: Int) {
    ENVIRONMENT_HOLDER(sfi = 0x07, maxRecords = 1),
    EVENT_LOG(sfi = 0x08, maxRecords = 3),
    CONTRACTS(sfi = 0x09, maxRecords = 8),
    COUNTERS(sfi = 0x19, maxRecords = 1),
    SPECIAL_EVENTS(sfi = 0x1D, maxRecords = 3),
    CONTRACT_LIST(sfi = 0x1E, maxRecords = 1),
}
