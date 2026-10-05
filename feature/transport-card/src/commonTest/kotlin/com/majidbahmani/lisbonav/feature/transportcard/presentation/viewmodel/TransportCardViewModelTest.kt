package com.majidbahmani.lisbonav.feature.transportcard.presentation.viewmodel

import com.majidbahmani.lisbonav.analytics.AnalyticsEvent
import com.majidbahmani.lisbonav.feature.transportcard.domain.model.CardRead
import com.majidbahmani.lisbonav.feature.transportcard.domain.model.CardRead.Reason
import com.majidbahmani.lisbonav.feature.transportcard.domain.model.TransportCard
import com.majidbahmani.lisbonav.feature.transportcard.domain.model.TransportPass
import com.majidbahmani.lisbonav.feature.transportcard.domain.repository.TransportCardReader
import com.majidbahmani.lisbonav.feature.transportcard.domain.usecase.ReadTransportCardUseCase
import com.majidbahmani.lisbonav.feature.transportcard.fake.FakeAnalytics
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class TransportCardViewModelTest {

    @BeforeTest
    fun setUp() = Dispatchers.setMain(StandardTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    /** Like the real reader: listens while collected; taps are emitted by the test. */
    private class ScriptedCardReader : TransportCardReader {
        val taps = MutableSharedFlow<CardRead>()
        var nfcOff = false
        var startedReadings = 0
            private set
        var activeReadings = 0
            private set

        override fun readCards(): Flow<CardRead> = flow {
            startedReadings++
            activeReadings++
            try {
                if (nfcOff) emit(CardRead.Failure(Reason.NFC_DISABLED)) else emitAll(taps)
            } finally {
                activeReadings--
            }
        }
    }

    private val reader = ScriptedCardReader()
    private val analytics = FakeAnalytics()

    // The real use case with a fake reader (doc 22). Lazy: created after setMain, so viewModelScope
    // runs on the test dispatcher.
    private val viewModel by lazy { TransportCardViewModel(ReadTransportCardUseCase(reader), analytics) }

    private val today = LocalDate(2026, 10, 5)
    private val expired =
        TransportPass(TransportPass.Type.OTHER, 1, LocalDate(2026, 9, 1), LocalDate(2026, 9, 30), null)
    private val october =
        TransportPass(TransportPass.Type.OTHER, 2, LocalDate(2026, 9, 28), LocalDate(2026, 10, 31), null)
    private val card =
        TransportCard("123", null, null, passes = listOf(expired, october), trips = emptyList(), readOn = today)

    /** The screen collecting the state; cancel the returned job to "leave the screen". */
    private fun TestScope.showScreen() = backgroundScope.launch { viewModel.uiState.collect {} }

    private suspend fun TestScope.tap(vararg reads: CardRead) {
        reads.forEach { reader.taps.emit(it) }
        runCurrent()
    }

    @Test
    fun readsOnlyWhileTheScreenIsShown() = runTest {
        runCurrent()
        assertEquals(TransportCardUiState.Waiting, viewModel.uiState.value)
        assertEquals(0, reader.startedReadings)

        val screen = showScreen()
        runCurrent()
        assertEquals(1, reader.activeReadings)

        screen.cancel()
        runCurrent()
        assertEquals(0, reader.activeReadings) // stops at once: NFC reader mode off
    }

    @Test
    fun tap_showsReadingThenTheCard_withPassesOrderedByTheUseCase() = runTest {
        showScreen()
        runCurrent()

        tap(CardRead.Started)
        assertEquals(TransportCardUiState.Reading, viewModel.uiState.value)

        tap(CardRead.Success(card))
        assertEquals(
            TransportCardUiState.CardShown(card.copy(passes = listOf(october, expired))),
            viewModel.uiState.value,
        )
    }

    @Test
    fun cardRemoved_showsError_andTheNextTapIsReadWithoutRetry() = runTest {
        showScreen()
        runCurrent()

        tap(CardRead.Started, CardRead.Failure(Reason.CARD_REMOVED))
        assertEquals(TransportCardUiState.Error(Reason.CARD_REMOVED), viewModel.uiState.value)

        tap(CardRead.Started, CardRead.Success(card))
        assertEquals(1, reader.startedReadings)
        assertEquals("123", (viewModel.uiState.value as TransportCardUiState.CardShown).card.number)
    }

    @Test
    fun nfcOff_showsError_andRetryStartsReadingAgain() = runTest {
        reader.nfcOff = true
        showScreen()
        runCurrent()
        assertEquals(TransportCardUiState.Error(Reason.NFC_DISABLED), viewModel.uiState.value)

        reader.nfcOff = false // the user switched NFC on
        viewModel.retry()
        runCurrent()

        assertEquals(TransportCardUiState.Waiting, viewModel.uiState.value)
        assertEquals(2, reader.startedReadings)
        assertEquals(1, reader.activeReadings)
    }

    @Test
    fun comingBack_keepsTheCardShown_andReadsAgain() = runTest {
        val screen = showScreen()
        runCurrent()
        tap(CardRead.Started, CardRead.Success(card))
        screen.cancel()
        runCurrent()

        showScreen()
        runCurrent()

        assertEquals("123", (viewModel.uiState.value as TransportCardUiState.CardShown).card.number)
        assertEquals(2, reader.startedReadings)
    }

    @Test
    fun comingBack_clearsAnErrorOrAnInterruptedRead() = runTest {
        val screen = showScreen()
        runCurrent()
        tap(CardRead.Started, CardRead.Failure(Reason.NOT_A_NAVEGANTE_CARD))
        screen.cancel()
        runCurrent()

        showScreen()
        runCurrent()

        assertEquals(TransportCardUiState.Waiting, viewModel.uiState.value)
    }

    // --- Analytics ---

    private fun cardRead(result: String) = AnalyticsEvent("card_read", mapOf("result" to result))

    @Test
    fun eachOutcome_logsOneCardRead_withoutCardData() = runTest {
        showScreen()
        runCurrent()

        tap(CardRead.Started) // still reading: nothing yet
        assertEquals(emptyList(), analytics.events)

        tap(CardRead.Success(card))
        tap(CardRead.Started, CardRead.Failure(Reason.CARD_REMOVED))
        tap(CardRead.Started, CardRead.Failure(Reason.NOT_A_NAVEGANTE_CARD))

        // Only the result: no card number, passes or trips.
        assertEquals(
            listOf(cardRead("success"), cardRead("card_removed"), cardRead("not_navegante")),
            analytics.events,
        )
    }

    @Test
    fun nfcOff_logsCardReadNfcOff() = runTest {
        reader.nfcOff = true
        showScreen()
        runCurrent()

        assertEquals(listOf(cardRead("nfc_off")), analytics.events)
    }
}
