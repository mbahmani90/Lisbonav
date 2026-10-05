package com.majidbahmani.lisbonav.feature.transportcard.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.majidbahmani.lisbonav.feature.transportcard.domain.model.CardRead.Reason
import com.majidbahmani.lisbonav.feature.transportcard.domain.model.CardTrip
import com.majidbahmani.lisbonav.feature.transportcard.domain.model.PassStatus
import com.majidbahmani.lisbonav.feature.transportcard.domain.model.TransportCard
import com.majidbahmani.lisbonav.feature.transportcard.domain.model.TransportPass
import com.majidbahmani.lisbonav.feature.transportcard.domain.model.statusOn
import com.majidbahmani.lisbonav.feature.transportcard.presentation.viewmodel.TransportCardUiState
import com.majidbahmani.lisbonav.feature.transportcard.presentation.viewmodel.TransportCardViewModel
import com.majidbahmani.lisbonav.feature.transportcard.resources.Res
import com.majidbahmani.lisbonav.feature.transportcard.resources.card_birth_date
import com.majidbahmani.lisbonav.feature.transportcard.resources.card_error_nfc_off
import com.majidbahmani.lisbonav.feature.transportcard.resources.card_error_nfc_off_hint
import com.majidbahmani.lisbonav.feature.transportcard.resources.card_error_no_nfc
import com.majidbahmani.lisbonav.feature.transportcard.resources.card_error_no_nfc_hint
import com.majidbahmani.lisbonav.feature.transportcard.resources.card_error_not_navegante
import com.majidbahmani.lisbonav.feature.transportcard.resources.card_error_not_navegante_hint
import com.majidbahmani.lisbonav.feature.transportcard.resources.card_error_removed
import com.majidbahmani.lisbonav.feature.transportcard.resources.card_error_removed_hint
import com.majidbahmani.lisbonav.feature.transportcard.resources.card_hint
import com.majidbahmani.lisbonav.feature.transportcard.resources.card_no_passes
import com.majidbahmani.lisbonav.feature.transportcard.resources.card_no_trips
import com.majidbahmani.lisbonav.feature.transportcard.resources.card_number
import com.majidbahmani.lisbonav.feature.transportcard.resources.card_number_unknown
import com.majidbahmani.lisbonav.feature.transportcard.resources.card_open_settings
import com.majidbahmani.lisbonav.feature.transportcard.resources.card_passes
import com.majidbahmani.lisbonav.feature.transportcard.resources.card_read_another
import com.majidbahmani.lisbonav.feature.transportcard.resources.card_reading
import com.majidbahmani.lisbonav.feature.transportcard.resources.card_title
import com.majidbahmani.lisbonav.feature.transportcard.resources.card_trips
import com.majidbahmani.lisbonav.feature.transportcard.resources.card_try_again
import com.majidbahmani.lisbonav.feature.transportcard.resources.card_valid_until
import com.majidbahmani.lisbonav.feature.transportcard.resources.operator_carris
import com.majidbahmani.lisbonav.feature.transportcard.resources.operator_cp
import com.majidbahmani.lisbonav.feature.transportcard.resources.operator_metro
import com.majidbahmani.lisbonav.feature.transportcard.resources.operator_other
import com.majidbahmani.lisbonav.feature.transportcard.resources.pass_active
import com.majidbahmani.lisbonav.feature.transportcard.resources.pass_active_until
import com.majidbahmani.lisbonav.feature.transportcard.resources.pass_balance
import com.majidbahmani.lisbonav.feature.transportcard.resources.pass_expired
import com.majidbahmani.lisbonav.feature.transportcard.resources.pass_navegante_lisboa
import com.majidbahmani.lisbonav.feature.transportcard.resources.pass_other
import com.majidbahmani.lisbonav.feature.transportcard.resources.pass_starts
import com.majidbahmani.lisbonav.feature.transportcard.resources.pass_zapping
import com.majidbahmani.lisbonav.feature.transportcard.resources.trip_other
import com.majidbahmani.lisbonav.feature.transportcard.resources.trip_tap_off
import com.majidbahmani.lisbonav.feature.transportcard.resources.trip_tap_on
import com.majidbahmani.lisbonav.feature.transportcard.resources.trip_transfer
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/** Stateful entry point: gets the ViewModel from Koin and collects its state. */
@Composable
fun TransportCardRoute(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    viewModel: TransportCardViewModel = koinViewModel(),
) {
    // Lifecycle-aware: stops collecting in the background, which stops NFC reading (WhileSubscribed(0)).
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TransportCardScreen(
        uiState = uiState,
        onRetry = viewModel::retry,
        onOpenNfcSettings = rememberOpenNfcSettings(),
        modifier = modifier,
        contentPadding = contentPadding,
    )
}

