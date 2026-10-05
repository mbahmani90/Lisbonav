package com.majidbahmani.lisbonav.presentation.ui.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.majidbahmani.lisbonav.presentation.viewmodel.VehicleMapUiState
import com.majidbahmani.lisbonav.presentation.viewmodel.VehicleMapUiState.ErrorReason
import com.majidbahmani.lisbonav.presentation.viewmodel.VehicleMapViewModel
import lisbonav.shared.generated.resources.Res
import lisbonav.shared.generated.resources.error_no_connection
import lisbonav.shared.generated.resources.error_service
import lisbonav.shared.generated.resources.retry
import lisbonav.shared.generated.resources.vehicle_line
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/** Stateful entry point: gets the ViewModel from Koin and collects its state. */
@Composable
fun VehicleMapRoute(
    modifier: Modifier = Modifier,
    viewModel: VehicleMapViewModel = koinViewModel(),
) {
    // Lifecycle-aware: stops collecting in the background, which stops the polling (WhileSubscribed).
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    VehicleMapScreen(
        uiState = uiState,
        onRetry = viewModel::retry,
        modifier = modifier,
    )
}

/** Stateless: the map fills the screen; loading and errors are shown on top of it. */
@Composable
fun VehicleMapScreen(
    uiState: VehicleMapUiState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val linePrefix = stringResource(Res.string.vehicle_line)

    Box(modifier = modifier.fillMaxSize()) {
        VehicleMap(
            vehicles = uiState.vehicles,
            markerTitle = { vehicle -> "$linePrefix ${vehicle.lineId}" },
            modifier = Modifier.fillMaxSize(),
        )

        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }

        uiState.error?.let { error ->
            ErrorBanner(
                error = error,
                onRetry = onRetry,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(16.dp),
            )
        }
    }
}

@Composable
private fun ErrorBanner(
    error: ErrorReason,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val message = when (error) {
        ErrorReason.NO_CONNECTION -> stringResource(Res.string.error_no_connection)
        ErrorReason.SERVICE -> stringResource(Res.string.error_service)
    }
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        shape = MaterialTheme.shapes.medium,
        shadowElevation = 4.dp,
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = message, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            Spacer(Modifier.width(8.dp))
            TextButton(onClick = onRetry) {
                Text(stringResource(Res.string.retry))
            }
        }
    }
}
