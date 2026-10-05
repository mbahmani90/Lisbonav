package com.majidbahmani.lisbonav.feature.transportcard.fake

import com.majidbahmani.lisbonav.analytics.Analytics
import com.majidbahmani.lisbonav.analytics.AnalyticsEvent
import com.majidbahmani.calypso.nfc.lisboa.LisboaCard
import com.majidbahmani.calypso.nfc.lisboa.LisboaContract
import com.majidbahmani.calypso.nfc.lisboa.LisboaTrip
import com.majidbahmani.lisbonav.feature.transportcard.data.nfc.CardTapSource
import com.majidbahmani.lisbonav.feature.transportcard.data.nfc.TappedCard
import com.majidbahmani.lisbonav.feature.transportcard.domain.model.CardRead
import com.majidbahmani.lisbonav.feature.transportcard.domain.repository.TransportCardReader
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

/** A card that records whether it was closed; it is never actually talked to (readDump is faked). */
internal class FakeTappedCard : TappedCard {
    var closed = false
        private set

    override suspend fun transceive(command: ByteArray): ByteArray = error("Not used: readDump is faked")

    override fun close() {
        closed = true
    }
}

internal class FakeCardTapSource(private val cards: List<FakeTappedCard> = emptyList(), private val error: Throwable? = null) : CardTapSource {
    override fun taps(): Flow<TappedCard> = if (error != null) flow { throw error } else cards.asFlow()
}

/** A decoded Lisbon card with made-up values. */
internal fun lisboaCard(
    contracts: List<LisboaContract> = listOf(
        LisboaContract(slot = 1, provider = 31, tariff = 33592, startDate = LocalDate(2025, 3, 1), validUntil = null, balanceCents = 102),
        LisboaContract(slot = 2, provider = 31, tariff = 900, startDate = LocalDate(2026, 9, 28), validUntil = LocalDate(2026, 10, 31), balanceCents = null),
    ),
    trips: List<LisboaTrip> = listOf(
        LisboaTrip(LocalDateTime(2026, 10, 2, 21, 7), LisboaTrip.Transition.TAP_OFF, provider = 2, routeNumber = 5, locationId = 17, contractSlotsUsed = setOf(2)),
        LisboaTrip(LocalDateTime(2026, 10, 2, 19, 41), LisboaTrip.Transition.TAP_ON, provider = 1, routeNumber = 735, locationId = 3, contractSlotsUsed = setOf(2)),
    ),
) = LisboaCard(
    engravedSerialNumber = 123_456_789L,
    holderName = "NOT USED",
    holderBirthDate = LocalDate(1990, 7, 15),
    issueDate = LocalDate(2020, 3, 1),
    validUntil = LocalDate(2029, 3, 1),
    contracts = contracts,
    trips = trips,
)

/** Emits the given reads, in order, when collected. */
internal class FakeTransportCardReader(private val reads: List<CardRead>) : TransportCardReader {
    override fun readCards(): Flow<CardRead> = reads.asFlow()
}

/** Records logged events, in order. */
internal class FakeAnalytics : Analytics {
    val events = mutableListOf<AnalyticsEvent>()
    override fun log(event: AnalyticsEvent) {
        events += event
    }
    override fun setCollectionEnabled(enabled: Boolean) = Unit
}