/**
 * Stateless: renders the state; [onOpenNfcSettings] is null where there are no NFC settings (iOS).
 * [contentPadding]: space taken by the app's floating bars (it already includes the system bar below them).
 */
@Composable
fun TransportCardScreen(
    uiState: TransportCardUiState,
    onRetry: () -> Unit,
    onOpenNfcSettings: (() -> Unit)?,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            // The bottom comes from contentPadding: the list scrolls behind the floating bar.
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)),
    ) {
        if (uiState is TransportCardUiState.CardShown) {
            CardDetails(card = uiState.card, bottomPadding = contentPadding.calculateBottomPadding())
        } else {
            // Prompts are centred in the space above the bar.
            Box(Modifier.padding(bottom = contentPadding.calculateBottomPadding())) {
                when (uiState) {
                    TransportCardUiState.Waiting -> Prompt(title = Res.string.card_title, hint = Res.string.card_hint)

                    TransportCardUiState.Reading -> Prompt(
                        title = Res.string.card_reading,
                        hint = null,
                        isReading = true,
                    )

                    is TransportCardUiState.Error -> ErrorPrompt(uiState.reason, onRetry, onOpenNfcSettings)

                    is TransportCardUiState.CardShown -> Unit
                }
            }
        }
    }
}

@Composable
private fun ErrorPrompt(reason: Reason, onRetry: () -> Unit, onOpenNfcSettings: (() -> Unit)?) {
    when (reason) {
        // Reading stopped: the user has to change something, then start again.
        Reason.NFC_DISABLED -> Prompt(Res.string.card_error_nfc_off, Res.string.card_error_nfc_off_hint) {
            onOpenNfcSettings?.let { Button(onClick = it) { Text(stringResource(Res.string.card_open_settings)) } }
            OutlinedButton(onClick = onRetry) { Text(stringResource(Res.string.card_try_again)) }
        }

        Reason.NFC_NOT_SUPPORTED -> Prompt(Res.string.card_error_no_nfc, Res.string.card_error_no_nfc_hint)

        // Still reading: the next tap is read without any button.
        Reason.CARD_REMOVED -> Prompt(Res.string.card_error_removed, Res.string.card_error_removed_hint)

        Reason.NOT_A_NAVEGANTE_CARD -> Prompt(
            Res.string.card_error_not_navegante,
            Res.string.card_error_not_navegante_hint,
        )
    }
}

