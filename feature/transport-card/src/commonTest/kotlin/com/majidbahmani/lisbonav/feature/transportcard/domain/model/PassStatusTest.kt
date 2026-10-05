package com.majidbahmani.lisbonav.feature.transportcard.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.datetime.LocalDate

class PassStatusTest {

    private val today = LocalDate(2026, 10, 5)

    private fun pass(start: LocalDate? = null, until: LocalDate? = null, balance: Int? = null) =
        TransportPass(
            TransportPass.Type.OTHER,
            tariffCode = 900,
            startDate = start,
            validUntil = until,
            balanceCents = balance,
        )

    @Test
    fun withinItsPeriod_isActive_includingTheFirstAndLastDay() {
        assertEquals(PassStatus.ACTIVE, pass(LocalDate(2026, 10, 1), LocalDate(2026, 10, 31)).statusOn(today))
        assertEquals(PassStatus.ACTIVE, pass(today, LocalDate(2026, 10, 31)).statusOn(today))
        assertEquals(PassStatus.ACTIVE, pass(LocalDate(2026, 9, 1), today).statusOn(today))
    }

    @Test
    fun afterItsLastDay_isExpired() {
        assertEquals(PassStatus.EXPIRED, pass(LocalDate(2026, 9, 1), LocalDate(2026, 9, 30)).statusOn(today))
    }

    @Test
    fun beforeItsFirstDay_isNotStarted() {
        assertEquals(PassStatus.NOT_STARTED, pass(LocalDate(2026, 11, 1), LocalDate(2026, 11, 30)).statusOn(today))
    }

    @Test
    fun aBalance_isStoredValue_whateverTheDates() {
        assertEquals(PassStatus.STORED_VALUE, pass(LocalDate(2025, 1, 1), balance = 102).statusOn(today))
    }

    @Test
    fun noDates_countsAsActive() {
        assertEquals(PassStatus.ACTIVE, pass().statusOn(today))
    }
}
