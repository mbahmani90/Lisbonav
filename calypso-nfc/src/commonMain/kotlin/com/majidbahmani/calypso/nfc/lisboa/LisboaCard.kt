package com.majidbahmani.calypso.nfc.lisboa

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

/**
 * A Lisbon transit card (Navegante, formerly Lisboa Viva), decoded from a Calypso read.
 * Holder fields are personal data: show them to the card holder only, never log them.
 */
data class LisboaCard(
    /** The number engraved on the card (ICC file); null if the file isn't readable. */
    val engravedSerialNumber: Long?,
    /** Personalised cards only. */
    val holderName: String?,
    /** Personalised cards only. */
    val holderBirthDate: LocalDate?,
    val issueDate: LocalDate?,
    /** The card itself (not a pass) stops working after this date. */
    val validUntil: LocalDate?,
    /** Loaded passes and tickets, by slot. Empty slots are left out. */
    val contracts: List<LisboaContract>,
    /** The last validations, as stored on the card (newest first). */
    val trips: List<LisboaTrip>,
)

data class LisboaContract(
    /** 1-based contract slot on the card; trips refer to it. */
    val slot: Int,
    val provider: Int,
    val tariff: Int,
    val startDate: LocalDate?,
    /** Last day the pass is valid; null when the card doesn't say (e.g. pay-as-you-go). */
    val validUntil: LocalDate?,
    /** Stored value in euro cents, for pay-as-you-go (Zapping); null for passes. */
    val balanceCents: Int?,
) {
    /** The tariff when this app knows its name; null for other codes. */
    val knownTariff: LisboaTariff? get() = LisboaTariff.of(provider, tariff)
}

/** Tariffs with a known name (provider + tariff code, as stored on the card). */
enum class LisboaTariff(val provider: Int, val code: Int) {
    /** Pay-as-you-go stored value, accepted by most operators. */
    ZAPPING(provider = 31, code = 33592),
    NAVEGANTE_LISBOA(provider = 31, code = 906),
    ;

    companion object {
        fun of(provider: Int, code: Int): LisboaTariff? = entries.firstOrNull { it.provider == provider && it.code == code }
    }
}

data class LisboaTrip(
    /** Lisbon local time of the validation. */
    val time: LocalDateTime,
    val transition: Transition,
    /** The operator code; [operator] when it's a known one. */
    val provider: Int,
    val routeNumber: Int,
    val locationId: Int,
    /** Which contract slots paid for this trip. */
    val contractSlotsUsed: Set<Int>,
) {
    val operator: LisboaOperator? get() = LisboaOperator.of(provider)

    enum class Transition { TAP_ON, TRANSFER, TAP_OFF, OTHER }
}

enum class LisboaOperator(val code: Int) {
    CARRIS(1),
    METRO(2),
    CP(3),
    ;

    companion object {
        fun of(code: Int): LisboaOperator? = entries.firstOrNull { it.code == code }
    }
}
