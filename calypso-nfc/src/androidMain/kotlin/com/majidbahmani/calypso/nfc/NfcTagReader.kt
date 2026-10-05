package com.majidbahmani.calypso.nfc

import android.app.Activity
import android.app.Application
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.lang.ref.WeakReference
import java.util.concurrent.CopyOnWriteArraySet

/**
 * Cards tapped on the phone, as ISO 14443-4 [IsoDep] (Calypso, DESFire, EMV, …), using NFC reader mode.
 *
 * Reader mode stays on while [cardTaps] is collected: turning it off resets the NFC radio and
 * would cut the connection to a card that is still being read. Reader mode only works for a
 * resumed Activity, so this follows the app's activities through
 * [Application.ActivityLifecycleCallbacks] (weak reference, no leak). Create it once, in
 * `Application.onCreate`.
 */
class NfcTagReader(application: Application) {

    private val adapter: NfcAdapter? = NfcAdapter.getDefaultAdapter(application)
    private val mainHandler = Handler(Looper.getMainLooper())

    /** One per active collector; called on a binder thread. */
    private val listeners = CopyOnWriteArraySet<(IsoDep) -> Unit>()

    // Only touched on the main thread.
    private var resumedActivity: WeakReference<Activity>? = null

    init {
        application.registerActivityLifecycleCallbacks(
            object : ActivityLifecycleCallbacksAdapter() {
                override fun onActivityResumed(activity: Activity) {
                    resumedActivity = WeakReference(activity)
                    if (listeners.isNotEmpty()) enableReaderMode(activity)
                }

                override fun onActivityPaused(activity: Activity) {
                    if (resumedActivity?.get() === activity) {
                        disableReaderMode(activity)
                        resumedActivity = null
                    }
                }
            },
        )
    }

    val isSupported: Boolean get() = adapter != null

    /**
     * Emits each tapped card (not yet connected). Read the card inside `collect`: reader mode stays
     * on until collection stops. Fails with [NfcUnavailableException] without NFC.
     */
    fun cardTaps(): Flow<IsoDep> = callbackFlow {
        val adapter = adapter ?: throw NfcUnavailableException(NfcUnavailableException.Reason.NOT_SUPPORTED)
        if (!adapter.isEnabled) throw NfcUnavailableException(NfcUnavailableException.Reason.DISABLED)

        val listener: (IsoDep) -> Unit = { isoDep -> trySend(isoDep) }
        mainHandler.post {
            listeners += listener
            resumedActivity?.get()?.let(::enableReaderMode)
        }
        awaitClose {
            mainHandler.post {
                listeners -= listener
                if (listeners.isEmpty()) resumedActivity?.get()?.let(::disableReaderMode)
            }
        }
    }

    /** Called by Android on a binder thread. Tags without IsoDep (e.g. MIFARE Classic) are ignored. */
    private fun onTagDiscovered(tag: Tag) {
        val isoDep = IsoDep.get(tag) ?: return
        listeners.forEach { it(isoDep) }
    }

    private fun enableReaderMode(activity: Activity) {
        adapter?.enableReaderMode(
            activity,
            ::onTagDiscovered,
            NfcAdapter.FLAG_READER_NFC_A or
                NfcAdapter.FLAG_READER_NFC_B or // Calypso cards are usually ISO 14443-B
                NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK or // transit cards carry no NDEF; saves a round trip
                NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS,
            Bundle().apply { putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, PRESENCE_CHECK_DELAY_MILLIS) },
        )
    }

    private fun disableReaderMode(activity: Activity) {
        adapter?.disableReaderMode(activity)
    }

    private companion object {
        const val PRESENCE_CHECK_DELAY_MILLIS = 250
    }
}

/** Empty callbacks, so only the needed ones are overridden. */
private abstract class ActivityLifecycleCallbacksAdapter : Application.ActivityLifecycleCallbacks {
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
    override fun onActivityStarted(activity: Activity) = Unit
    override fun onActivityResumed(activity: Activity) = Unit
    override fun onActivityPaused(activity: Activity) = Unit
    override fun onActivityStopped(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
    override fun onActivityDestroyed(activity: Activity) = Unit
}
