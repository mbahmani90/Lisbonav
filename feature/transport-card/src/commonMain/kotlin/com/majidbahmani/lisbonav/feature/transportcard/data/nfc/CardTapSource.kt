package com.majidbahmani.lisbonav.feature.transportcard.data.nfc

import com.majidbahmani.calypso.nfc.CardTransport
import kotlinx.coroutines.flow.Flow

/** A card held to the phone: talk to it with APDUs, then close it. */
internal interface TappedCard :
    CardTransport,
    AutoCloseable

/**
 * Where tapped cards come from: Android NFC reader mode, or a fake in tests.
 * Cards are emitted while collected; throws `NfcUnavailableException` without NFC.
 */
internal fun interface CardTapSource {
    fun taps(): Flow<TappedCard>
}
