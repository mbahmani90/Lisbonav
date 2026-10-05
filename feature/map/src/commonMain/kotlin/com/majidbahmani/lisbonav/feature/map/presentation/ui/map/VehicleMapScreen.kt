package com.majidbahmani.lisbonav.feature.map.presentation.ui.map

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.majidbahmani.lisbonav.feature.map.presentation.viewmodel.VehicleMapUiState
import com.majidbahmani.lisbonav.feature.map.presentation.viewmodel.VehicleMapUiState.ErrorReason
import com.majidbahmani.lisbonav.feature.map.presentation.viewmodel.VehicleMapViewModel
import com.majidbahmani.lisbonav.feature.map.resources.Res
import com.majidbahmani.lisbonav.feature.map.resources.error_no_connection
import com.majidbahmani.lisbonav.feature.map.resources.error_service
import com.majidbahmani.lisbonav.feature.map.resources.ic_close
import com.majidbahmani.lisbonav.feature.map.resources.ic_search
import com.majidbahmani.lisbonav.feature.map.resources.retry
import com.majidbahmani.lisbonav.feature.map.resources.search_clear
import com.majidbahmani.lisbonav.feature.map.resources.search_line_hint
import com.majidbahmani.lisbonav.feature.map.resources.vehicle_line
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/** Stateful entry point: gets the ViewModel from Koin and collects its state. */
@Composable
fun VehicleMapRoute(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    viewModel: VehicleMapViewModel = koinViewModel(),
) {
    // Lifecycle-aware: stops collecting in the background, which stops the polling (WhileSubscribed).
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    VehicleMapScreen(
        uiState = uiState,
        // Read directly (Compose state), not through uiState, so no keystroke is lost.
        query = viewModel.query,
        onQueryChange = viewModel::onQueryChange,
        onRetry = viewModel::retry,
        modifier = modifier,
        contentPadding = contentPadding,
    )
}

/** Stateless: the map fills the screen; the search bar, loading and errors are shown on top of it. */
@Composable
fun VehicleMapScreen(
    uiState: VehicleMapUiState,
    query: String,
    onQueryChange: (String) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    val linePrefix = stringResource(Res.string.vehicle_line)

    Box(modifier = modifier.fillMaxSize()) {
        VehicleMap(
            vehicles = uiState.vehicles,
            markerTitle = { vehicle -> "$linePrefix ${vehicle.lineId}" },
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPadding,
        )

        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                // Below the status bar / notch, then the requested spacing.
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
                .padding(start = 32.dp, end = 32.dp, top = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            LineSearchBar(query = query, onQueryChange = onQueryChange)

            uiState.error?.let { error ->
                ErrorBanner(error = error, onRetry = onRetry)
            }
        }
    }
}

@Composable
private fun LineSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    // 50 percent of the height on each corner: the radius is always half of the field's height.
    val shape = RoundedCornerShape(percent = 50)
    val focusManager = LocalFocusManager.current
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface)

    // BasicTextField, not OutlinedTextField: Material's field is at least 56 dp high.
    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .height(SearchBarHeight)
            // Light grey, slightly transparent so the map shows through; text and icons stay opaque.
            .background(MaterialTheme.colorScheme.surfaceDim.copy(alpha = 0.7f), shape)
            .border(1.dp, if (isFocused) MaterialTheme.colorScheme.primary else Color.Transparent, shape),
        textStyle = textStyle,
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        singleLine = true,
        interactionSource = interactionSource,
        // The map already filters while typing; "Search" on the keyboard just closes it.
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier.padding(start = 12.dp, end = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(painterResource(Res.drawable.ic_search), contentDescription = null, modifier = Modifier.size(20.dp))
                Box(Modifier.weight(1f).padding(horizontal = 8.dp)) {
                    if (query.isEmpty()) {
                        Text(
                            text = stringResource(Res.string.search_line_hint),
                            style = textStyle,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    innerTextField()
                }
                if (query.isNotEmpty()) {
                    // 40 dp: the bar's height; Material's default 48 dp touch target would not fit.
                    IconButton(onClick = { onQueryChange("") }, modifier = Modifier.size(SearchBarHeight)) {
                        Icon(
                            painterResource(Res.drawable.ic_close),
                            contentDescription = stringResource(Res.string.search_clear),
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
        },
    )
}

/** 30% lower than Material's 56 dp text field. */
private val SearchBarHeight = 40.dp

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
