package com.majidbahmani.lisbonav.feature.transportcard.domain.repository

import com.majidbahmani.lisbonav.feature.transportcard.domain.model.CardRead
import kotlinx.coroutines.flow.Flow

interface TransportCardReader {

    /**
     * Reads every card held to the phone while collected: [CardRead.Started], then a success or a
     * failure, per tap. Stop collecting to stop reading (NFC reader mode off).
     * When NFC is missing or off, emits that failure and completes.
     */
    fun readCards(): Flow<CardRead>
}
