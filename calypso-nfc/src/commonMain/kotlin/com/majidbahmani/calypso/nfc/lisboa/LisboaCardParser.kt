package com.majidbahmani.calypso.nfc.lisboa

import com.majidbahmani.calypso.nfc.BitReader
import com.majidbahmani.calypso.nfc.CalypsoFile
import com.majidbahmani.calypso.nfc.RawCalypsoDump
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.minus
import kotlinx.datetime.plus

/**
 * Decodes a Lisbon card from a raw Calypso read (EN 1545 / Intercode bit layout).
 *
 * Field layout as documented by the Metrodroid project (Lisboa Viva reader), checked against a
 * real Navegante card; this is an independent implementation. Unknown fields are skipped.
 */
object LisboaCardParser {

    /** EN 1545 country code of Portugal, in the environment record. */
    const val NETWORK_PORTUGAL = 0x131

    /** EN 1545 dates count days (and date-times seconds) from this day, Lisbon local time. */
    private val EPOCH = LocalDate(1997, 1, 1)

    private const val PERIOD_UNIT_DAYS = 0x109
    private const val PERIOD_UNIT_MONTHS = 0x10A

    /** Null when the card isn't a Lisbon Calypso card (or its environment can't be read). */
    fun parse(dump: RawCalypsoDump): LisboaCard? {
        if (!dump.isCalypso) return null
        val environment = dump.records(CalypsoFile.ENVIRONMENT_HOLDER).firstOrNull() ?: return null
        val env = BitReader(environment)
        env.skip(13)
        if (env.readInt(12) != NETWORK_PORTUGAL) return null
        env.skip(5 + 8 + 24) // unknown, serial prefix, internal serial
        val issueDate = date(env.readInt(14))
        val validUntil = date(env.readInt(14))
        env.skip(15)
        val birthDate = bcdDate(env.read(32))

        val counters = dump.records(CalypsoFile.COUNTERS).firstOrNull()
        return LisboaCard(
            engravedSerialNumber = dump.records(CalypsoFile.ICC).firstOrNull()?.engravedSerial(),
            holderName = dump.records(CalypsoFile.ID).firstOrNull()?.latin1Text(),
            holderBirthDate = birthDate,
            issueDate = issueDate,
            validUntil = validUntil,
            contracts = dump.records(CalypsoFile.CONTRACTS).mapIndexedNotNull { index, record ->
                if (record.isAllZero()) null else contract(slot = index + 1, record, counters)
            },
            trips = dump.records(CalypsoFile.EVENT_LOG).mapNotNull { record ->
                if (record.isAllZero()) null else trip(record)
            },
        )
    }

    private fun contract(slot: Int, record: ByteArray, counters: ByteArray?): LisboaContract {
        val bits = BitReader(record)
        val provider = bits.readInt(7)
        val tariff = bits.readInt(16)
        bits.skip(2)
        val start = date(bits.readInt(14))
        bits.skip(5 + 19) // sale agent, unknown
        val periodUnits = bits.readInt(16)
        bits.skip(14) // a date whose meaning isn't known (not the end of validity)
        val period = bits.readInt(7)

        val validUntil = when {
            start == null -> null
            periodUnits == PERIOD_UNIT_DAYS -> start.plus(DatePeriod(days = period - 1))
            // Calendar months: valid until the end of the month `period` months after the start month.
            periodUnits == PERIOD_UNIT_MONTHS ->
                LocalDate(start.year, start.month, 1).plus(DatePeriod(months = period)).minus(DatePeriod(days = 1))
            else -> null
        }
        val isZapping = LisboaTariff.of(provider, tariff) == LisboaTariff.ZAPPING
        return LisboaContract(
            slot = slot,
            provider = provider,
            tariff = tariff,
            startDate = start,
            validUntil = validUntil,
            // The shared counters record holds one 3-byte value per contract slot.
            balanceCents = if (isZapping) counters?.uint24At(3 * (slot - 1)) else null,
        )
    }

    private fun trip(record: ByteArray): LisboaTrip {
        val bits = BitReader(record)
        val time = dateTime(bits.read(30))
        bits.skip(3 + 30 + 5) // unknown, first validation of the journey, unknown
        val contractsBitmap = bits.readInt(4)
        bits.skip(29)
        val transition = when (bits.readInt(3)) {
            1 -> LisboaTrip.Transition.TAP_ON
            3 -> LisboaTrip.Transition.TRANSFER
            4 -> LisboaTrip.Transition.TAP_OFF
            else -> LisboaTrip.Transition.OTHER
        }
        val provider = bits.readInt(5)
        bits.skip(16 + 4 + 16) // vehicle, unknown, device
        val route = bits.readInt(16)
        val location = bits.readInt(8)
        return LisboaTrip(
            time = time,
            transition = transition,
            provider = provider,
            routeNumber = route,
            locationId = location,
            // Bit 0 (least significant) is slot 1.
            contractSlotsUsed = (0 until 4).filter { contractsBitmap shr it and 1 == 1 }.map { it + 1 }.toSet(),
        )
    }

    private fun date(days: Int): LocalDate? = if (days == 0) null else EPOCH.plus(DatePeriod(days = days))

    /** Seconds since the epoch, in Lisbon local time. */
    private fun dateTime(seconds: Long): LocalDateTime {
        val day = EPOCH.plus(DatePeriod(days = (seconds / 86_400).toInt()))
        val second = (seconds % 86_400).toInt()
        return LocalDateTime(day, LocalTime(second / 3600, second / 60 % 60, second % 60))
    }

    /** YYYYMMDD in binary-coded decimal; null when zero or not a real date. */
    private fun bcdDate(value: Long): LocalDate? {
        if (value == 0L) return null
        val digits = value.toString(16).padStart(8, '0')
        if (!digits.all { it.isDigit() }) return null
        return runCatching {
            LocalDate(digits.substring(0, 4).toInt(), digits.substring(4, 6).toInt(), digits.substring(6, 8).toInt())
        }.getOrNull()
    }

    private fun ByteArray.engravedSerial(): Long? {
        if (size < 20) return null
        return (16 until 20).fold(0L) { acc, i -> (acc shl 8) or (this[i].toLong() and 0xFF) }.takeIf { it != 0L }
    }

    /** Latin-1 text, without the zero / space padding. */
    private fun ByteArray.latin1Text(): String? =
        map { (it.toInt() and 0xFF).toChar() }.joinToString("").trim { it == ' ' || it == '\u0000' }.takeIf { it.isNotEmpty() }

    private fun ByteArray.uint24At(offset: Int): Int? {
        if (offset + 3 > size) return null
        return ((this[offset].toInt() and 0xFF) shl 16) or ((this[offset + 1].toInt() and 0xFF) shl 8) or (this[offset + 2].toInt() and 0xFF)
    }

    private fun ByteArray.isAllZero() = all { it == 0.toByte() }
}
