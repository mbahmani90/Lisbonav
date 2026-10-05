package com.majidbahmani.lisbonav.feature.transportcard.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

/**
 * A Navegante card as the app shows it. Plain Kotlin: no NFC or SDK types.
 * Holder data is personal: shown to the card holder only, never logged or stored.
 */
data class TransportCard(
    /** The number engraved on the card; null if the card didn't give it. */
    val number: String?,
    /** Personalised cards only. */
    val holderBirthDate: LocalDate?,
    /** The card itself (not a pass) works until this date. */
    val validUntil: LocalDate?,
    val passes: List<TransportPass>,
    /** The last validations stored on the card, newest first. */
    val trips: List<CardTrip>,
)

data class TransportPass(
    val type: Type,
    /** The card's tariff code, shown for passes without a known name. */
    val tariffCode: Int,
    val startDate: LocalDate?,
    /** Last valid day; null for pay-as-you-go. */
    val validUntil: LocalDate?,
    /** Pay-as-you-go balance in euro cents; null for passes. */
    val balanceCents: Int?,
) {
    enum class Type { ZAPPING, NAVEGANTE_LISBOA, OTHER }
}

data class CardTrip(
    /** Lisbon local time. */
    val time: LocalDateTime,
    val operator: Operator,
    val kind: Kind,
    val routeNumber: Int,
) {
    enum class Operator { CARRIS, METRO, CP, OTHER }

    enum class Kind { TAP_ON, TRANSFER, TAP_OFF, OTHER }
}
