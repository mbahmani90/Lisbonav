package com.majidbahmani.calypso.nfc.lisboa

import com.majidbahmani.calypso.nfc.ApduResponse
import com.majidbahmani.calypso.nfc.BitWriter
import com.majidbahmani.calypso.nfc.CalypsoFile
import com.majidbahmani.calypso.nfc.RawCalypsoDump
import com.majidbahmani.calypso.nfc.RawFile
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Synthetic records built field by field (no real card data). Days count from 1997-01-01:
 * 10_865 = 2026-09-28, 1_000 = 1999-09-28.
 */
class LisboaCardParserTest {

    private fun days(date: LocalDate) = (date.toEpochDays() - LocalDate(1997, 1, 1).toEpochDays()).toInt()

    private fun environment(
        country: Int = LisboaCardParser.NETWORK_PORTUGAL,
        issue: LocalDate = LocalDate(2020, 3, 1),
        validUntil: LocalDate = LocalDate(2029, 3, 1),
        birthBcd: Long = 0x19_90_07_15, // 1990-07-15
    ) = BitWriter()
        .write(0, 13).write(country, 12).write(0, 5).write(0, 8).write(123_456, 24)
        .write(days(issue), 14).write(days(validUntil), 14).write(0, 15).write(birthBcd, 32)
        .toByteArray()

    private fun contract(provider: Int, tariff: Int, start: LocalDate?, units: Int, period: Int) = BitWriter()
        .write(provider, 7).write(tariff, 16).write(0, 2).write(start?.let(::days) ?: 0, 14)
        .write(0, 5).write(0, 19).write(units, 16).write(0, 14).write(period, 7)
        .toByteArray()

    private fun trip(time: LocalDateTime, transition: Int, provider: Int, slotsBitmap: Int, route: Int, location: Int): ByteArray {
        val seconds = days(time.date).toLong() * 86_400 + time.hour * 3600 + time.minute * 60 + time.second
        return BitWriter()
            .write(seconds, 30).write(0, 3).write(0, 30).write(0, 5).write(slotsBitmap, 4).write(0, 29)
            .write(transition, 3).write(provider, 5).write(0, 16).write(0, 4).write(0, 16)
            .write(route, 16).write(location, 8)
            .toByteArray()
    }

    private fun dump(vararg files: Pair<CalypsoFile, List<ByteArray>>, calypso: Boolean = true) = RawCalypsoDump(
        selectResponse = ApduResponse(ByteArray(0), if (calypso) ApduResponse.SW_SUCCESS else ApduResponse.SW_FILE_NOT_FOUND),
        files = files.map { (file, records) -> RawFile(file, records, stopStatus = null) },
    )

    private val zapping = contract(provider = 31, tariff = 33592, start = LocalDate(2025, 3, 1), units = 0xE10, period = 62)
    private val monthlyPass = contract(provider = 31, tariff = 900, start = LocalDate(2026, 9, 28), units = 0x10A, period = 2)

    @Test
    fun notCalypso_orNotPortugal_returnsNull() {
        assertNull(LisboaCardParser.parse(dump(CalypsoFile.ENVIRONMENT_HOLDER to listOf(environment()), calypso = false)))
        assertNull(LisboaCardParser.parse(dump(CalypsoFile.ENVIRONMENT_HOLDER to listOf(environment(country = 0x250)))))
        assertNull(LisboaCardParser.parse(dump()))
    }

    @Test
    fun environment_issueValidityAndBirthDate() {
        val card = LisboaCardParser.parse(dump(CalypsoFile.ENVIRONMENT_HOLDER to listOf(environment())))!!

        assertEquals(LocalDate(2020, 3, 1), card.issueDate)
        assertEquals(LocalDate(2029, 3, 1), card.validUntil)
        assertEquals(LocalDate(1990, 7, 15), card.holderBirthDate)
    }

    @Test
    fun birthDate_notBcdOrZero_isNull() {
        assertNull(LisboaCardParser.parse(dump(CalypsoFile.ENVIRONMENT_HOLDER to listOf(environment(birthBcd = 0x1990_0A15))))!!.holderBirthDate)
        assertNull(LisboaCardParser.parse(dump(CalypsoFile.ENVIRONMENT_HOLDER to listOf(environment(birthBcd = 0))))!!.holderBirthDate)
    }

    @Test
    fun holderName_latin1_withoutPadding() {
        val name = "JOÃO SILVA".map { it.code.toByte() }.toByteArray() + ByteArray(19) // Ã is 0xC3 in Latin-1
        val card = LisboaCardParser.parse(
            dump(CalypsoFile.ENVIRONMENT_HOLDER to listOf(environment()), CalypsoFile.ID to listOf(name)),
        )!!

        assertEquals("JOÃO SILVA", card.holderName)
    }

