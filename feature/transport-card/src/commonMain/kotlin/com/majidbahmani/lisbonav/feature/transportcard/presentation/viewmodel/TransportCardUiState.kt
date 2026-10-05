package com.majidbahmani.lisbonav.feature.transportcard.presentation.viewmodel

import com.majidbahmani.lisbonav.feature.transportcard.domain.model.CardRead
import com.majidbahmani.lisbonav.feature.transportcard.domain.model.TransportCard

sealed interface TransportCardUiState {
    /** Ready: hold a card to the phone. */
    data object Waiting : TransportCardUiState

    /** A card is being read: it must stay in place. */
    data object Reading : TransportCardUiState

    data class CardShown(val card: TransportCard) : TransportCardUiState

    /** The reason, not a message: the screen picks the text and the actions. */
    data class Error(val reason: CardRead.Reason) : TransportCardUiState
}
