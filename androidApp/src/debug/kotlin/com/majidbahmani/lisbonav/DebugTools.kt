package com.majidbahmani.lisbonav

import android.app.Application
import android.nfc.tech.IsoDep
import android.util.Log
import com.majidbahmani.calypso.nfc.CalypsoReader
import com.majidbahmani.calypso.nfc.IsoDepTransport
import com.majidbahmani.calypso.nfc.NfcTagReader
import com.majidbahmani.calypso.nfc.NfcUnavailableException
import com.majidbahmani.calypso.nfc.lisboa.LisboaCardParser
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Debug builds only (src/debug). While the app is open, every tapped card is read with
 * CalypsoReader.readRaw() and the raw dump is written to Logcat:
 *
 *     adb logcat -s CalypsoDump
 *
 * TEMPORARY: used to design the Calypso parser from a real card; removed when the card screen
 * exists (step 3). The dump can contain personal data: never add this to release builds.
 */
object DebugTools {

    private const val TAG = "CalypsoDump"

    fun install(application: Application) {
        val tagReader = NfcTagReader(application)
        if (!tagReader.isSupported) {
            Log.i(TAG, "No NFC on this device: card dump disabled")
            return
        }
        MainScope().launch {
            while (isActive) {
                try {
                    // Reader mode stays on while collecting, so the connection to the card survives the read.
                    tagReader.cardTaps().collect { isoDep -> dump(isoDep) }
                } catch (e: NfcUnavailableException) {
                    Log.w(TAG, "NFC unavailable (${e.reason}); retrying in 5 s")
                    delay(5_000)
                }
            }
        }
    }

    private suspend fun dump(isoDep: IsoDep) {
        Log.d(TAG, "Card tapped, reading…")
        try {
            IsoDepTransport(isoDep).use { transport ->
                val dump = CalypsoReader(transport).readRaw()
                dump.toDebugString().lines().filter { it.isNotBlank() }.forEach { Log.d(TAG, it) }
                // Decoded summary: presence of personal fields only, never their values.
                val card = LisboaCardParser.parse(dump)
                if (card == null) {
                    Log.d(TAG, "Parsed: not a Lisbon card")
                } else {
                    Log.d(TAG, "Parsed: serial=${card.engravedSerialNumber != null} name=${card.holderName != null} birthDate=${card.holderBirthDate != null}")
                    card.contracts.forEach { Log.d(TAG, "Parsed contract: slot ${it.slot} ${it.knownTariff ?: it.tariff} until ${it.validUntil} balance ${it.balanceCents}") }
                    card.trips.forEach { Log.d(TAG, "Parsed trip: ${it.time} ${it.operator ?: it.provider} ${it.transition} slots ${it.contractSlotsUsed}") }
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Usually TagLostException: the card was moved away too early.
            Log.w(TAG, "Read failed: ${e::class.simpleName}: ${e.message}")
        }
    }
}
