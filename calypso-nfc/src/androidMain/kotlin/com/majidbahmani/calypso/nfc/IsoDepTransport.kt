package com.majidbahmani.calypso.nfc

import android.nfc.tech.IsoDep
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * [CardTransport] over Android's [IsoDep] (ISO 14443-4). Connects on first use; close it when done.
 * IsoDep calls block until the card answers, so they run on [ioDispatcher] (main-safe, doc 14).
 */
class IsoDepTransport(
    private val isoDep: IsoDep,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : CardTransport,
    AutoCloseable {

    override suspend fun transceive(command: ByteArray): ByteArray = withContext(ioDispatcher) {
        if (!isoDep.isConnected) {
            isoDep.connect()
            isoDep.timeout = TIMEOUT_MILLIS
        }
        isoDep.transceive(command)
    }

    override fun close() {
        // The card may already be gone; closing is best effort.
        runCatching { isoDep.close() }
    }

    private companion object {
        /** Calypso cards answer within a few hundred ms; the default (~1 s) is fine, a bit more is safer. */
        const val TIMEOUT_MILLIS = 2_000
    }
}
