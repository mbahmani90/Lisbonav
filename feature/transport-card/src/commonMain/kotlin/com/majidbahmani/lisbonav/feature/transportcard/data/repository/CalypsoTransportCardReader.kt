package com.majidbahmani.lisbonav.feature.transportcard.data.repository

import com.majidbahmani.calypso.nfc.CalypsoReader
import com.majidbahmani.calypso.nfc.CardTransport
import com.majidbahmani.calypso.nfc.NfcUnavailableException
import com.majidbahmani.calypso.nfc.RawCalypsoDump
import com.majidbahmani.calypso.nfc.lisboa.LisboaCard
import com.majidbahmani.calypso.nfc.lisboa.LisboaCardParser
import com.majidbahmani.lisbonav.feature.transportcard.data.mapper.toDomain
import com.majidbahmani.lisbonav.feature.transportcard.data.nfc.CardTapSource
import com.majidbahmani.lisbonav.feature.transportcard.data.nfc.TappedCard
import com.majidbahmani.lisbonav.feature.transportcard.domain.model.CardRead
import com.majidbahmani.lisbonav.feature.transportcard.domain.model.CardRead.Reason
import com.majidbahmani.lisbonav.feature.transportcard.domain.repository.TransportCardReader
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Clock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/** Card dates are Lisbon local dates, so "today" is too. */
private val LISBON = TimeZone.of("Europe/Lisbon")

/**
 * Reads tapped cards with the calypso-nfc SDK: raw read → Lisbon parser → domain model.
 * [readDump] and [parse] are the SDK by default; tests replace them, and [today] (Lisbon date).
 */
internal class CalypsoTransportCardReader(
    private val cardTaps: CardTapSource,
    private val readDump: suspend (CardTransport) -> RawCalypsoDump = { CalypsoReader(it).readRaw() },
    private val parse: (RawCalypsoDump) -> LisboaCard? = LisboaCardParser::parse,
    private val today: () -> LocalDate = { Clock.System.todayIn(LISBON) },
) : TransportCardReader {

    override fun readCards(): Flow<CardRead> = flow {
        try {
            // Sequential: the next card is only handled after this one is read and closed.
            cardTaps.taps().collect { card ->
                emit(CardRead.Started)
                emit(read(card))
            }
        } catch (e: NfcUnavailableException) {
            emit(
                CardRead.Failure(
                    when (e.reason) {
                        NfcUnavailableException.Reason.NOT_SUPPORTED -> Reason.NFC_NOT_SUPPORTED
                        NfcUnavailableException.Reason.DISABLED -> Reason.NFC_DISABLED
                    },
                ),
            )
        }
    }

    private suspend fun read(card: TappedCard): CardRead {
        val dump = try {
            card.use { readDump(it) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // The radio connection broke mid-read: on Android a TagLostException (an IOException).
            return CardRead.Failure(Reason.CARD_REMOVED)
        }
        return try {
            parse(dump)?.let { CardRead.Success(it.toDomain(readOn = today())) }
                ?: CardRead.Failure(Reason.NOT_A_NAVEGANTE_CARD)
        } catch (e: IllegalArgumentException) {
            // Records shorter or different than the Lisbon layout: some other Calypso card.
            CardRead.Failure(Reason.NOT_A_NAVEGANTE_CARD)
        }
    }
}
