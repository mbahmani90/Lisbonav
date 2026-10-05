package com.majidbahmani.lisbonav.feature.consent.presentation.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.majidbahmani.lisbonav.feature.consent.presentation.viewmodel.ConsentViewModel
import com.majidbahmani.lisbonav.feature.consent.resources.Res
import com.majidbahmani.lisbonav.feature.consent.resources.consent_allow
import com.majidbahmani.lisbonav.feature.consent.resources.consent_deny
import com.majidbahmani.lisbonav.feature.consent.resources.consent_message
import com.majidbahmani.lisbonav.feature.consent.resources.consent_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * The consent feature's entry point: the app puts it over its first screen. Shows the question
 * until the user answers, then nothing.
 */
@Composable
fun AnalyticsConsentDialog(viewModel: ConsentViewModel = koinViewModel()) {
    val showDialog by viewModel.showDialog.collectAsStateWithLifecycle()

    if (showDialog) {
        ConsentDialog(onAllow = viewModel::onAllow, onDeny = viewModel::onDeny)
    }
}

/**
 * Stateless. Both answers look the same (GDPR: refusing must be as easy as allowing), and the
 * dialog needs one of them: back and taps outside don't close it.
 */
@Composable
fun ConsentDialog(
    onAllow: () -> Unit,
    onDeny: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text(stringResource(Res.string.consent_title)) },
        text = { Text(stringResource(Res.string.consent_message)) },
        confirmButton = { TextButton(onClick = onAllow) { Text(stringResource(Res.string.consent_allow)) } },
        dismissButton = { TextButton(onClick = onDeny) { Text(stringResource(Res.string.consent_deny)) } },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
        modifier = modifier,
    )
}
