package com.majidbahmani.lisbonav.feature.transportcard.presentation.analytics

import com.majidbahmani.lisbonav.analytics.AnalyticsEvent
import com.majidbahmani.lisbonav.feature.transportcard.domain.model.CardRead
import com.majidbahmani.lisbonav.feature.transportcard.domain.model.CardRead.Reason

/** The card reader's analytics events: the outcome only, never card data (number, passes, trips). */
internal object CardAnalyticsEvents {

    /** `card_read` for an outcome; null while a read is still going on ([CardRead.Started]). */
    fun cardRead(read: CardRead): AnalyticsEvent? = when (read) {
        CardRead.Started -> null
        is CardRead.Success -> cardRead("success")
        is CardRead.Failure -> cardRead(read.reason.toResult())
    }

    private fun cardRead(result: String) = AnalyticsEvent("card_read", mapOf("result" to result))

    private fun Reason.toResult() = when (this) {
        Reason.NFC_NOT_SUPPORTED -> "no_nfc"
        Reason.NFC_DISABLED -> "nfc_off"
        Reason.CARD_REMOVED -> "card_removed"
        Reason.NOT_A_NAVEGANTE_CARD -> "not_navegante"
    }
}