    @Test
    fun engravedSerial_fromIccBytes16to19_andMissingFilesAreNull() {
        val icc = ByteArray(29).also { it[16] = 0x00; it[17] = 0x12; it[18] = 0x34; it[19] = 0x56 }
        val withIcc = LisboaCardParser.parse(dump(CalypsoFile.ENVIRONMENT_HOLDER to listOf(environment()), CalypsoFile.ICC to listOf(icc)))!!
        val without = LisboaCardParser.parse(dump(CalypsoFile.ENVIRONMENT_HOLDER to listOf(environment())))!!

        assertEquals(0x123456L, withIcc.engravedSerialNumber)
        assertNull(without.engravedSerialNumber)
        assertNull(without.holderName)
    }

    @Test
    fun contracts_zappingBalance_monthlyPassValidity_emptySlotsSkipped() {
        val counters = byteArrayOf(0x00, 0x00, 0x66) + ByteArray(26) // slot 1: 102 cents
        val card = LisboaCardParser.parse(
            dump(
                CalypsoFile.ENVIRONMENT_HOLDER to listOf(environment()),
                CalypsoFile.CONTRACTS to listOf(zapping, monthlyPass, ByteArray(29), ByteArray(29)),
                CalypsoFile.COUNTERS to listOf(counters),
            ),
        )!!

        assertEquals(listOf(1, 2), card.contracts.map { it.slot })
        val (pay, pass) = card.contracts
        assertEquals(LisboaTariff.ZAPPING, pay.knownTariff)
        assertEquals(102, pay.balanceCents)
        assertNull(pay.validUntil)
        assertNull(pass.knownTariff) // 900: no known name
        assertEquals(LocalDate(2026, 9, 28), pass.startDate)
        assertEquals(LocalDate(2026, 10, 31), pass.validUntil) // bought before the month it covers
        assertNull(pass.balanceCents)
    }

    @Test
    fun contract_validForDays() {
        val tenDays = contract(provider = 31, tariff = 906, start = LocalDate(2026, 10, 1), units = 0x109, period = 10)
        val card = LisboaCardParser.parse(dump(CalypsoFile.ENVIRONMENT_HOLDER to listOf(environment()), CalypsoFile.CONTRACTS to listOf(tenDays)))!!

        assertEquals(LisboaTariff.NAVEGANTE_LISBOA, card.contracts.single().knownTariff)
        assertEquals(LocalDate(2026, 10, 10), card.contracts.single().validUntil)
    }

    @Test
    fun trips_timeOperatorTransitionAndSlots_emptyRecordsSkipped() {
        val card = LisboaCardParser.parse(
            dump(
                CalypsoFile.ENVIRONMENT_HOLDER to listOf(environment()),
                CalypsoFile.EVENT_LOG to listOf(
                    trip(LocalDateTime(2026, 10, 2, 21, 7, 30), transition = 4, provider = 2, slotsBitmap = 0b0010, route = 5, location = 17),
                    trip(LocalDateTime(2026, 10, 2, 19, 41, 0), transition = 1, provider = 1, slotsBitmap = 0b0011, route = 735, location = 3),
                    ByteArray(29),
                ),
            ),
        )!!

        assertEquals(2, card.trips.size)
        val (metro, bus) = card.trips
        assertEquals(LocalDateTime(2026, 10, 2, 21, 7, 30), metro.time)
        assertEquals(LisboaTrip.Transition.TAP_OFF, metro.transition)
        assertEquals(LisboaOperator.METRO, metro.operator)
        assertEquals(setOf(2), metro.contractSlotsUsed)
        assertEquals(17, metro.locationId)
        assertEquals(LisboaTrip.Transition.TAP_ON, bus.transition)
        assertEquals(LisboaOperator.CARRIS, bus.operator)
        assertEquals(735, bus.routeNumber)
        assertEquals(setOf(1, 2), bus.contractSlotsUsed)
    }

    @Test
    fun unknownOperatorAndTransition() {
        val other = trip(LocalDateTime(2026, 1, 1, 8, 0), transition = 6, provider = 20, slotsBitmap = 0, route = 0, location = 0)
        val trip = LisboaCardParser.parse(dump(CalypsoFile.ENVIRONMENT_HOLDER to listOf(environment()), CalypsoFile.EVENT_LOG to listOf(other)))!!.trips.single()

        assertNull(trip.operator)
        assertEquals(20, trip.provider)
        assertEquals(LisboaTrip.Transition.OTHER, trip.transition)
        assertTrue(trip.contractSlotsUsed.isEmpty())
    }
}
