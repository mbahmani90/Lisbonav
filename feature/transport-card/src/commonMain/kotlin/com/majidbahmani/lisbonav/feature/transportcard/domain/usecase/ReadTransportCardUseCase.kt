package com.majidbahmani.lisbonav.feature.transportcard.domain.usecase

import com.majidbahmani.lisbonav.feature.transportcard.domain.model.CardRead
import com.majidbahmani.lisbonav.feature.transportcard.domain.model.PassStatus
import com.majidbahmani.lisbonav.feature.transportcard.domain.model.TransportPass
import com.majidbahmani.lisbonav.feature.transportcard.domain.model.statusOn
import com.majidbahmani.lisbonav.feature.transportcard.domain.repository.TransportCardReader
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate

/**
 * Reads tapped cards while collected (see [TransportCardReader.readCards]).
 *
 * Business rule: passes come in the order a traveller needs them, by status on the day of the
 * read: active (the one ending soonest first), not started yet, pay-as-you-go balance, expired.
 */
class ReadTransportCardUseCase(
    private val reader: TransportCardReader,
) {
    operator fun invoke(): Flow<CardRead> = reader.readCards().map { read ->
        when (read) {
            is CardRead.Success -> CardRead.Success(
                read.card.copy(passes = read.card.passes.sortedForDisplay(read.card.readOn)),
            )
            else -> read
        }
    }

    private fun List<TransportPass>.sortedForDisplay(today: LocalDate): List<TransportPass> =
        // Stable sort: passes that compare equal keep the card's order.
        sortedWith(
            compareBy<TransportPass> { STATUS_ORDER.indexOf(it.statusOn(today)) }
                .thenBy(nullsLast()) { it.validUntil },
        )

    private companion object {
        val STATUS_ORDER = listOf(PassStatus.ACTIVE, PassStatus.NOT_STARTED, PassStatus.STORED_VALUE, PassStatus.EXPIRED)
    }
}
