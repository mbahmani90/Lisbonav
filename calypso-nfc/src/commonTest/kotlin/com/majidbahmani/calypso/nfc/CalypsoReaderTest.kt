package com.majidbahmani.calypso.nfc

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CalypsoReaderTest {

    private val select = "00A4040008315449432E49434100"

    // Made-up record bytes: the real layout comes from the step-1 dump of a real card.
    private val environment = "11223344"
    private val contract1 = "AAAA"
    private val contract2 = "BBBB"

    private fun cardWith(vararg responses: Pair<String, String>) =
        FakeCardTransport(mapOf(select to "6F00" + "9000", *responses))

    private fun RawCalypsoDump.file(file: CalypsoFile) = files.single { it.file == file }

    @Test
    fun notACalypsoCard_returnsDumpWithoutFiles() = runTest {
        val card = FakeCardTransport(mapOf(select to "6A82"))

        val dump = CalypsoReader(card).readRaw()

        assertFalse(dump.isCalypso)
        assertTrue(dump.files.isEmpty())
        assertEquals(listOf(select), card.sentCommands)
    }

    @Test
    fun readsRecordsUntilRecordNotFound() = runTest {
        val card = cardWith(
            "00B2013C00" to environment + "9000",
            "00B2014C00" to contract1 + "9000",
            "00B2024C00" to contract2 + "9000",
            "00B2034C00" to "6A83",
        )

        val dump = CalypsoReader(card).readRaw()

        assertTrue(dump.isCalypso)
        assertEquals(listOf(environment), dump.file(CalypsoFile.ENVIRONMENT_HOLDER).records.map { it.toHex() })
        // ENVIRONMENT_HOLDER has 1 record max: no further READ RECORD, no stop status.
        assertNull(dump.file(CalypsoFile.ENVIRONMENT_HOLDER).stopStatus)
        assertEquals(listOf(contract1, contract2), dump.file(CalypsoFile.CONTRACTS).records.map { it.toHex() })
        assertEquals("6A83", dump.file(CalypsoFile.CONTRACTS).stopStatus)
    }

    @Test
    fun missingFile_isReported_andReadingContinues() = runTest {
        val card = cardWith(
            "00B2013C00" to environment + "9000",
            // event log (SFI 08 → P2 44) not present: default 6A82
            "00B2014C00" to contract1 + "9000",
            "00B2024C00" to "6A83",
        )

        val dump = CalypsoReader(card).readRaw()

        assertEquals("6A82", dump.file(CalypsoFile.EVENT_LOG).stopStatus)
        assertTrue(dump.file(CalypsoFile.EVENT_LOG).records.isEmpty())
        // Files after the missing one were still read.
        assertEquals(listOf(contract1), dump.file(CalypsoFile.CONTRACTS).records.map { it.toHex() })
        assertEquals(CalypsoFile.entries.size, dump.files.size)
    }

    @Test
    fun classNotSupported_switchesToCalypsoClass_forTheRestOfTheRead() = runTest {
        val card = cardWith(
            "00B2013C00" to "6E00",
            "94B2013C00" to environment + "9000",
            "94B2014C00" to contract1 + "9000",
        )

        val dump = CalypsoReader(card).readRaw()

        assertEquals(listOf(environment), dump.file(CalypsoFile.ENVIRONMENT_HOLDER).records.map { it.toHex() })
        assertEquals(listOf(contract1), dump.file(CalypsoFile.CONTRACTS).records.take(1).map { it.toHex() })
        // After the refused command, no command uses class 00 again.
        val refused = card.sentCommands.indexOf("00B2013C00")
        assertTrue(card.sentCommands.drop(refused + 1).none { it.startsWith("00B2") })
    }

    @Test
    fun wrongLength_isRetriedWithTheLengthTheCardAsks() = runTest {
        val card = cardWith(
            "00B2013C00" to "6C04",
            "00B2013C04" to environment + "9000",
        )

        val dump = CalypsoReader(card).readRaw()

        assertEquals(listOf(environment), dump.file(CalypsoFile.ENVIRONMENT_HOLDER).records.map { it.toHex() })
    }
}
