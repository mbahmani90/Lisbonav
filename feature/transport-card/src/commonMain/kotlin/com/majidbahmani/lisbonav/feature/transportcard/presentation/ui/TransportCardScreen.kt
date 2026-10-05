package com.majidbahmani.lisbonav.feature.transportcard.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.majidbahmani.lisbonav.feature.transportcard.resources.Res
import com.majidbahmani.lisbonav.feature.transportcard.resources.card_coming_soon
import com.majidbahmani.lisbonav.feature.transportcard.resources.card_title
import org.jetbrains.compose.resources.stringResource

/**
 * Placeholder until card reading is wired to :calypso-nfc: shows where the feature will live.
 * Stateless, so no ViewModel yet.
 */
@Composable
fun TransportCardScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CardIllustration()
        Text(
            text = stringResource(Res.string.card_title),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 32.dp),
        )
        Text(
            text = stringResource(Res.string.card_coming_soon),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
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
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.25f), RoundedCornerShape(6.dp)),
        )
    }
}
