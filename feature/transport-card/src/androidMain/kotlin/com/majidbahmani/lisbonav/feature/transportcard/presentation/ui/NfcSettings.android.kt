package com.majidbahmani.lisbonav.feature.transportcard.presentation.ui

import android.content.Intent
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
internal actual fun rememberOpenNfcSettings(): (() -> Unit)? {
    val context = LocalContext.current
    return remember(context) { { context.startActivity(Intent(Settings.ACTION_NFC_SETTINGS)) } }
}
