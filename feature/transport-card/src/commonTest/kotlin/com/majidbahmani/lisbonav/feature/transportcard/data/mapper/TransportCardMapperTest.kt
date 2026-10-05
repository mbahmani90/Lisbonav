package com.majidbahmani.lisbonav.feature.transportcard.data.mapper

import com.majidbahmani.calypso.nfc.lisboa.LisboaContract
import com.majidbahmani.calypso.nfc.lisboa.LisboaTrip
import com.majidbahmani.lisbonav.feature.transportcard.domain.model.CardTrip
import com.majidbahmani.lisbonav.feature.transportcard.domain.model.TransportCard
import com.majidbahmani.lisbonav.feature.transportcard.domain.model.TransportPass
import com.majidbahmani.lisbonav.feature.transportcard.fake.lisboaCard
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

class TransportCardMapperTest {

    @Test
    fun card_mapsAllFields() {
        assertEquals(
            TransportCard(
                number = "123456789",
                holderBirthDate = LocalDate(1990, 7, 15),
                validUntil = LocalDate(2029, 3, 1),
                passes = listOf(
                    TransportPass(
                        TransportPass.Type.ZAPPING,
                        33592,
                        LocalDate(2025, 3, 1),
                        validUntil = null,
                        balanceCents = 102,
                    ),
                    TransportPass(
                        TransportPass.Type.OTHER,
                        900,
                        LocalDate(2026, 9, 28),
                        LocalDate(2026, 10, 31),
                        balanceCents = null,
                    ),
                ),
                trips = listOf(
                    CardTrip(
                        LocalDateTime(2026, 10, 2, 21, 7),
                        CardTrip.Operator.METRO,
                        CardTrip.Kind.TAP_OFF,
                        routeNumber = 5,
                    ),
                    CardTrip(
                        LocalDateTime(2026, 10, 2, 19, 41),
                        CardTrip.Operator.CARRIS,
                        CardTrip.Kind.TAP_ON,
                        routeNumber = 735,
                    ),
                ),
                readOn = LocalDate(2026, 10, 5),
            ),
            lisboaCard().toDomain(readOn = LocalDate(2026, 10, 5)),
        )
    }

    @Test
    fun pass_navegLisboaTariff() {
        val pass = LisboaContract(
            slot = 1,
            provider = 31,
            tariff = 906,
            startDate = null,
            validUntil = null,
            balanceCents = null,
        ).toDomain()
        assertEquals(TransportPass.Type.NAVEGANTE_LISBOA, pass.type)
    }

    @Test
    fun trip_otherOperatorsAndKinds() {
        fun trip(provider: Int, transition: LisboaTrip.Transition) =
            LisboaTrip(
                LocalDateTime(2026, 1, 1, 8, 0),
                transition,
                provider,
                routeNumber = 0,
                locationId = 0,
                contractSlotsUsed = emptySet(),
            ).toDomain()

        assertEquals(CardTrip.Operator.CP, trip(3, LisboaTrip.Transition.TRANSFER).operator)
        assertEquals(CardTrip.Kind.TRANSFER, trip(3, LisboaTrip.Transition.TRANSFER).kind)
        assertEquals(CardTrip.Operator.OTHER, trip(20, LisboaTrip.Transition.OTHER).operator)
        assertEquals(CardTrip.Kind.OTHER, trip(20, LisboaTrip.Transition.OTHER).kind)
    }

    @Test
    fun missingNumber_staysNull() {
        assertEquals(
            null,
            lisboaCard().copy(engravedSerialNumber = null).toDomain(readOn = LocalDate(2026, 10, 5)).number,
        )
    }
}