@Composable
private fun Prompt(
    title: StringResource,
    hint: StringResource?,
    isReading: Boolean = false,
    actions: @Composable () -> Unit = {},
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CardIllustration()
        Spacer(Modifier.height(32.dp))
        if (isReading) {
            CircularProgressIndicator()
            Spacer(Modifier.height(16.dp))
        }
        Text(stringResource(title), style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        hint?.let {
            Text(
                text = stringResource(it),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
        Column(
            modifier = Modifier.padding(top = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) { actions() }
    }
}

@Composable
private fun CardDetails(card: TransportCard, bottomPadding: Dp) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 24.dp, top = 24.dp, end = 24.dp, bottom = 24.dp + bottomPadding),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item(key = "header") {
            Column(Modifier.padding(bottom = 16.dp)) {
                Text(
                    text =
                        card.number?.let { stringResource(Res.string.card_number, it) }
                            ?: stringResource(Res.string.card_number_unknown),
                    style = MaterialTheme.typography.headlineSmall,
                )
                card.validUntil?.let {
                    Text(
                        stringResource(Res.string.card_valid_until, it.formatted()),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                card.holderBirthDate?.let {
                    Text(
                        text = stringResource(Res.string.card_birth_date, it.formatted()),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        item(key = "passes-title") { SectionTitle(Res.string.card_passes) }
        if (card.passes.isEmpty()) {
            item(key = "no-passes") { EmptyLine(Res.string.card_no_passes) }
        }
        items(card.passes, key = { "pass-${it.tariffCode}-${it.startDate}" }) { pass ->
            PassRow(pass, status = pass.statusOn(card.readOn))
        }

        item(key = "trips-title") { SectionTitle(Res.string.card_trips, topPadding = 16) }
        if (card.trips.isEmpty()) {
            item(key = "no-trips") { EmptyLine(Res.string.card_no_trips) }
        }
        items(card.trips, key = { "trip-${it.time}-${it.kind}" }) { trip -> TripRow(trip) }

        item(key = "footer") { EmptyLine(Res.string.card_read_another, topPadding = 16) }
    }
}

@Composable
private fun PassRow(pass: TransportPass, status: PassStatus) {
    val name = when (pass.type) {
        TransportPass.Type.ZAPPING -> stringResource(Res.string.pass_zapping)
        TransportPass.Type.NAVEGANTE_LISBOA -> stringResource(Res.string.pass_navegante_lisboa)
        TransportPass.Type.OTHER -> stringResource(Res.string.pass_other, pass.tariffCode.toString())
    }
    val detail = when (status) {
        PassStatus.ACTIVE -> pass.validUntil?.let { stringResource(Res.string.pass_active_until, it.formatted()) }
            ?: stringResource(Res.string.pass_active)

        PassStatus.NOT_STARTED -> stringResource(Res.string.pass_starts, pass.startDate?.formatted().orEmpty())

        PassStatus.STORED_VALUE -> stringResource(Res.string.pass_balance, euros(pass.balanceCents ?: 0))

        PassStatus.EXPIRED -> stringResource(Res.string.pass_expired, pass.validUntil?.formatted().orEmpty())
    }
    val isExpired = status == PassStatus.EXPIRED
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                color = if (isExpired) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            )
            Text(
                text = detail,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isExpired) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.primary
                },
            )
        }
    }
}

@Composable
private fun TripRow(trip: CardTrip) {
    val operator = when (trip.operator) {
        CardTrip.Operator.CARRIS -> Res.string.operator_carris
        CardTrip.Operator.METRO -> Res.string.operator_metro
        CardTrip.Operator.CP -> Res.string.operator_cp
        CardTrip.Operator.OTHER -> Res.string.operator_other
    }
    val kind = when (trip.kind) {
        CardTrip.Kind.TAP_ON -> Res.string.trip_tap_on
        CardTrip.Kind.TAP_OFF -> Res.string.trip_tap_off
        CardTrip.Kind.TRANSFER -> Res.string.trip_transfer
        CardTrip.Kind.OTHER -> Res.string.trip_other
    }
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(stringResource(operator), style = MaterialTheme.typography.titleSmall)
            Text(
                stringResource(kind),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(trip.time.formatted(), style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun SectionTitle(text: StringResource, topPadding: Int = 0) {
    Text(
        text = stringResource(text),
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(top = topPadding.dp, bottom = 4.dp),
    )
}

@Composable
private fun EmptyLine(text: StringResource, topPadding: Int = 0) {
    Text(
        text = stringResource(text),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = topPadding.dp),
    )
}

/** A simple transit card: brand yellow with a chip, drawn from the theme colours (no image asset). */
@Composable
private fun CardIllustration() {
    Box(
        modifier = Modifier
            .size(width = 200.dp, height = 126.dp)
            .background(MaterialTheme.colorScheme.secondary, RoundedCornerShape(16.dp)),
    ) {
        Box(
            modifier = Modifier
                .padding(start = 24.dp, top = 44.dp)
                .size(width = 40.dp, height = 32.dp)
                .background(MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.25f), RoundedCornerShape(6.dp)),
        )
    }
}

// English month names for now; a localised date format comes with translations.
private val DATE_FORMAT = LocalDate.Format {
    day(padding = Padding.NONE)
    char(' ')
    monthName(MonthNames.ENGLISH_ABBREVIATED)
    char(' ')
    year()
}

private val DATE_TIME_FORMAT = LocalDateTime.Format {
    date(DATE_FORMAT)
    char(',')
    char(' ')
    hour()
    char(':')
    minute()
}

private fun LocalDate.formatted(): String = DATE_FORMAT.format(this)

private fun LocalDateTime.formatted(): String = DATE_TIME_FORMAT.format(this)

/** 102 → "€1.02". */
internal fun euros(cents: Int): String = "€${cents / 100}.${(cents % 100).toString().padStart(2, '0')}"
