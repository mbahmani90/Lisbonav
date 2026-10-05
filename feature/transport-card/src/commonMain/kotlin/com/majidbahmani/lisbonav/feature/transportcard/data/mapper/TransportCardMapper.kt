package com.majidbahmani.lisbonav.feature.transportcard.data.mapper

import com.majidbahmani.calypso.nfc.lisboa.LisboaCard
import com.majidbahmani.calypso.nfc.lisboa.LisboaContract
import com.majidbahmani.calypso.nfc.lisboa.LisboaOperator
import com.majidbahmani.calypso.nfc.lisboa.LisboaTariff
import com.majidbahmani.calypso.nfc.lisboa.LisboaTrip
import com.majidbahmani.lisbonav.feature.transportcard.domain.model.CardTrip
import com.majidbahmani.lisbonav.feature.transportcard.domain.model.TransportCard
import com.majidbahmani.lisbonav.feature.transportcard.domain.model.TransportPass
import kotlinx.datetime.LocalDate

/** SDK model → domain. The holder name isn't used: the card doesn't give it without operator keys. */
internal fun LisboaCard.toDomain(readOn: LocalDate): TransportCard = TransportCard(
    number = engravedSerialNumber?.toString(),
    holderBirthDate = holderBirthDate,
    validUntil = validUntil,
    passes = contracts.map { it.toDomain() },
    trips = trips.map { it.toDomain() },
    readOn = readOn,
)

internal fun LisboaContract.toDomain(): TransportPass = TransportPass(
    type = when (knownTariff) {
        LisboaTariff.ZAPPING -> TransportPass.Type.ZAPPING
        LisboaTariff.NAVEGANTE_LISBOA -> TransportPass.Type.NAVEGANTE_LISBOA
        null -> TransportPass.Type.OTHER
    },
    tariffCode = tariff,
    startDate = startDate,
    validUntil = validUntil,
    balanceCents = balanceCents,
)

internal fun LisboaTrip.toDomain(): CardTrip = CardTrip(
    time = time,
    operator = when (operator) {
        LisboaOperator.CARRIS -> CardTrip.Operator.CARRIS
        LisboaOperator.METRO -> CardTrip.Operator.METRO
        LisboaOperator.CP -> CardTrip.Operator.CP
        null -> CardTrip.Operator.OTHER
    },
    kind = when (transition) {
        LisboaTrip.Transition.TAP_ON -> CardTrip.Kind.TAP_ON
        LisboaTrip.Transition.TRANSFER -> CardTrip.Kind.TRANSFER
        LisboaTrip.Transition.TAP_OFF -> CardTrip.Kind.TAP_OFF
        LisboaTrip.Transition.OTHER -> CardTrip.Kind.OTHER
    },
    routeNumber = routeNumber,
)
