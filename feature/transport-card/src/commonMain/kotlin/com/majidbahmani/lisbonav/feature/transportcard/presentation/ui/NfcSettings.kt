package com.majidbahmani.lisbonav.feature.transportcard.presentation.ui

import androidx.compose.runtime.Composable

/** Opens the system NFC settings; null where the platform has none to open (iOS). */
@Composable
internal expect fun rememberOpenNfcSettings(): (() -> Unit)?
