package com.majidbahmani.lisbonav.feature.transportcard.data.nfc

import android.nfc.tech.IsoDep
import com.majidbahmani.calypso.nfc.IsoDepTransport
import com.majidbahmani.calypso.nfc.NfcTagReader
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Taps from Android NFC reader mode, which stays on while [taps] is collected. */
internal class AndroidCardTapSource(private val tagReader: NfcTagReader) : CardTapSource {

    override fun taps(): Flow<TappedCard> = tagReader.cardTaps().map { isoDep -> IsoDepTappedCard(isoDep) }

    private class IsoDepTappedCard(isoDep: IsoDep) : TappedCard {
        private val transport = IsoDepTransport(isoDep)

        override suspend fun transceive(command: ByteArray): ByteArray = transport.transceive(command)

        override fun close() = transport.close()
    }
}
