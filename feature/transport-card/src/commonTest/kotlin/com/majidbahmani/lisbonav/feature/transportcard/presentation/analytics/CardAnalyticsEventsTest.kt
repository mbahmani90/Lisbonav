package com.majidbahmani.lisbonav.feature.transportcard.presentation.analytics

import com.majidbahmani.lisbonav.feature.transportcard.domain.model.CardRead
import com.majidbahmani.lisbonav.feature.transportcard.domain.model.CardRead.Reason
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CardAnalyticsEventsTest {

    @Test
    fun everyFailureReason_hasItsOwnResult() {
        val results = Reason.entries.associateWith { CardAnalyticsEvents.cardRead(CardRead.Failure(it))?.params?.get("result") }

        assertEquals(
            mapOf(
                Reason.NFC_NOT_SUPPORTED to "no_nfc",
                Reason.NFC_DISABLED to "nfc_off",
                Reason.CARD_REMOVED to "card_removed",
                Reason.NOT_A_NAVEGANTE_CARD to "not_navegante",
            ),
            results,
        )
    }

    @Test
    fun readStarted_isNotAnOutcome() {
        assertNull(CardAnalyticsEvents.cardRead(CardRead.Started))
    }
}
