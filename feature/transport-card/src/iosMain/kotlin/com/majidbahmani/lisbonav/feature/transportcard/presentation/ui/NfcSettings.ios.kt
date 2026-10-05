package com.majidbahmani.lisbonav.feature.transportcard.presentation.ui

import androidx.compose.runtime.Composable

/** iOS has no NFC switch to open. */
@Composable
internal actual fun rememberOpenNfcSettings(): (() -> Unit)? = null
