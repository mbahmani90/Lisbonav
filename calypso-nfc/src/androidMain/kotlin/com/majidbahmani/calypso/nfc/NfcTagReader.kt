package com.majidbahmani.calypso.nfc

import android.app.Activity
import android.app.Application
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference
import kotlin.coroutines.resume

/**
 * Waits for an ISO 14443-4 card (Calypso, DESFire, EMV, …) using NFC reader mode.
 *
 * Reader mode only works for a resumed Activity, so this follows the app's activities through
 * [Application.ActivityLifecycleCallbacks] (weak reference, no leak) and turns reader mode on only
 * while [awaitIsoDep] is waiting. Create it once, in `Application.onCreate`.
 */
class NfcTagReader(application: Application) {

    private val adapter: NfcAdapter? = NfcAdapter.getDefaultAdapter(application)
    private val mainHandler = Handler(Looper.getMainLooper())

    // Both only touched on the main thread.
    private var resumedActivity: WeakReference<Activity>? = null
    private var waiting: CancellableContinuation<IsoDep>? = null

    init {
        application.registerActivityLifecycleCallbacks(
            object : ActivityLifecycleCallbacksAdapter() {
                override fun onActivityResumed(activity: Activity) {
                    resumedActivity = WeakReference(activity)
                    if (waiting != null) enableReaderMode(activity)
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
     * Suspends until a card is tapped and returns it as [IsoDep] (not yet connected).
     * Throws [NfcUnavailableException] without NFC. Cancelling stops reader mode.
     */
    suspend fun awaitIsoDep(): IsoDep = withContext(Dispatchers.Main.immediate) {
        val adapter = adapter ?: throw NfcUnavailableException(NfcUnavailableException.Reason.NOT_SUPPORTED)
        if (!adapter.isEnabled) throw NfcUnavailableException(NfcUnavailableException.Reason.DISABLED)

        suspendCancellableCoroutine { continuation ->
            check(waiting == null) { "Only one awaitIsoDep() at a time" }
            waiting = continuation
            resumedActivity?.get()?.let(::enableReaderMode)
            continuation.invokeOnCancellation {
                mainHandler.post {
                    if (waiting === continuation) {
                        waiting = null
                        resumedActivity?.get()?.let(::disableReaderMode)
                    }
                }
            }
        }
    }

    /** Called by Android on a binder thread. Tags without IsoDep (e.g. MIFARE Classic) are ignored. */
    private fun onTagDiscovered(tag: Tag) {
        val isoDep = IsoDep.get(tag) ?: return
        mainHandler.post {
            val continuation = waiting ?: return@post
            waiting = null
            resumedActivity?.get()?.let(::disableReaderMode)
            if (continuation.isActive) continuation.resume(isoDep)
        }
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
