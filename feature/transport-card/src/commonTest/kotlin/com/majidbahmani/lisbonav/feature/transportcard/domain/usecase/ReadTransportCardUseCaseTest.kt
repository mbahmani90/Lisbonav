package com.majidbahmani.lisbonav.feature.transportcard.domain.usecase

import com.majidbahmani.lisbonav.feature.transportcard.domain.model.CardRead
import com.majidbahmani.lisbonav.feature.transportcard.domain.model.TransportCard
import com.majidbahmani.lisbonav.feature.transportcard.domain.model.TransportPass
import com.majidbahmani.lisbonav.feature.transportcard.fake.FakeTransportCardReader
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class ReadTransportCardUseCaseTest {

    private val today = LocalDate(2026, 10, 5)

    private fun pass(name: Int, start: LocalDate? = null, until: LocalDate? = null, balance: Int? = null) =
        TransportPass(TransportPass.Type.OTHER, tariffCode = name, startDate = start, validUntil = until, balanceCents = balance)

    private fun card(passes: List<TransportPass>) =
        TransportCard(number = "1", holderBirthDate = null, validUntil = null, passes = passes, trips = emptyList(), readOn = today)

    private suspend fun orderOf(vararg passes: TransportPass): List<Int> {
        val reads = ReadTransportCardUseCase(FakeTransportCardReader(listOf(CardRead.Success(card(passes.toList()))))).invoke().toList()
        return (reads.single() as CardRead.Success).card.passes.map { it.tariffCode }
    }

    @Test
    fun passes_activeThenNotStartedThenStoredValueThenExpired() = runTest {
        val expired = pass(1, LocalDate(2026, 9, 1), LocalDate(2026, 9, 30))
        val zapping = pass(2, LocalDate(2025, 3, 1), balance = 102)
        val nextMonth = pass(3, LocalDate(2026, 11, 1), LocalDate(2026, 11, 30))
        val october = pass(4, LocalDate(2026, 9, 28), LocalDate(2026, 10, 31))

        assertEquals(listOf(4, 3, 2, 1), orderOf(expired, zapping, nextMonth, october))
    }

    @Test
    fun activePasses_endingSoonestFirst() = runTest {
        val untilDecember = pass(1, LocalDate(2026, 10, 1), LocalDate(2026, 12, 31))
        val untilOctober = pass(2, LocalDate(2026, 10, 1), LocalDate(2026, 10, 31))
        val noEnd = pass(3)

        assertEquals(listOf(2, 1, 3), orderOf(untilDecember, noEnd, untilOctober))
    }

    @Test
    fun otherReads_passThroughUnchanged() = runTest {
        val reads = listOf(CardRead.Started, CardRead.Failure(CardRead.Reason.CARD_REMOVED))

        assertEquals(reads, ReadTransportCardUseCase(FakeTransportCardReader(reads)).invoke().toList())
    }

    @Test
    fun tripsAndOtherCardFields_areKept() = runTest {
        val card = card(listOf(pass(1))).copy(number = "123", validUntil = LocalDate(2029, 3, 1))

        val read = ReadTransportCardUseCase(FakeTransportCardReader(listOf(CardRead.Success(card)))).invoke().toList().single()

        assertEquals(CardRead.Success(card), read)
    }
}
