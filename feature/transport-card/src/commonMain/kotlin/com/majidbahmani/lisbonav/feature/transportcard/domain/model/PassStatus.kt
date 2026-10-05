package com.majidbahmani.lisbonav.feature.transportcard.domain.model

import kotlinx.datetime.LocalDate

/** Where a pass stands on a given day. */
enum class PassStatus {
    /** Valid today. */
    ACTIVE,

    /** Bought for a period that hasn't started yet (e.g. next month's pass). */
    NOT_STARTED,

    /** Pay-as-you-go balance (Zapping): no validity period. */
    STORED_VALUE,

    /** Past its last valid day. */
    EXPIRED,
}

/** The pass's status on [date]. A pass without dates counts as active: the card doesn't say otherwise. */
fun TransportPass.statusOn(date: LocalDate): PassStatus = when {
    balanceCents != null -> PassStatus.STORED_VALUE
    validUntil != null && validUntil < date -> PassStatus.EXPIRED
    startDate != null && startDate > date -> PassStatus.NOT_STARTED
    else -> PassStatus.ACTIVE
}
