package com.majidbahmani.lisbonav.feature.transportcard.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.majidbahmani.lisbonav.feature.transportcard.domain.model.CardRead
import com.majidbahmani.lisbonav.feature.transportcard.domain.usecase.ReadTransportCardUseCase
import com.majidbahmani.lisbonav.feature.transportcard.presentation.viewmodel.TransportCardUiState.CardShown
import com.majidbahmani.lisbonav.feature.transportcard.presentation.viewmodel.TransportCardUiState.Error
import com.majidbahmani.lisbonav.feature.transportcard.presentation.viewmodel.TransportCardUiState.Reading
import com.majidbahmani.lisbonav.feature.transportcard.presentation.viewmodel.TransportCardUiState.Waiting
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

/**
 * Reads cards while the screen is visible.
 *
 * - Reading (NFC reader mode) runs only while the UI collects [uiState]: it stops at once when the
 *   user leaves the tab or the app, so the NFC radio isn't held for nothing.
 * - Coming back keeps a card already shown, but clears an error or an interrupted read: they belong
 *   to the previous visit (e.g. NFC was switched on in the settings meanwhile).
 * - [retry] starts over, e.g. after turning NFC on.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TransportCardViewModel(
    private val readCard: ReadTransportCardUseCase,
) : ViewModel() {

    private val retryTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    // The last state shown; survives a stop/restart of the reading below.
    private var lastState: TransportCardUiState = Waiting

    val uiState: StateFlow<TransportCardUiState> = retryTrigger
        .onStart { emit(Unit) }
        .flatMapLatest {
            flow {
                if (lastState !is CardShown) emit(Waiting)
                emitAll(readCard().map { it.toUiState() })
            }
        }
        .onEach { lastState = it }
        .stateIn(
            scope = viewModelScope,
            // 0: stop reading as soon as the screen is gone (NFC reader mode off).
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 0),
            initialValue = Waiting,
        )

    fun retry() {
        lastState = Waiting
        retryTrigger.tryEmit(Unit)
    }

    private fun CardRead.toUiState(): TransportCardUiState = when (this) {
        CardRead.Started -> Reading
        is CardRead.Success -> CardShown(card)
        is CardRead.Failure -> Error(reason)
    }
}
