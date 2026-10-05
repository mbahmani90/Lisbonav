package com.majidbahmani.lisbonav.feature.transportcard.domain.model

/** What happens when a card is held to the phone. */
sealed interface CardRead {
    /** A card was detected; reading takes a moment, so the card must stay in place. */
    data object Started : CardRead

    data class Success(val card: TransportCard) : CardRead

    data class Failure(val reason: Reason) : CardRead

    /** The reason, not a message: the UI picks the text. */
    enum class Reason {
        /** The device can't read cards (no NFC, or not supported on this platform yet). */
        NFC_NOT_SUPPORTED,

        /** NFC is switched off in the system settings. */
        NFC_DISABLED,

        /** The card left the phone before the read finished. */
        CARD_REMOVED,

        /** A contactless card, but not a Lisbon Navegante card. */
        NOT_A_NAVEGANTE_CARD,
    }
}
