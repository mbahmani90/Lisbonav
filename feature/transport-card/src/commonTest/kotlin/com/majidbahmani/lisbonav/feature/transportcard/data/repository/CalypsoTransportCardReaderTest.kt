package com.majidbahmani.lisbonav.feature.transportcard.data.repository

import com.majidbahmani.calypso.nfc.ApduResponse
import com.majidbahmani.calypso.nfc.NfcUnavailableException
import com.majidbahmani.calypso.nfc.RawCalypsoDump
import com.majidbahmani.lisbonav.feature.transportcard.data.mapper.toDomain
import com.majidbahmani.lisbonav.feature.transportcard.domain.model.CardRead
import com.majidbahmani.lisbonav.feature.transportcard.domain.model.CardRead.Reason
import com.majidbahmani.lisbonav.feature.transportcard.fake.FakeCardTapSource
import com.majidbahmani.lisbonav.feature.transportcard.fake.FakeTappedCard
import com.majidbahmani.lisbonav.feature.transportcard.fake.lisboaCard
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CalypsoTransportCardReaderTest {

    private val today = LocalDate(2026, 10, 5)
    private val dump = RawCalypsoDump(ApduResponse(ByteArray(0), ApduResponse.SW_SUCCESS), files = emptyList())

    private fun reader(
        source: FakeCardTapSource,
        readDump: suspend () -> RawCalypsoDump = { dump },
        parse: (RawCalypsoDump) -> com.majidbahmani.calypso.nfc.lisboa.LisboaCard? = { lisboaCard() },
    ) = CalypsoTransportCardReader(source, readDump = { readDump() }, parse = parse, today = { today })

    @Test
    fun tap_emitsStartedThenTheCard_andClosesIt() = runTest {
        val card = FakeTappedCard()

        val reads = reader(FakeCardTapSource(listOf(card))).readCards().toList()

        assertEquals(listOf(CardRead.Started, CardRead.Success(lisboaCard().toDomain(readOn = today))), reads)
        assertTrue(card.closed)
    }

    @Test
    fun everyTap_isRead() = runTest {
        val reads = reader(FakeCardTapSource(listOf(FakeTappedCard(), FakeTappedCard()))).readCards().toList()

        assertEquals(4, reads.size)
        assertEquals(2, reads.count { it is CardRead.Success })
    }

    @Test
    fun notALisbonCard_isReported() = runTest {
        val reads = reader(FakeCardTapSource(listOf(FakeTappedCard())), parse = { null }).readCards().toList()

        assertEquals(CardRead.Failure(Reason.NOT_A_NAVEGANTE_CARD), reads.last())
    }

    @Test
    fun recordsThatDontFitTheLayout_areNotALisbonCard() = runTest {
        val reads = reader(FakeCardTapSource(listOf(FakeTappedCard())), parse = { throw IllegalArgumentException("short record") })
            .readCards().toList()

        assertEquals(CardRead.Failure(Reason.NOT_A_NAVEGANTE_CARD), reads.last())
    }

    @Test
    fun cardRemovedMidRead_isReported_andCardClosed() = runTest {
        val card = FakeTappedCard()
        val reads = reader(FakeCardTapSource(listOf(card)), readDump = { throw RuntimeException("Tag was lost") })
            .readCards().toList()

        assertEquals(listOf(CardRead.Started, CardRead.Failure(Reason.CARD_REMOVED)), reads)
        assertTrue(card.closed)
    }

    @Test
    fun nfcDisabled_reportsOnceAndCompletes() = runTest {
        val reads = reader(FakeCardTapSource(error = NfcUnavailableException(NfcUnavailableException.Reason.DISABLED)))
            .readCards().toList()

        assertEquals(listOf(CardRead.Failure(Reason.NFC_DISABLED)), reads)
    }

    @Test
    fun noNfc_reportsNotSupported() = runTest {
        val reads = reader(FakeCardTapSource(error = NfcUnavailableException(NfcUnavailableException.Reason.NOT_SUPPORTED)))
            .readCards().toList()

        assertEquals(listOf(CardRead.Failure(Reason.NFC_NOT_SUPPORTED)), reads)
    }
}
